package sample;

public class TextDialogSettings {
    private String fileToRead;
    private boolean isEditable;
    private static TextDialogSettings instance;
    private double prefHeight;
    private double prefWidth;
    private double maxWidth;

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

    public double getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }

    public double getPrefHeight() {
        return prefHeight;
    }

    public void setPrefHeight(double prefHeight) {
        this.prefHeight = prefHeight;
    }

    public double getPrefWidth() {
        return prefWidth;
    }

    public void setPrefWidth(double prefWidth) {
        this.prefWidth = prefWidth;
    }


}
