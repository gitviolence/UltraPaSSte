package brova.toshiba.client.storages.implement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FriendStorage {

    private final List<String> friends = new ArrayList<>();

    public void add(String text) {
        String textValue = this.createTextFromToLowerCaseWithTrim(text);
        if (!textValue.isEmpty() && !this.isFriend(textValue)) {
            this.friends.add(text.trim());
        }
    }

    public void remove(String text) {
        String textValue = this.createTextFromToLowerCaseWithTrim(text);
        this.friends.removeIf(input -> this.createTextFromToLowerCaseWithTrim(input).equals(textValue));
    }

    public void clear() {
        this.friends.clear();
    }

    public boolean isFriend(String text) {
        String textValue = this.createTextFromToLowerCaseWithTrim(text);
        if (textValue.isEmpty()) {
            return false;
        } else {
            for (String textResult : this.friends) {
                if (this.createTextFromToLowerCaseWithTrim(textResult).equals(textValue)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isEmpty() {
        return this.friends.isEmpty();
    }

    private String createTextFromToLowerCaseWithTrim(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    public List<String> getFriends() {
        return this.friends;
    }
}
