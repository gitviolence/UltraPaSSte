package brova.toshiba.client.event;

import brova.toshiba.client.core.PanicController;
import brova.toshiba.client.utils.profile.PerformanceProfiler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class EventBus {

    private static final ConcurrentHashMap<Class<?>, Object> putIfAbsentObjectByClass = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<?>, Long> orDefaultLongByClass = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Class<? extends EventBase>, List<ListenerMethod>> putAllListenerMethodByClass = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Long> handlerWarningTimes = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Long> eventWarningTimes = new ConcurrentHashMap<>();

    private static volatile boolean perfDebug = Boolean.parseBoolean(System.getProperty("toshiba.perf.debug", "false"));

    private static final long handlerWarningThresholdMillis = Long.getLong("toshiba.perf.handlerMs", 8L) * 1000000L;

    private static final long eventWarningThresholdMillis = Long.getLong("toshiba.perf.eventMs", 18L) * 1000000L;

    private static final long lastUpdateTimestampForBase = Long.getLong("toshiba.perf.cooldownMs", 1000L) * 1000000L;

    private static volatile boolean stateFlag = true;

    private static final AtomicLong atomicLong = new AtomicLong();

    public static void setPerfDebug(boolean perfDebugValue) {
        perfDebug = perfDebugValue;
    }

    public static boolean isPerfDebug() {
        return perfDebug;
    }

    public static void register(Object value) {
        if (!PanicController.isActive() && putIfAbsentObjectByClass.putIfAbsent(value.getClass(), value) == null) {
            orDefaultLongByClass.put(value.getClass(), atomicLong.getAndIncrement());
            stateFlag = true;
        }
    }

    public static void unregister(Object value) {
        putIfAbsentObjectByClass.remove(value.getClass());
        orDefaultLongByClass.remove(value.getClass());
        stateFlag = true;
    }

    public static void clean() {
        putIfAbsentObjectByClass.clear();
        orDefaultLongByClass.clear();
        putAllListenerMethodByClass.clear();
        stateFlag = false;
    }

    public static void invoke(EventBase eventBase) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (PanicController.isActive()) {
            return;
        }
        PerformanceProfiler profiler = PerformanceProfiler.INSTANCE;
        boolean profiling = profiler.isRunning() || perfDebug;
        long eventStartNanos = profiling ? System.nanoTime() : 0L;
        if (stateFlag) {
            updatePriorityWithCompare();
        }
        List<ListenerMethod> listenerMethods = putAllListenerMethodByClass.get(eventBase.getClass());
        if (listenerMethods == null || listenerMethods.isEmpty()) {
            return;
        }
        for (ListenerMethod listenerMethod : listenerMethods) {
            if (PanicController.isActive()) {
                return;
            }
            Object listener = brova.toshiba.client.utils.runtime.AccessGuard.invoke(listenerMethod, new Object[] {}, (accessReceiver, accessArguments) -> ((ListenerMethod) accessReceiver).listener());
            if (!putIfAbsentObjectByClass.containsKey(listener.getClass())) {
                continue;
            }
            Method method = brova.toshiba.client.utils.runtime.AccessGuard.invoke(listenerMethod, new Object[] {}, (accessReceiver, accessArguments) -> ((ListenerMethod) accessReceiver).method());
            long handlerStartNanos = profiling ? System.nanoTime() : 0L;
            try {
                method.invoke(listener, eventBase);
            } catch (IllegalAccessException | InvocationTargetException failure) {
                if (profiling) {
                    recordHandlerTiming(profiler, eventBase, listenerMethod, method, handlerStartNanos);
                }
                throw failure;
            }
            if (profiling) {
                recordHandlerTiming(profiler, eventBase, listenerMethod, method, handlerStartNanos);
            }
        }
        if (profiling) {
            long eventElapsedNanos = System.nanoTime() - eventStartNanos;
            profiler.record("Event:" + eventBase.getClass().getSimpleName(), eventElapsedNanos);
            if (eventElapsedNanos >= eventWarningThresholdMillis) {
                onBase(eventBase, eventElapsedNanos, listenerMethods.size());
            }
        }
    }

    private static void recordHandlerTiming(PerformanceProfiler profiler, EventBase eventBase, ListenerMethod listenerMethod, Method method, long startNanos) {
        long elapsedNanos = System.nanoTime() - startNanos;
        String handlerName = listenerMethod.listener().getClass().getSimpleName() + "#" + method.getName();
        profiler.record(handlerName, elapsedNanos);
        if (elapsedNanos >= handlerWarningThresholdMillis) {
            onBase(eventBase, listenerMethod, elapsedNanos);
        }
    }

    public static boolean hasListeners(Class<? extends EventBase> type) {
        if (PanicController.isActive()) {
            return false;
        } else {
            if (stateFlag) {
                updatePriorityWithCompare();
            }
            var values = putAllListenerMethodByClass.get(type);
            return values != null && !values.isEmpty();
        }
    }

    private static synchronized void updatePriorityWithCompare() {
        if (stateFlag) {
            ConcurrentHashMap<Class<? extends EventBase>, List<ListenerMethod>> concurrentHashMap = new ConcurrentHashMap<>();
            for (Object value : putIfAbsentObjectByClass.values()) {
                Method[] declaredMethods = value.getClass().getDeclaredMethods();
                for (int index = 0; index < declaredMethods.length; index++) {
                    Method method = declaredMethods[index];
                    if (method.isAnnotationPresent(EventHandler.class)) {
                        var parameterTypes = method.getParameterTypes();
                        if (parameterTypes.length == 1 && EventBase.class.isAssignableFrom(parameterTypes[0])) {
                            Class<? extends EventBase> typeValue = parameterTypes[0].asSubclass(EventBase.class);
                            method.setAccessible(true);
                            concurrentHashMap.computeIfAbsent(typeValue, type -> new ArrayList<>()).add(new ListenerMethod(value, method, method.getAnnotation(EventHandler.class).priority(), orDefaultLongByClass.getOrDefault(value.getClass(), Long.MAX_VALUE), index));
                        }
                    }
                }
            }
            for (List<ListenerMethod> listenerMethods : concurrentHashMap.values()) {
                listenerMethods.sort((leftListenerMethod, rightListenerMethod) -> {
                    int intValue = Integer.compare(rightListenerMethod.priority(), leftListenerMethod.priority());
                    if (intValue != 0) {
                        return intValue;
                    } else {
                        int intValueValue = Long.compare(leftListenerMethod.registrationOrder(), rightListenerMethod.registrationOrder());
                        return intValueValue != 0 ? intValueValue : Integer.compare(leftListenerMethod.methodOrder(), rightListenerMethod.methodOrder());
                    }
                });
            }
            putAllListenerMethodByClass.clear();
            putAllListenerMethodByClass.putAll(concurrentHashMap);
            stateFlag = false;
        }
    }

    private static void onBase(EventBase eventBase, ListenerMethod listenerMethod, long longValue) {
        long currentTimeNanos = System.nanoTime();
        String text = listenerMethod.listener().getClass().getName() + "#" + listenerMethod.method().getName();
        Long timestamp = handlerWarningTimes.get(text);
        if (timestamp == null || currentTimeNanos - timestamp >= lastUpdateTimestampForBase) {
            handlerWarningTimes.put(text, currentTimeNanos);
            double doubleValue = longValue / 1000000.0;
            System.err.printf(Locale.US, "[Toshiba Perf Warning] Handler %s took %.2f ms for event %s%n", text, doubleValue, eventBase.getClass().getSimpleName());
        }
    }

    private static void onBase(EventBase eventBase, long longValue, int intValue) {
        long currentTimeNanos = System.nanoTime();
        String name = eventBase.getClass().getName();
        Long timestamp = eventWarningTimes.get(name);
        if (timestamp == null || currentTimeNanos - timestamp >= lastUpdateTimestampForBase) {
            eventWarningTimes.put(name, currentTimeNanos);
            double doubleValue = longValue / 1000000.0;
            System.err.printf(Locale.US, "[Toshiba Perf Warning] Event %s took %.2f ms across %d listeners%n", name, doubleValue, intValue);
        }
    }
}
