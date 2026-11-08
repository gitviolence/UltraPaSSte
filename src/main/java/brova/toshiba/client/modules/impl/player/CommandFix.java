package brova.toshiba.client.modules.impl.player;

import brova.toshiba.client.modules.Module;
import brova.toshiba.client.modules.Module.ModuleCategory;
import java.util.HashMap;
import java.util.Map;

public class CommandFix extends Module {

    public static CommandFix INSTANCE = new CommandFix();

    private static final Map<Character, Character> orDefaultCharacterByCharacter = new HashMap<>();

    public CommandFix() {
        super("CommandFix", "Исправляет команды, случайно набранные в русской раскладке", ModuleCategory.PLAYER);
    }

    public String fixCommandName(String text) {
        if (text != null && !text.isEmpty()) {
            int index = text.indexOf(32);
            String textValue = index < 0 ? text : text.substring(0, index);
            String textResult = index < 0 ? "" : text.substring(index);
            return this.isCharAt(textValue) ? this.createTextFromCharAtWithAppend(textValue) + textResult : text;
        } else {
            return text;
        }
    }

    private boolean isCharAt(String text) {
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character >= 1072 && character <= 1103 || character >= 1040 && character <= 1071 || character == 1105 || character == 1025) {
                return true;
            }
        }
        return false;
    }

    private String createTextFromCharAtWithAppend(String text) {
        StringBuilder textBuilder = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            textBuilder.append(orDefaultCharacterByCharacter.getOrDefault(character, character));
        }
        return textBuilder.toString();
    }

    static {
        String text = "йцукенгшщзхъфывапролджэячсмитьбюё.";
        String textValue = "qwertyuiop[]asdfghjkl;'zxcvbnm,.`/";
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            char characterValue = textValue.charAt(index);
            orDefaultCharacterByCharacter.put(character, characterValue);
            orDefaultCharacterByCharacter.put(Character.toUpperCase(character), Character.toUpperCase(characterValue));
        }
    }
}
