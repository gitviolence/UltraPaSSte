package brova.toshiba.client.utils.discord;

import java.util.ArrayList;
import java.util.List;

public class RichPresence {

    public String state;

    public String details;

    public long startTimestamp;

    public long endTimestamp;

    public String largeImageKey;

    public String largeImageText;

    public String smallImageKey;

    public String smallImageText;

    public List<PresenceButton> buttons = new ArrayList<>();

    public static RichPresenceBuilder builder() {
        return new RichPresenceBuilder();
    }
}
