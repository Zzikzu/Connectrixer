package sample;

import io.FileReadWriter;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

public class TextDialogController {
    private String fileName = TextDialogSettings.getInstance().getFileToRead();
    private boolean isEditable = TextDialogSettings.getInstance().isEditable();
    private double prefHeight = TextDialogSettings.getInstance().getPrefHeight();
    private double prefWidth = TextDialogSettings.getInstance().getPrefWidth();
    private double maxWidth = TextDialogSettings.getInstance().getMaxWidth();

    @FXML
    private TextArea dialogTextArea;

    TextArea getDialogTextArea() {
        return dialogTextArea;
    }

    @FXML
    private DialogPane dialogPane;

    public void initialize(){
//        double prefHeight = 375.0;
//        double prefWidth = 375.0;


        if (fileName != null){
            dialogTextArea.setText(FileReadWriter.read(fileName));
            dialogTextArea.setEditable(isEditable);

            String text = dialogTextArea.getText();
            String[] textLines = text.split(System.getProperty("line.separator"));
            int longestLine = 1;
            for (String line : textLines){
                if (line.length() > longestLine){
                    longestLine = line.length();
                }
            }

            double textWidth = longestLine;
            if (textWidth > prefWidth){

                if (textWidth < maxWidth){
                    prefWidth = textWidth;
                }else {
                    prefWidth = maxWidth;
                }

            }

            dialogTextArea.setPrefHeight(prefHeight);
            dialogTextArea.setPrefWidth(prefWidth);

            dialogPane.setPrefHeight(prefHeight);
            dialogPane.setPrefWidth(prefWidth);
        }
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    void setEditable(boolean editable) {
        isEditable = editable;
    }
}
