package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface DiscordRpcLibrary extends Library {

    DiscordRpcLibrary INSTANCE = Native.load("discord-rpc", DiscordRpcLibrary.class);

    void Discord_Initialize(String text, DiscordEventHandlers discordEventHandlers, boolean flag, String textValue);

    void Discord_UpdatePresence(DiscordRichPresence discordRichPresence);

    void Discord_Shutdown();

    void Discord_RunCallbacks();
}
