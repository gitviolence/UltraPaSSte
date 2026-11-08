package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Callback;

public interface ReadyCallback extends Callback {

    void apply(DiscordUserStructure discordUserStructure);
}
