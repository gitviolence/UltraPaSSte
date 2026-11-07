package brova.toshiba.client.gui.click;

public final class TextEditor {

    private String selectedText = "";

    private int cursor;

    private int cachedCountForCalculateInteger;

    private boolean active;

    private boolean stateFlag;

    private String status = "";

    private boolean successful;

    public void initialize(String text) {
        if (!this.stateFlag) {
            this.setText(text);
            this.stateFlag = true;
        }
    }

    public String getText() {
        return this.selectedText;
    }

    public void setText(String text) {
        this.selectedText = this.createReplaceWithSubstring(text);
        this.cursor = this.selectedText.length();
        this.cachedCountForCalculateInteger = this.cursor;
    }

    public int getCursor() {
        return this.cursor;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setCursor(int intValue, boolean flag) {
        this.cursor = this.calculateInteger(intValue);
        if (!flag) {
            this.cachedCountForCalculateInteger = this.cursor;
        }
    }

    public void selectAll() {
        this.cachedCountForCalculateInteger = 0;
        this.cursor = this.selectedText.length();
    }

    public boolean hasSelection() {
        return this.cursor != this.cachedCountForCalculateInteger;
    }

    public String getSelectedText() {
        return this.hasSelection() ? this.selectedText.substring(this.getSelectionStart(), this.getSelectionEnd()) : "";
    }

    public void insert(String text) {
        String textValue = this.createReplaceWithSubstring(text);
        int selectionStart = this.getSelectionStart();
        int selectionEnd = this.getSelectionEnd();
        int intValue = 500000 - (this.selectedText.length() - (selectionEnd - selectionStart));
        if (intValue > 0) {
            if (textValue.length() > intValue) {
                textValue = textValue.substring(0, intValue);
            }
            String textResult = this.selectedText.substring(0, selectionStart) + textValue + this.selectedText.substring(selectionEnd);
            brova.toshiba.client.utils.runtime.AccessGuard.write(this, textResult, (accessReceiver, accessValue) -> {
                accessReceiver.selectedText = accessValue;
            });
            this.cursor = selectionStart + textValue.length();
            this.cachedCountForCalculateInteger = this.cursor;
        }
    }

    public void backspace() {
        if (this.hasSelection()) {
            this.insert("");
        } else if (this.cursor > 0) {
            this.selectedText = this.selectedText.substring(0, this.cursor - 1) + this.selectedText.substring(this.cursor);
            this.cursor--;
            this.cachedCountForCalculateInteger = this.cursor;
        }
    }

    public void delete() {
        if (this.hasSelection()) {
            this.insert("");
        } else if (this.cursor < this.selectedText.length()) {
            this.selectedText = this.selectedText.substring(0, this.cursor) + this.selectedText.substring(this.cursor + 1);
            this.cachedCountForCalculateInteger = this.cursor;
        }
    }

    public void moveCursor(int intValue, boolean flag) {
        this.setCursor(this.cursor + intValue, flag);
    }

    public void moveToLineBoundary(boolean flag, boolean flagValue) {
        int intValue;
        if (flag) {
            int index = this.selectedText.indexOf(10, this.cursor);
            intValue = index < 0 ? this.selectedText.length() : index;
        } else {
            int indexValue = this.selectedText.lastIndexOf(10, Math.max(0, this.cursor - 1));
            intValue = indexValue + 1;
        }
        brova.toshiba.client.utils.runtime.AccessGuard.invokeVoid(this, new Object[] { intValue, flagValue }, (accessReceiver, accessArguments) -> {
            ((TextEditor) accessReceiver).setCursor(((int) accessArguments[0]), ((boolean) accessArguments[1]));
        });
    }

    public void setStatus(String text, boolean successful) {
        this.status = text == null ? "" : text;
        this.successful = successful;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isSuccessful() {
        return this.successful;
    }

    private int getSelectionStart() {
        return Math.min(this.cursor, this.cachedCountForCalculateInteger);
    }

    private int getSelectionEnd() {
        return Math.max(this.cursor, this.cachedCountForCalculateInteger);
    }

    private int calculateInteger(int intValue) {
        return Math.max(0, Math.min(this.selectedText.length(), intValue));
    }

    private String createReplaceWithSubstring(String text) {
        if (text == null) {
            return "";
        } else {
            String textValue = text.replace("\r\n", "\n").replace('\r', '\n');
            return textValue.length() > 500000 ? textValue.substring(0, 500000) : textValue;
        }
    }
}
