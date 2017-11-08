package sample;

import core.Connectrix;
import io.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Optional;

public class MainController {

    @FXML
    private Button runButton;

    @FXML
    private TextArea textArea;

    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem fileOpen;

    @FXML
    private MenuItem fileSave;

    @FXML
    private MenuItem fileSaveAs;

    @FXML
    private MenuItem fileExit;

    @FXML
    private GridPane mainWindow;


    public void initialize(){
        redirectOutputStream();
        fileOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        fileSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        fileSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
        fileExit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));

        System.out.println("Hello!");
        Connectrix.getInstance().readHostList();
    }

    @FXML
    public void onRunButtonClicked() {
        Thread thread = new Thread(() -> {
            buttonsInactive(true);
            if (ExcelWorkbook.getInstance().isWorkbookLoaded()){
                Connectrix.getInstance().start();
                buttonsInactive(false);
            }else {
                System.out.println();
                System.out.println("No Workbook loaded!");
                System.out.println("Please load it");
            }
            buttonsInactive(false);

        });
        thread.start();
    }

    @FXML
    public void onFileOpenClicked() {

        buttonsInactive(true);
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter xlsxFiles = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
        FileChooser.ExtensionFilter allFiles = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
        fileChooser.setTitle("Open Resource File");
        fileChooser.setInitialDirectory(UserProperty.getInstance().getWorkDir());
        fileChooser.getExtensionFilters().add(xlsxFiles);
        fileChooser.getExtensionFilters().add(allFiles);
        File selectedFile = fileChooser.showOpenDialog(null);

        Thread thread = new Thread(() -> {

            if (selectedFile != null) {
                UserProperty.getInstance().setWorkDir(selectedFile.getParentFile());

                System.out.println();
                System.out.println("File selected: " + selectedFile.getName());
                System.out.println(selectedFile.getPath());
                ExcelWorkbook.getInstance().setFilePath(selectedFile.getPath());
                ExcelWorkbook.getInstance().loadWorkbook();

            } else {
                System.out.println();
                System.out.println("File selection cancelled.");
            }
            buttonsInactive(false);
        });
        thread.start();
    }

    @FXML
    public void onFileSaveClicked(){
        Thread thread = new Thread(() -> {
            buttonsInactive(true);
            if (ExcelWorkbook.getInstance().isWorkbookLoaded()){
                ExcelWorkbook.getInstance().saveWorkbook();
                buttonsInactive(false);
            }else {
                System.out.println();
                System.out.println("No Workbook loaded!");
                System.out.println("Please load it");
            }
            buttonsInactive(false);

        });
        thread.start();
    }

    @FXML
    public void onFileSaveAsClicked() {
        if (ExcelWorkbook.getInstance().isWorkbookLoaded()) {
            buttonsInactive(true);
            FileChooser.ExtensionFilter xlsxFiles = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
            FileChooser.ExtensionFilter allFiles = new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File");
            fileChooser.setInitialDirectory(UserProperty.getInstance().getWorkDir());
            fileChooser.getExtensionFilters().add(xlsxFiles);
            fileChooser.getExtensionFilters().add(allFiles);
            File selectedFile = fileChooser.showSaveDialog(null);

            Thread thread = new Thread(() -> {

                if (selectedFile != null) {
                    UserProperty.getInstance().setWorkDir(selectedFile.getParentFile());

                    System.out.println();
                    System.out.println("File selected: " + selectedFile.getName());
                    System.out.println(selectedFile.getPath());

                    if (selectedFile.getName().contains(".")){
                        ExcelWorkbook.getInstance().setFilePath(selectedFile.getPath());
                        ExcelWorkbook.getInstance().saveWorkbook();

                    }else {
                        File newFile = new File(selectedFile.getAbsolutePath() + ".xlsx");
                        ExcelWorkbook.getInstance().setFilePath(newFile.getPath());
                        ExcelWorkbook.getInstance().saveWorkbook();
                    }

                } else {
                    System.out.println();
                    System.out.println("File saving cancelled.");
                }
                buttonsInactive(false);
            });
            thread.start();
        } else {
            Thread thread = new Thread(() -> {
                System.out.println();
                System.out.println("No Workbook loaded!");
                System.out.println("Please load it");
            });
            thread.start();
        }
    }

    @FXML
    public void onFileExitClicked(){
        Platform.exit();
    }

    @FXML
    public void onEditHostListClicked(){
        showDialog("hostlist.txt", "Host List", true, true);
    }

    @FXML
    public void onHelpReadMeClicked(){
        showDialog("readme.txt", "Read Me", false, false);
    }

    @FXML
    public void onHelpErrorLogClicked(){
        showDialog("error.log", "Error Log", false, false);

    }

    @FXML
    public void onHelpAboutClicked(){
        showDialog("about.txt", "About program", false, false);
    }

    private void showDialog(String fileName, String title, Boolean isEditable, Boolean hasCancelButton) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());

        DialogSettings.getInstance().setEditable(isEditable);
        DialogSettings.getInstance().setFileToRead(fileName);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("dialog.fxml"));


        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            ErrorMessage.getInstance().customMeassage("Couldn't load the dialog window");
            e.printStackTrace();
            return;
        }

        DialogController controller = fxmlLoader.getController();
        controller.setFileName(fileName);
        controller.setEditable(isEditable);


        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        if (hasCancelButton) {
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        }

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (fileName.equals("hostlist.txt")) {

                String text = controller.getDialogTextArea().getText();
                FileReadWriter.write(text, fileName, false);
                Connectrix.getInstance().readHostList();
                System.out.println("Host list updated");

            }

        }
    }

    private void buttonsInactive(boolean active){
        runButton.setDisable(active);
        menuBar.setDisable(active);
    }

    private void redirectOutputStream(){
        Console console = new Console(textArea);
        System.setOut(console.getPrintStream());

        PrintStream errorStream = new PrintStream(new ErrorOutputStream(ErrorLog.getInstance()));
//        System.setErr(errorStream);
    }

}
