package sample;

import io.FileReadWriter;
import javafx.fxml.FXML;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextArea;

public class DialogController {
    private String fileName = DialogSettings.getInstance().getFileToRead();
    private boolean isEditable = DialogSettings.getInstance().isEditable();

    @FXML
    private TextArea textArea;

    @FXML
    private DialogPane dialogPane;

    public void initialize(){
        double prefHeight = 375.0;
        double prefWidth = 375.0;

        if (fileName != null){
            textArea.setText(FileReadWriter.read(fileName));
            textArea.setEditable(isEditable);

            String text = textArea.getText();
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

            textArea.setPrefWidth(prefHeight);
            textArea.setPrefWidth(prefWidth);

            dialogPane.setPrefWidth(prefHeight);
            dialogPane.setPrefWidth(prefWidth);
        }
    }
}
