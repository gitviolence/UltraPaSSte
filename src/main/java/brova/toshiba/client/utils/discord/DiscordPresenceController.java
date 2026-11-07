package brova.toshiba.client.utils.discord;

import brova.toshiba.client.data.LoaderUserInfo;

public final class DiscordPresenceController {

    private DiscordPresenceController() {
    }

    public static void startup() {
        try {
            DiscordRpcManager.setOnReady(input -> {
                RichPresence richPresence = RichPresence.builder().details("UID: " + createTextFromUID()).state("Role: " + createTextFromRoleWithToUpperCase()).build();
                DiscordRpcManager.updatePresence(richPresence);
            });
            DiscordRpcManager.setOnDisconnected(inputValue -> System.err.println("[Discord] RPC Disconnected: " + inputValue));
            DiscordRpcManager.setOnErrored(inputResult -> System.err.println("[Discord] RPC Errored: " + inputResult));
            DiscordRpcManager.startup("1319608304913223691", false);
        } catch (Throwable failure) {
        }
    }

    public static void shutdown() {
        try {
            brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(null, new Object[] {}, (accessReceiver, accessArguments) -> {
                DiscordRpcManager.shutdown();
            });
        } catch (Throwable failure) {
        }
    }

    private static String createTextFromUID() {
        String uID = LoaderUserInfo.getUID();
        return uID != null && !uID.isEmpty() ? uID : "1";
    }

    private static String createTextFromRoleWithToUpperCase() {
        String role = LoaderUserInfo.getRole();
        return role != null && !role.isEmpty() ? role.toUpperCase() : "USER";
    }
}
