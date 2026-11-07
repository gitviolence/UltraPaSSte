package brova.toshiba.client.utils.discord;

import java.io.Serializable;

public class PresenceButton implements Serializable {

    private static final long serialVersionUID = 3006041478246223947L;

    private final String label;

    private final String url;

    private PresenceButton(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public static PresenceButton of(String text, String textValue) {
        String textResult = text.substring(0, Math.min(text.length(), 31));
        return new PresenceButton(textResult, textValue);
    }

    public String getLabel() {
        return this.label;
    }

    public String getUrl() {
        return this.url;
    }
}
