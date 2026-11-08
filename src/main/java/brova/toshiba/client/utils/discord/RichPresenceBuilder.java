package brova.toshiba.client.utils.discord;

import java.util.List;

public class RichPresenceBuilder {

    private final RichPresence richPresence = new RichPresence();

    public RichPresenceBuilder details(String text) {
        if (text != null && !text.isEmpty()) {
            this.richPresence.details = text.substring(0, Math.min(text.length(), 128));
        }
        return this;
    }

    public RichPresenceBuilder state(String text) {
        if (text != null && !text.isEmpty()) {
            this.richPresence.state = text.substring(0, Math.min(text.length(), 128));
        }
        return this;
    }

    public RichPresenceBuilder largeImageKey(String largeImageKey) {
        this.richPresence.largeImageKey = largeImageKey;
        return this;
    }

    public RichPresenceBuilder largeImageText(String largeImageText) {
        this.richPresence.largeImageText = largeImageText;
        return this;
    }

    public RichPresenceBuilder smallImageKey(String smallImageKey) {
        this.richPresence.smallImageKey = smallImageKey;
        return this;
    }

    public RichPresenceBuilder smallImageText(String smallImageText) {
        this.richPresence.smallImageText = smallImageText;
        return this;
    }

    public RichPresenceBuilder button(PresenceButton presenceButton) {
        brova.toshiba.client.utils.runtime.AccessGuard.read(this.richPresence, accessReceiver -> accessReceiver.buttons).add(presenceButton);
        return this;
    }

    public RichPresence build() {
        return this.richPresence;
    }
}
