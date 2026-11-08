package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Callback;

public interface SpectateGameCallback extends Callback {

    void apply(String text);
}
