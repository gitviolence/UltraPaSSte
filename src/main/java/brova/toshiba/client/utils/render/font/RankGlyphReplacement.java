package brova.toshiba.client.utils.render.font;

import brova.toshiba.client.utils.render.ColorMathUtil;
import java.util.HashMap;
import java.util.Map;

public class RankGlyphReplacement {

    private static final Map<Integer, String> stringEntries = new HashMap<>();

    private static final Map<Integer, Integer> integerEntries = new HashMap<>();

    private static final int[] integerValues = new int[] { 42240, 42244, 42248, 42258, 42262, 42272, 42276, 42280, 42336, 42290, 42294, 42308, 42326, 42312, 42304, 42322, 42249, 42259, 42263, 42273, 42277, 42281, 42291, 42295, 42241, 42245, 42313, 4144, 4138, 4132, 4134, 4140, 4139, 4151, 4130, 4148, 4141, 4152, 4133, 4131, 4149, 4150, 4137, 4129, 4145, 4143, 4146, 4135, 4153, 4126 };

    public static String replaceCodePoint(int intValue) {
        return stringEntries.get(intValue);
    }

    public static int getGradientColorForReplacement(int intValue, int intValueValue, int intValueResult, float floatValue, int intValueItem) {
        if (isInteger(intValue)) {
            Integer value = integerEntries.get(intValue);
            if (value == null) {
                return calculateAlphaColor(intValueItem, floatValue);
            } else {
                int intValueEntry = ColorMathUtil.darken(value, 0.8F);
                float floatValueValue = intValueResult <= 1 ? 1.0F : (float) intValueValue / (intValueResult - 1);
                int intValueCopy = ColorMathUtil.interpolateColor(intValueEntry, value, floatValueValue);
                return calculateAlphaColor(intValueCopy, floatValue);
            }
        } else {
            return calculateAlphaColor(intValueItem, floatValue);
        }
    }

    private static boolean isInteger(int intValue) {
        for (int intValueValue : integerValues) {
            if (intValueValue == intValue) {
                return true;
            }
        }
        return false;
    }

    private static int calculateAlphaColor(int intValue, float floatValue) {
        int intValueValue = Math.max(0, Math.min(255, (int) (floatValue * 255.0F)));
        return ColorMathUtil.setAlphaColor(intValue, intValueValue);
    }

    static {
        stringEntries.put(9889, "");
        stringEntries.put(9733, "");
        stringEntries.put(42240, "PLAYER");
        stringEntries.put(42244, "HERO");
        stringEntries.put(42248, "TITAN");
        stringEntries.put(42258, "AVENGER");
        stringEntries.put(42262, "OVERLORD");
        stringEntries.put(42272, "MAGISTER");
        stringEntries.put(42276, "IMPERATOR");
        stringEntries.put(42280, "DRAGON");
        stringEntries.put(42336, "D.HELPER");
        stringEntries.put(42290, "BULL");
        stringEntries.put(42294, "TIGER");
        stringEntries.put(42308, "VAMPIRE");
        stringEntries.put(42326, "BUNNY");
        stringEntries.put(42312, "COBRA");
        stringEntries.put(42304, "HYDRA");
        stringEntries.put(42322, "RABBIT");
        stringEntries.put(42249, "HELPER");
        stringEntries.put(42259, "ML.MODER");
        stringEntries.put(42263, "MODER");
        stringEntries.put(42273, "MODER+");
        stringEntries.put(42277, "ST.MODER");
        stringEntries.put(42281, "GL.MODER");
        stringEntries.put(42291, "ML.ADMIN");
        stringEntries.put(42295, "ADMIN");
        stringEntries.put(42241, "MEDIA");
        stringEntries.put(42245, "YT");
        stringEntries.put(42305, "GOD");
        stringEntries.put(4144, "HERO");
        stringEntries.put(4138, "TITAN");
        stringEntries.put(4132, "PRINCE");
        stringEntries.put(4134, "PHOENIX");
        stringEntries.put(4140, "OVERLORD");
        stringEntries.put(4139, "GUARDIAN");
        stringEntries.put(4151, "KRATOS");
        stringEntries.put(4130, "PHANTOM");
        stringEntries.put(4148, "CUSTOM");
        stringEntries.put(4141, "WINTER");
        stringEntries.put(4152, "SAKURA");
        stringEntries.put(4133, "SUMMER");
        stringEntries.put(4131, "HALLOWEEN");
        stringEntries.put(4149, "TIKTOK");
        stringEntries.put(4150, "TIKTOK+");
        stringEntries.put(4137, "MEDIA");
        stringEntries.put(4129, "YOUTUBE");
        stringEntries.put(4145, "HELPER");
        stringEntries.put(4143, "ML.ADMIN");
        stringEntries.put(4146, "MODER");
        stringEntries.put(4135, "CURATOR");
        stringEntries.put(4153, "SPECTATOR");
        stringEntries.put(4126, "DEVELOPER");
        stringEntries.put(7424, "A");
        stringEntries.put(665, "B");
        stringEntries.put(7428, "C");
        stringEntries.put(7429, "D");
        stringEntries.put(7431, "E");
        stringEntries.put(42800, "F");
        stringEntries.put(610, "G");
        stringEntries.put(668, "H");
        stringEntries.put(618, "I");
        stringEntries.put(7434, "J");
        stringEntries.put(7435, "K");
        stringEntries.put(671, "L");
        stringEntries.put(7437, "M");
        stringEntries.put(628, "N");
        stringEntries.put(7439, "O");
        stringEntries.put(7448, "P");
        stringEntries.put(491, "Q");
        stringEntries.put(640, "R");
        stringEntries.put(7451, "T");
        stringEntries.put(7452, "U");
        stringEntries.put(42801, "S");
        stringEntries.put(7456, "V");
        stringEntries.put(7457, "W");
        stringEntries.put(7521, "X");
        stringEntries.put(655, "Y");
        stringEntries.put(7458, "Z");
        integerEntries.put(42240, ColorMathUtil.rgb(141, 143, 141));
        integerEntries.put(42244, ColorMathUtil.rgb(100, 113, 251));
        integerEntries.put(42248, ColorMathUtil.rgb(245, 220, 29));
        integerEntries.put(42258, ColorMathUtil.rgb(79, 201, 83));
        integerEntries.put(42262, ColorMathUtil.rgb(85, 255, 255));
        integerEntries.put(42272, ColorMathUtil.rgb(224, 138, 52));
        integerEntries.put(42276, ColorMathUtil.rgb(202, 60, 60));
        integerEntries.put(42280, ColorMathUtil.rgb(245, 51, 238));
        integerEntries.put(42336, ColorMathUtil.rgb(214, 200, 42));
        integerEntries.put(42290, ColorMathUtil.rgb(121, 81, 202));
        integerEntries.put(42294, ColorMathUtil.rgb(202, 130, 60));
        integerEntries.put(42308, ColorMathUtil.rgb(202, 60, 60));
        integerEntries.put(42326, ColorMathUtil.rgb(68, 65, 66));
        integerEntries.put(42312, ColorMathUtil.rgb(127, 214, 86));
        integerEntries.put(42304, ColorMathUtil.rgb(92, 120, 7));
        integerEntries.put(42322, ColorMathUtil.rgb(230, 232, 230));
        integerEntries.put(42249, ColorMathUtil.rgb(214, 200, 42));
        integerEntries.put(42259, ColorMathUtil.rgb(100, 113, 251));
        integerEntries.put(42263, ColorMathUtil.rgb(100, 113, 251));
        integerEntries.put(42273, ColorMathUtil.rgb(121, 81, 202));
        integerEntries.put(42277, ColorMathUtil.rgb(100, 113, 251));
        integerEntries.put(42281, ColorMathUtil.rgb(121, 81, 202));
        integerEntries.put(42291, ColorMathUtil.rgb(64, 151, 214));
        integerEntries.put(42295, ColorMathUtil.rgb(202, 60, 60));
        integerEntries.put(42241, ColorMathUtil.rgb(121, 81, 202));
        integerEntries.put(42245, ColorMathUtil.rgb(255, 255, 255));
        integerEntries.put(42305, ColorMathUtil.rgb(245, 198, 29));
        integerEntries.put(42313, ColorMathUtil.rgb(202, 130, 60));
        integerEntries.put(4144, ColorMathUtil.rgb(13, 176, 209));
        integerEntries.put(4138, ColorMathUtil.rgb(21, 232, 24));
        integerEntries.put(4132, ColorMathUtil.rgb(232, 169, 21));
        integerEntries.put(4134, ColorMathUtil.rgb(237, 215, 19));
        integerEntries.put(4140, ColorMathUtil.rgb(64, 163, 152));
        integerEntries.put(4139, ColorMathUtil.rgb(86, 196, 99));
        integerEntries.put(4151, ColorMathUtil.rgb(147, 46, 230));
        integerEntries.put(4130, ColorMathUtil.rgb(230, 46, 46));
        integerEntries.put(4148, ColorMathUtil.rgb(16, 35, 179));
        integerEntries.put(4141, ColorMathUtil.rgb(55, 154, 184));
        integerEntries.put(4152, ColorMathUtil.rgb(184, 39, 159));
        integerEntries.put(4133, ColorMathUtil.rgb(255, 182, 56));
        integerEntries.put(4131, ColorMathUtil.rgb(232, 60, 30));
        integerEntries.put(4149, ColorMathUtil.rgb(0, 0, 0));
        integerEntries.put(4150, ColorMathUtil.rgb(0, 0, 0));
        integerEntries.put(4137, ColorMathUtil.rgb(37, 232, 30));
        integerEntries.put(4129, ColorMathUtil.rgb(232, 30, 30));
        integerEntries.put(4145, ColorMathUtil.rgb(30, 134, 232));
        integerEntries.put(4143, ColorMathUtil.rgb(89, 167, 227));
        integerEntries.put(4146, ColorMathUtil.rgb(62, 137, 194));
        integerEntries.put(4135, ColorMathUtil.rgb(56, 235, 74));
        integerEntries.put(4153, ColorMathUtil.rgb(173, 184, 174));
        integerEntries.put(4126, ColorMathUtil.rgb(255, 0, 25));
    }
}
