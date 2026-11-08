package brova.toshiba.client.utils.world;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class BaritoneController {

    private static Vec3d lastStuckPosition;

    private static long lastMovementMillis;

    private static int forwardEscapeTicks;

    private static boolean escapeRight;

    private static boolean wasPathing;

    private static boolean backingOut;

    private static boolean backwardEscapeRight;

    private static Vec3d backwardStartPosition;

    private static long backwardStartMillis;

    private static boolean baritoneUnavailable;

    private static Class<?> baritoneApiClass;

    private static Method getProviderMethod;

    private static Class<?> providerClass;

    private static Method getPrimaryBaritoneMethod;

    private static Class<?> inputEnumClass;

    private static final Map<String, Object> inputsByName = new HashMap<>();

    private static final Map<String, Method> noArgMethodCache = new HashMap<>();

    private static Class<?> inputHandlerClass;

    private static Method isInputForcedDownMethod;

    private static Method setInputForceStateMethod;

    private static Method clearAllKeysMethod;

    private BaritoneController() {
    }

    public static void onGameMessage(String text) {
        if (text != null && text.contains("Извините, но вы не можете сломать блок здесь")) {
            wasPathing = true;
        }
    }

    public static void tick() {
        MinecraftClient instance = MinecraftClient.getInstance();
        if (instance.player != null && instance.world != null) {
            try {
                Object value = findPrimaryBaritone();
                if (value == null) {
                    resetUnstuckState();
                    return;
                }
                Object valueValue = invokeCachedNoArgMethod(value, "getPathingBehavior");
                Object valueResult = invokeCachedNoArgMethod(value, "getInputOverrideHandler");
                if (valueValue == null || valueResult == null || !Boolean.TRUE.equals(invokeCachedNoArgMethod(valueValue, "isPathing"))) {
                    updateObject(valueResult);
                    clearStuckPosition();
                    return;
                }
                long currentTimeMillis = System.currentTimeMillis();
                Vec3d pos = instance.player.getPos();
                if (lastStuckPosition == null) {
                    lastStuckPosition = pos;
                    lastMovementMillis = currentTimeMillis;
                }
                if (wasPathing && isBreakingBlock(instance, valueResult)) {
                    updateCurrentTimeMillis(instance, pos);
                    wasPathing = false;
                }
                if (backingOut) {
                    if (!(pos.squaredDistanceTo(backwardStartPosition) >= 2500.0) && currentTimeMillis - backwardStartMillis < 25000L) {
                        applyBackwardUnstuckInput(instance, valueResult);
                        lastStuckPosition = pos;
                        lastMovementMillis = currentTimeMillis;
                        return;
                    }
                    clearAllForcedKeys(valueResult);
                    backingOut = false;
                    lastStuckPosition = pos;
                    lastMovementMillis = currentTimeMillis;
                    return;
                }
                if (forwardEscapeTicks > 0) {
                    applyForwardUnstuckInput(instance, valueResult);
                    forwardEscapeTicks--;
                    if (forwardEscapeTicks <= 0) {
                        clearAllForcedKeys(valueResult);
                        lastStuckPosition = instance.player.getPos();
                        lastMovementMillis = currentTimeMillis;
                    }
                    return;
                }
                if (isBreakingBlock(instance, valueResult)) {
                    lastStuckPosition = pos;
                    lastMovementMillis = currentTimeMillis;
                    return;
                }
                if (!isObject(valueResult)) {
                    lastStuckPosition = pos;
                    lastMovementMillis = currentTimeMillis;
                    return;
                }
                if (pos.squaredDistanceTo(lastStuckPosition) >= 1.0) {
                    lastStuckPosition = pos;
                    lastMovementMillis = currentTimeMillis;
                    return;
                }
                if (currentTimeMillis - lastMovementMillis < 7000L) {
                    return;
                }
                forwardEscapeTicks = 12;
                escapeRight = isMultiplyWithToRadians(instance, escapeRight, true);
                applyForwardUnstuckInput(instance, valueResult);
                lastStuckPosition = pos;
                lastMovementMillis = currentTimeMillis;
            } catch (Throwable failure) {
                resetUnstuckState();
            }
        } else {
            resetUnstuckState();
        }
    }

    private static Object findPrimaryBaritone() throws ReflectiveOperationException {
        if (baritoneUnavailable) {
            return null;
        } else {
            if (baritoneApiClass == null) {
                try {
                    baritoneApiClass = Class.forName("baritone.api.BaritoneAPI");
                    getProviderMethod = baritoneApiClass.getMethod("getProvider");
                } catch (ClassNotFoundException failure) {
                    baritoneUnavailable = true;
                    return null;
                }
            }
            Object value = getProviderMethod.invoke(null);
            if (value == null) {
                return null;
            } else {
                var classValue = value.getClass();
                if (getPrimaryBaritoneMethod == null || providerClass != classValue) {
                    providerClass = classValue;
                    getPrimaryBaritoneMethod = classValue.getMethod("getPrimaryBaritone");
                }
                return getPrimaryBaritoneMethod.invoke(value);
            }
        }
    }

    private static boolean isBreakingBlock(MinecraftClient client, Object value) throws ReflectiveOperationException {
        return client.interactionManager != null && client.interactionManager.isBreakingBlock() || isInputForcedDown(value, "CLICK_LEFT");
    }

    private static boolean isObject(Object value) throws ReflectiveOperationException {
        return isInputForcedDown(value, "MOVE_FORWARD") || isInputForcedDown(value, "MOVE_BACK") || isInputForcedDown(value, "MOVE_LEFT") || isInputForcedDown(value, "MOVE_RIGHT") || isInputForcedDown(value, "JUMP");
    }

    private static void updateCurrentTimeMillis(MinecraftClient client, Vec3d position) {
        backingOut = true;
        backwardStartPosition = position;
        backwardStartMillis = System.currentTimeMillis();
        backwardEscapeRight = isMultiplyWithToRadians(client, backwardEscapeRight, false);
    }

    private static void applyForwardUnstuckInput(MinecraftClient client, Object value) throws ReflectiveOperationException {
        clearAllForcedKeys(value);
        setInputForcedDown(value, "MOVE_FORWARD", true);
        setInputForcedDown(value, escapeRight ? "MOVE_RIGHT" : "MOVE_LEFT", true);
        if (client.player != null && client.player.isOnGround()) {
            setInputForcedDown(value, "JUMP", true);
        }
    }

    private static void applyBackwardUnstuckInput(MinecraftClient client, Object value) throws ReflectiveOperationException {
        clearAllForcedKeys(value);
        setInputForcedDown(value, "MOVE_BACK", true);
        setInputForcedDown(value, backwardEscapeRight ? "MOVE_RIGHT" : "MOVE_LEFT", true);
        if (client.player != null && client.player.isOnGround()) {
            setInputForcedDown(value, "JUMP", true);
        }
    }

    private static boolean isMultiplyWithToRadians(MinecraftClient client, boolean flag, boolean flagValue) {
        if (client.player == null) {
            return flag;
        } else {
            double doubleValue = Math.toRadians(client.player.getYaw());
            Vec3d position = new Vec3d(-MathHelper.sin((float) doubleValue), 0.0, MathHelper.cos((float) doubleValue));
            Vec3d positionValue = new Vec3d(position.z, 0.0, -position.x);
            Vec3d positionResult = positionValue.multiply(-1.0);
            Vec3d positionItem = flagValue ? position : position.multiply(-1.0);
            double doubleValueValue = calculateOffsetWithSpaceEmpty(client, positionValue.multiply(0.95).add(positionItem.multiply(0.35)));
            double doubleValueResult = calculateOffsetWithSpaceEmpty(client, positionResult.multiply(0.95).add(positionItem.multiply(0.35)));
            return doubleValueValue == doubleValueResult ? flag : doubleValueResult > doubleValueValue;
        }
    }

    private static double calculateOffsetWithSpaceEmpty(MinecraftClient client, Vec3d position) {
        Box box = client.player.getBoundingBox().offset(position);
        double doubleValue = 0.0;
        if (client.world.isSpaceEmpty(client.player, box)) {
            doubleValue++;
        }
        if (client.world.isSpaceEmpty(client.player, box.offset(0.0, 1.0, 0.0))) {
            doubleValue += 0.35;
        }
        return doubleValue;
    }

    private static void updateObject(Object value) {
        if (forwardEscapeTicks > 0 && value != null) {
            try {
                clearAllForcedKeys(value);
            } catch (ReflectiveOperationException failure) {
            }
        }
        forwardEscapeTicks = 0;
        if (backingOut && value != null) {
            try {
                clearAllForcedKeys(value);
            } catch (ReflectiveOperationException failureValue) {
            }
        }
        backingOut = false;
        wasPathing = false;
    }

    private static void clearStuckPosition() {
        lastStuckPosition = null;
        lastMovementMillis = 0L;
    }

    private static void resetUnstuckState() {
        forwardEscapeTicks = 0;
        lastStuckPosition = null;
        lastMovementMillis = 0L;
        wasPathing = false;
        backingOut = false;
        backwardStartPosition = null;
        backwardStartMillis = 0L;
    }

    private static boolean isInputForcedDown(Object value, String text) throws ReflectiveOperationException {
        Object valueValue = createObjectFromForNameWithAsSubclass(text);
        updateMethodWithClass(value, valueValue);
        Object valueResult = isInputForcedDownMethod.invoke(value, valueValue);
        return Boolean.TRUE.equals(valueResult);
    }

    private static void setInputForcedDown(Object value, String text, boolean flag) throws ReflectiveOperationException {
        Object valueValue = createObjectFromForNameWithAsSubclass(text);
        updateMethodWithClass(value, valueValue);
        setInputForceStateMethod.invoke(value, valueValue, flag);
    }

    private static void clearAllForcedKeys(Object value) throws ReflectiveOperationException {
        updateMethodWithClass(value, createObjectFromForNameWithAsSubclass("MOVE_FORWARD"));
        clearAllKeysMethod.invoke(value);
    }

    private static Object createObjectFromForNameWithAsSubclass(String text) throws ReflectiveOperationException {
        if (inputEnumClass == null) {
            inputEnumClass = Class.forName("baritone.api.utils.input.Input");
        }
        Object value = inputsByName.get(text);
        if (value != null) {
            return value;
        } else {
            for (Object constant : inputEnumClass.asSubclass(Enum.class).getEnumConstants()) {
                if (constant instanceof Enum<?> input && input.name().equals(text)) {
                    inputsByName.put(text, input);
                    return input;
                }
            }
            throw new IllegalArgumentException("No enum constant " + inputEnumClass.getName() + "." + text);
        }
    }

    private static Object invokeCachedNoArgMethod(Object value, String text) throws ReflectiveOperationException {
        var classValue = value.getClass();
        String textValue = classValue.getName() + "#" + text;
        Method method = noArgMethodCache.get(textValue);
        if (method == null) {
            method = classValue.getMethod(text);
            noArgMethodCache.put(textValue, method);
        }
        return method.invoke(value);
    }

    private static void updateMethodWithClass(Object value, Object valueValue) throws ReflectiveOperationException {
        var classValue = value.getClass();
        if (inputHandlerClass != classValue || isInputForcedDownMethod == null || setInputForceStateMethod == null || clearAllKeysMethod == null) {
            inputHandlerClass = classValue;
            var classValueValue = valueValue.getClass();
            isInputForcedDownMethod = classValue.getMethod("isInputForcedDown", classValueValue);
            setInputForceStateMethod = classValue.getMethod("setInputForceState", classValueValue, boolean.class);
            clearAllKeysMethod = classValue.getMethod("clearAllKeys");
        }
    }
}
