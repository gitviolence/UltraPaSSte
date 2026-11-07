package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Callback;

public interface JoinGameCallback extends Callback {

    void apply(String text);
}
