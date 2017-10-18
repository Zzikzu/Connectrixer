package sample;

public class DialogSettings {
    private String fileToRead;
    private boolean isEditable;
    private static DialogSettings instance;

    private DialogSettings() {
    }

    public static DialogSettings getInstance() {
        if (instance == null) {
            instance = new DialogSettings();
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
