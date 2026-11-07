package brova.toshiba.client.data;

import net.minecraft.client.MinecraftClient;

public class LoaderUserInfo {

    private static String cachedText = System.getProperty("toshiba.loader.username");

    private static String uID = System.getProperty("toshiba.loader.uid");

    private static String role = System.getProperty("toshiba.loader.role");

    private static String subTill = System.getProperty("toshiba.loader.sub_till");

    public static void init(String text, String textValue, String textResult, String textItem) {
        cachedText = text;
        uID = textValue;
        role = textResult;
        subTill = textItem;
    }

    public static String getUserName() {
        if (cachedText != null && !cachedText.isEmpty()) {
            return cachedText;
        } else {
            try {
                MinecraftClient instance = MinecraftClient.getInstance();
                if (instance != null && instance.getSession() != null) {
                    String username = instance.getSession().getUsername();
                    if (username != null && !username.isEmpty()) {
                        return username;
                    }
                }
            } catch (Throwable failure) {
            }
            String property = System.getProperty("user.name");
            return property != null && !property.isEmpty() ? property : "SystemGuard";
        }
    }

    public static String getUID() {
        return uID != null && !uID.isEmpty() ? uID : "-1";
    }

    public static String getRole() {
        return role != null && !role.isEmpty() ? role : "Делаю обнову";
    }

    public static String getSubTill() {
        return subTill != null && !subTill.isEmpty() ? subTill : "Lifetime";
    }
}
