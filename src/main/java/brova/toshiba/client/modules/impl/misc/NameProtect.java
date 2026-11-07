package brova.toshiba.client.modules.impl.misc;

import brova.toshiba.Toshiba;
import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import brova.toshiba.client.modules.impl.misc.NameProtect.ReplacementCache;
import brova.toshiba.client.modules.settings.implement.TextSetting;
import brova.toshiba.client.settings.impl.BooleanSetting;
import brova.toshiba.client.utils.text.TextReplacementUtil;
import brova.toshiba.mixin.ChatScreenAccessor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class NameProtect extends Module {

    public static final NameProtect INSTANCE = new NameProtect();

    private final BooleanSetting hideFriends = new BooleanSetting("Скрывать френдов", true);

    private final TextSetting replacementName = new TextSetting("Никнейм", "toshiba", 32);

    private final Map<String, String> stringEntries = new ReplacementCache(512, 0.75F, true);

    private NameProtect() {
        super("NameProtect", "Скрывает никнейм вас или друзей", ModuleCategory.MISC);
        this.addSettings(this.hideFriends, this.replacementName);
    }

    public String patch(String text) {
        if (text == null) {
            return null;
        } else if (!this.isLocalState()) {
            return text;
        } else {
            String textValue = this.createTextFromSessionWithUsername(text);
            String textResult = this.stringEntries.get(textValue);
            if (textResult != null) {
                return textResult;
            } else {
                String textItem = brova.toshiba.client.utils.runtime.AccessGuard.invoke(this, new Object[] {}, (accessReceiver, accessArguments) -> ((NameProtect) accessReceiver).getReplacementName());
                String textEntry = this.createAppendWithToString(text, mc.getSession().getUsername(), textItem);
                if (this.hideFriends.isState() && Toshiba.INSTANCE != null && Toshiba.INSTANCE.friendStorage != null) {
                    for (String textCopy : Toshiba.INSTANCE.friendStorage.getFriends()) {
                        textEntry = this.createAppendWithToString(textEntry, textCopy, textItem);
                    }
                }
                this.stringEntries.put(textValue, textEntry);
                return textEntry;
            }
        }
    }

    public String patchIncomingText(String text) {
        return this.patch(text);
    }

    public Text patchText(Text text) {
        if (text == null) {
            return null;
        } else if (!this.isLocalState()) {
            return text;
        } else {
            String replacementName = this.getReplacementName();
            Text textValue = TextReplacementUtil.replace(text, mc.getSession().getUsername(), replacementName);
            if (this.hideFriends.isState() && Toshiba.INSTANCE != null && Toshiba.INSTANCE.friendStorage != null) {
                for (String textResult : Toshiba.INSTANCE.friendStorage.getFriends()) {
                    textValue = TextReplacementUtil.replace(textValue, textResult, replacementName);
                }
            }
            return textValue;
        }
    }

    public String getReplacementName() {
        String text = this.replacementName.get();
        return text != null && !text.isBlank() ? text : "toshiba";
    }

    private String createAppendWithToString(String text, String textValue, String textResult) {
        if (text != null && textValue != null && !textValue.isEmpty()) {
            int intValue = this.calculateRegionMatches(text, textValue, 0);
            if (intValue < 0) {
                return text;
            } else {
                StringBuilder textBuilder = new StringBuilder(text.length() + textResult.length());
                int intValueValue = 0;
                for (int index = intValue; index >= 0; index = this.calculateRegionMatches(text, textValue, intValueValue)) {
                    textBuilder.append(text, intValueValue, index).append(textResult);
                    intValueValue = index + textValue.length();
                }
                textBuilder.append(text, intValueValue, text.length());
                return textBuilder.toString();
            }
        } else {
            return text;
        }
    }

    private int calculateRegionMatches(String text, String textValue, int intValue) {
        int intValueValue = text.length() - textValue.length();
        for (int index = Math.max(0, intValue); index <= intValueValue; index++) {
            if (text.regionMatches(true, index, textValue, 0, textValue.length())) {
                return index;
            }
        }
        return -1;
    }

    private String createTextFromSessionWithUsername(String text) {
        String textValue = mc != null && mc.getSession() != null ? mc.getSession().getUsername() : "";
        int intValue = 0;
        if (this.hideFriends.isState() && Toshiba.INSTANCE != null && Toshiba.INSTANCE.friendStorage != null) {
            var friends = Toshiba.INSTANCE.friendStorage.getFriends();
            intValue = friends.hashCode();
        }
        return textValue + this.getReplacementName() + this.hideFriends.isState() + intValue + text;
    }

    private boolean isLocalState() {
        return this.isEnable() && mc != null && mc.player != null && mc.world != null && !this.isToLowerCaseWithToshiba$getChatField();
    }

    private boolean isToLowerCaseWithToshiba$getChatField() {
        if (!(mc.currentScreen instanceof ChatScreen chatScreen)) {
            return false;
        } else {
            TextFieldWidget textFieldWidget = ((ChatScreenAccessor) chatScreen).toshiba$getChatField();
            if (textFieldWidget == null) {
                return false;
            } else {
                String text = textFieldWidget.getText();
                if (text == null) {
                    return false;
                } else {
                    String normalizedText = text.trim().toLowerCase();
                    String textValue = Toshiba.INSTANCE != null && Toshiba.INSTANCE.commandStorage != null ? Toshiba.INSTANCE.commandStorage.getPrefix().toLowerCase() : ".";
                    return normalizedText.startsWith(textValue + "friend remove");
                }
            }
        }
    }

    public static class ReplacementCache extends LinkedHashMap<String, String> {

        private static final long serialVersionUID = -6562838508134886872L;

        public ReplacementCache(int intValue, float floatValue, boolean flag) {
            super(intValue, floatValue, flag);
        }

        @Override
        public boolean removeEldestEntry(Entry<String, String> entry) {
            return this.size() > 512;
        }
    }
}
