package brova.toshiba.client.utils.discord.callback;

import brova.toshiba.client.utils.discord.DiscordError;

public interface ErrorListener {

    void onErrored(DiscordError discordError);
}
