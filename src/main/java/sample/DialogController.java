package sample;

import io.FileReadWriter;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

public class DialogController {
    private String fileName = DialogSettings.getInstance().getFileToRead();
    private boolean isEditable = DialogSettings.getInstance().isEditable();

    @FXML
    private TextArea dialogTextArea;

    TextArea getDialogTextArea() {
        return dialogTextArea;
    }

    @FXML
    private DialogPane dialogPane;

    public void initialize(){
        double prefHeight = 375.0;
        double prefWidth = 375.0;

        if (fileName != null){
            dialogTextArea.setText(FileReadWriter.read(fileName));
            dialogTextArea.setEditable(isEditable);

            String text = dialogTextArea.getText();
            String[] textLines = text.split(System.getProperty("line.separator"));
            int longestLine = 0;
            for (String line : textLines){
                if (line.length() > longestLine){
                    longestLine = line.length();
                }
            }

            double textWidth = text.length() / longestLine * 11;
            if (textWidth > prefWidth){
                prefWidth = textWidth;
            }

            dialogTextArea.setPrefWidth(prefHeight);
            dialogTextArea.setPrefWidth(prefWidth);

            dialogPane.setPrefWidth(prefHeight);
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
