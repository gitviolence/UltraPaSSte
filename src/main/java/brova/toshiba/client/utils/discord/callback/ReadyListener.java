package brova.toshiba.client.utils.discord.callback;

import brova.toshiba.client.utils.discord.DiscordUser;

public interface ReadyListener {

    void onReady(DiscordUser discordUser);
}
