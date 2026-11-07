package brova.toshiba.client.utils.discord;

public class DiscordError {

    public final int code;

    public final String message;

    public DiscordError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message + " (" + this.code + ")";
    }
}
