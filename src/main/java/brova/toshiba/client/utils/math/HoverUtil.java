package brova.toshiba.client.utils.math;

public class HoverUtil {

    public static boolean isHovering(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, int intValue, int intValueValue) {
        return intValue >= floatValue && intValueValue >= floatValueValue && intValue < floatValue + floatValueResult && intValueValue < floatValueValue + floatValueItem;
    }

    public static boolean isInRegion(int intValue, int intValueValue, int intValueResult, int intValueItem, int intValueEntry, int intValueCopy) {
        return intValue >= intValueResult && intValue <= intValueResult + intValueEntry && intValueValue >= intValueItem && intValueValue <= intValueItem + intValueCopy;
    }

    public static boolean isInRegion(double doubleValue, double doubleValueValue, float floatValue, float floatValueValue, float floatValueResult, float floatValueItem) {
        return doubleValue >= floatValue && doubleValue <= floatValue + floatValueResult && doubleValueValue >= floatValueValue && doubleValueValue <= floatValueValue + floatValueItem;
    }

    public static boolean isHovering(float floatValue, float floatValueValue, float floatValueResult, float floatValueItem, double doubleValue, double doubleValueValue) {
        return doubleValue >= floatValue && doubleValueValue >= floatValueValue && doubleValue < floatValue + floatValueResult && doubleValueValue < floatValueValue + floatValueItem;
    }

    public static boolean isInRegion(double doubleValue, double doubleValueValue, int intValue, int intValueValue, int intValueResult, int intValueItem) {
        return doubleValue >= intValue && doubleValue <= intValue + intValueResult && doubleValueValue >= intValueValue && doubleValueValue <= intValueValue + intValueItem;
    }

    public static boolean isHovered(double doubleValue, double doubleValueValue, double doubleValueResult, double doubleValueItem, double doubleValueEntry, double doubleValueCopy) {
        return doubleValue > doubleValueResult && doubleValue < doubleValueResult + doubleValueEntry && doubleValueValue > doubleValueItem && doubleValueValue < doubleValueItem + doubleValueCopy;
    }
}
