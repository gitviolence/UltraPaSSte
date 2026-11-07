package brova.toshiba.client.utils.discord;

import brova.toshiba.client.utils.discord.callback.DisconnectListener;
import brova.toshiba.client.utils.discord.callback.ErrorListener;
import brova.toshiba.client.utils.discord.callback.ReadyListener;
import brova.toshiba.client.utils.discord.nativeapi.DiscordEventHandlers;
import brova.toshiba.client.utils.discord.nativeapi.DiscordRichPresence;
import brova.toshiba.client.utils.discord.nativeapi.DiscordRpcLibrary;
import java.util.List;

public class DiscordRpcManager {

    private static boolean stateFlag;

    private static Thread thread;

    private static ReadyListener onReady;

    private static DisconnectListener onDisconnected;

    private static ErrorListener onErrored;

    public static void setOnReady(ReadyListener onReadyValue) {
        onReady = onReadyValue;
    }

    public static void setOnDisconnected(DisconnectListener onDisconnectedValue) {
        onDisconnected = onDisconnectedValue;
    }

    public static void setOnErrored(ErrorListener onErroredValue) {
        onErrored = onErroredValue;
    }

    public static void startup(String text, boolean flag) {
        stateFlag = true;
        DiscordEventHandlers discordEventHandlers = new DiscordEventHandlers();
        discordEventHandlers.ready = input -> {
            if (onReady != null) {
                onReady.onReady(new DiscordUser(input.userId, input.username, input.discriminator));
            }
        };
        discordEventHandlers.disconnected = (inputValue, inputResult) -> {
            if (onDisconnected != null) {
                onDisconnected.onDisconnected(new DiscordError(inputValue, inputResult));
            }
        };
        discordEventHandlers.errored = (inputItem, inputEntry) -> {
            if (onErrored != null) {
                onErrored.onErrored(new DiscordError(inputItem, inputEntry));
            }
        };
        DiscordRpcLibrary.INSTANCE.Discord_Initialize(text, discordEventHandlers, flag, "");
        thread = new Thread(() -> {
            while (stateFlag) {
                try {
                    DiscordRpcLibrary.INSTANCE.Discord_RunCallbacks();
                    Thread.sleep(2000L);
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception failureValue) {
                    System.err.println("[DiscordRpc] " + failureValue.getMessage());
                }
            }
        }, "Discord-RPC-Callback-Handler");
        thread.setDaemon(true);
        thread.start();
    }

    public static void updatePresence(RichPresence richPresence) {
        if (stateFlag && richPresence != null) {
            DiscordRichPresence discordRichPresence = new DiscordRichPresence();
            String text = brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.state);
            brova.toshiba.client.utils.runtime.AccessGuard.write(discordRichPresence, text, (accessReceiver, accessValue) -> {
                accessReceiver.state = accessValue;
            });
            text = richPresence.details;
            brova.toshiba.client.utils.runtime.AccessGuard.write(discordRichPresence, text, (accessReceiver, accessValue) -> {
                accessReceiver.details = accessValue;
            });
            discordRichPresence.startTimestamp = richPresence.startTimestamp == 0L ? System.currentTimeMillis() / 1000L : richPresence.startTimestamp;
            discordRichPresence.endTimestamp = richPresence.endTimestamp;
            discordRichPresence.largeImageKey = richPresence.largeImageKey;
            discordRichPresence.largeImageText = brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.largeImageText);
            text = brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.smallImageKey);
            brova.toshiba.client.utils.runtime.AccessGuard.write(discordRichPresence, text, (accessReceiver, accessValue) -> {
                accessReceiver.smallImageKey = accessValue;
            });
            text = brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.smallImageText);
            brova.toshiba.client.utils.runtime.AccessGuard.write(discordRichPresence, text, (accessReceiver, accessValue) -> {
                accessReceiver.smallImageText = accessValue;
            });
            if (richPresence.buttons != null && !brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.buttons).isEmpty()) {
                if (brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.buttons).size() >= 1) {
                    discordRichPresence.button_label_1 = richPresence.buttons.get(0).getLabel();
                    discordRichPresence.button_url_1 = brova.toshiba.client.utils.runtime.AccessGuard.invoke(richPresence.buttons.get(0), new Object[] {}, (accessReceiver, accessArguments) -> ((PresenceButton) accessReceiver).getUrl());
                }
                if (brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.buttons).size() >= 2) {
                    discordRichPresence.button_label_2 = richPresence.buttons.get(1).getLabel();
                    text = brova.toshiba.client.utils.runtime.AccessGuard.read(richPresence, accessReceiver -> accessReceiver.buttons).get(1).getUrl();
                    brova.toshiba.client.utils.runtime.AccessGuard.write(discordRichPresence, text, (accessReceiver, accessValue) -> {
                        accessReceiver.button_url_2 = accessValue;
                    });
                }
            }
            DiscordRpcLibrary.INSTANCE.Discord_UpdatePresence(discordRichPresence);
        }
    }

    public static void shutdown() {
        stateFlag = false;
        if (thread != null) {
            thread.interrupt();
        }
        DiscordRpcLibrary.INSTANCE.Discord_Shutdown();
    }
}
