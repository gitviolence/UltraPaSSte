package brova.toshiba.client.modules.settings.implement;

import brova.toshiba.client.settings.SettingBase;
import java.util.function.Supplier;

public final class TextSetting extends SettingBase {

    private String text;

    private final int maxLength;

    public TextSetting(String text, String textValue) {
        this(text, textValue, 32);
    }

    public TextSetting(String text, String textValue, int intValue) {
        super(text);
        this.maxLength = Math.max(1, intValue);
        this.setText(textValue);
    }

    public void setText(String text) {
        if (text == null) {
            String textValue = "";
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, textValue, (accessReceiver, accessValue) -> {
                accessReceiver.text = accessValue;
            });
        } else {
            StringBuilder textBuilder = new StringBuilder();
            for (int index = 0; index < text.length() && textBuilder.length() < this.maxLength; index++) {
                char character = text.charAt(index);
                if (!Character.isISOControl(character)) {
                    textBuilder.append(character);
                }
            }
            String textResult = textBuilder.toString();
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, textResult, (accessReceiver, accessValue) -> {
                accessReceiver.text = accessValue;
            });
        }
    }

    public String get() {
        return this.text;
    }

    public TextSetting visible(Supplier<Boolean> visible) {
        this.visible = visible;
        return this;
    }

    public String getText() {
        return this.text;
    }

    public int getMaxLength() {
        return this.maxLength;
    }
}
