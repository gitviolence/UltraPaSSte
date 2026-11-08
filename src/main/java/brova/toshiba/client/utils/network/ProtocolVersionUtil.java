package brova.toshiba.client.utils.network;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProtocolVersionUtil {

    private static final Pattern pattern = Pattern.compile("1\\.(\\d+)");

    private static long lastUpdateTimestampForTargetProtocolBelowOneNineteen;

    private static boolean stateFlag;

    private ProtocolVersionUtil() {
    }

    public static boolean isTargetProtocolBelowOneNineteen() {
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis < lastUpdateTimestampForTargetProtocolBelowOneNineteen) {
            return stateFlag;
        } else {
            stateFlag = detectLegacyTargetProtocol();
            lastUpdateTimestampForTargetProtocolBelowOneNineteen = currentTimeMillis + 1500L;
            return stateFlag;
        }
    }

    private static boolean detectLegacyTargetProtocol() {
        try {
            var type = Class.forName("com.viaversion.viafabricplus.ViaFabricPlus");
            Object value = type.getMethod("getImpl").invoke(null);
            if (value == null) {
                return false;
            } else {
                Object valueValue = invokePublicNoArgMethod(value, "getTargetVersion");
                if (valueValue == null) {
                    return false;
                } else {
                    Integer valueResult = readProtocolVersionId(valueValue);
                    return valueResult != null && valueResult < 759;
                }
            }
        } catch (Throwable failure) {
            return false;
        }
    }

    private static Object invokePublicNoArgMethod(Object value, String text) {
        try {
            Method method = value.getClass().getMethod(text);
            return Modifier.isPublic(method.getModifiers()) && method.getParameterCount() == 0 ? method.invoke(value) : null;
        } catch (Throwable failure) {
            return null;
        }
    }

    private static Integer readProtocolVersionId(Object value) {
        try {
            Method method = value.getClass().getMethod("getVersion");
            if (method.invoke(value) instanceof Number number) {
                return number.intValue();
            }
        } catch (Throwable failure) {
        }
        try {
            for (Method methodValue : value.getClass().getMethods()) {
                if (Modifier.isPublic(methodValue.getModifiers()) && methodValue.getParameterCount() == 0) {
                    var returnType = methodValue.getReturnType();
                    if (returnType == int.class || returnType == Integer.class) {
                        String normalizedText = methodValue.getName().toLowerCase();
                        if ((normalizedText.contains("version") || normalizedText.contains("protocol") || normalizedText.contains("id")) && methodValue.invoke(value) instanceof Number numberValue) {
                            return numberValue.intValue();
                        }
                    }
                }
            }
        } catch (Throwable failureValue) {
        }
        Matcher matcher = pattern.matcher(String.valueOf(value));
        if (matcher.find()) {
            int intValue = Integer.parseInt(matcher.group(1));
            return intValue >= 19 ? 759 : 758;
        } else {
            return null;
        }
    }
}
