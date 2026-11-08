package brova.toshiba.client.event;

import java.lang.reflect.Method;

public record ListenerMethod(Object listener, Method method, int priority, long registrationOrder, int methodOrder) {
}
