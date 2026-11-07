package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Callback;

public interface DisconnectCallback extends Callback {

    void apply(int intValue, String text);
}
