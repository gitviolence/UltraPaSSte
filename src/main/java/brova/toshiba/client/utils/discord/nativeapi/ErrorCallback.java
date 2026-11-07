package brova.toshiba.client.utils.discord.nativeapi;

import com.sun.jna.Callback;

public interface ErrorCallback extends Callback {

    void apply(int intValue, String text);
}
