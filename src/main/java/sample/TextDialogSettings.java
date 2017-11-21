package sample;

public class TextDialogSettings {
    private String fileToRead;
    private boolean isEditable;
    private static TextDialogSettings instance;

    private TextDialogSettings() {
    }

    public static TextDialogSettings getInstance() {
        if (instance == null) {
            instance = new TextDialogSettings();
        }
        return instance;
    }

    String getFileToRead() {
        return fileToRead;
    }

    void setFileToRead(String fileToRead) {
        this.fileToRead = fileToRead;
    }

    boolean isEditable() {
        return isEditable;
    }

    void setEditable(boolean editable) {
        isEditable = editable;
    }
}
