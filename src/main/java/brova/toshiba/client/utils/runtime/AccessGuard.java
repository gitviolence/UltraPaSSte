package brova.toshiba.client.utils.runtime;

/**
 * Explicit fallback semantics of the former generated reflection helpers.
 * Receivers and arguments are evaluated by the caller before entering the guard.
 * Actual field/method access is statically compiled Java, with no reflection or JNI.
 */
public final class AccessGuard {

    private AccessGuard() {
    }

    @FunctionalInterface
    public interface Read<T, R> {

        R read(T receiver) throws Exception;
    }

    @FunctionalInterface
    public interface Write<T, V> {

        void write(T receiver, V value) throws Exception;
    }

    @FunctionalInterface
    public interface Invoke<T, R> {

        R invoke(T receiver, Object[] args) throws Throwable;
    }

    @FunctionalInterface
    public interface InvokeVoid<T> {

        void invoke(T receiver, Object[] args) throws Throwable;
    }

    public static <T, R> R read(T receiver, Read<T, R> access) {
        try {
            return access.read(receiver);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T, V> Object write(T receiver, V value, Write<T, V> access) {
        try {
            access.write(receiver, value);
        } catch (Exception e) {
        }
        return null;
    }

    public static <R> R invoke(Object receiver, Object[] args, Invoke<Object, R> access) {
        try {
            return access.invoke(receiver, args);
        } catch (Throwable e) {
            return null;
        }
    }

    public static Object invokeVoid(Object receiver, Object[] args, InvokeVoid<Object> access) {
        try {
            access.invoke(receiver, args);
        } catch (Throwable e) {
        }
        return null;
    }
}
