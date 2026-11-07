package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Callback;

public interface JoinRequestCallback extends Callback {

    void apply(DiscordUserStructure discordUserStructure);
}
