package sample;

import core.Connectrix;
import io.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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


        Thread thread = new Thread(() -> {
            buttonsInactive(true);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Running initial check");

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            UserProperties.getInstance().initialize();
            Connectrix.getInstance().readHostList();

            System.out.println("Check done, please continue");

            buttonsInactive(false);
        });
        thread.start();
    }

    @FXML
    public void onRunButtonClicked() {
        Thread thread = new Thread(() -> {
            buttonsInactive(true);
            if (ExcelWorkbook.getInstance().isWorkbookLoaded() && UserProperties.getInstance().credentialsSet()){
                Connectrix.getInstance().start();
                buttonsInactive(false);
            }

            if (!ExcelWorkbook.getInstance().isWorkbookLoaded()){
                System.out.println();
                System.out.println("No Workbook loaded!");
                System.out.println("Please load it");
            }

            if (!UserProperties.getInstance().credentialsSet()){
                System.out.println();
                System.out.println("Login credentials not set.");
                System.out.println("Please run: Edit => User settings");
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
        File workDir = UserProperties.getInstance().getWorkDir();
        if (workDir != null){
            fileChooser.setInitialDirectory(workDir);
        }
        fileChooser.getExtensionFilters().add(xlsxFiles);
        fileChooser.getExtensionFilters().add(allFiles);
        File selectedFile = fileChooser.showOpenDialog(null);

        Thread thread = new Thread(() -> {

            if (selectedFile != null) {
                UserProperties.getInstance().setWorkDir(selectedFile.getParentFile());

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
            fileChooser.setInitialDirectory(UserProperties.getInstance().getWorkDir());
            fileChooser.getExtensionFilters().add(xlsxFiles);
            fileChooser.getExtensionFilters().add(allFiles);
            File selectedFile = fileChooser.showSaveDialog(null);

            Thread thread = new Thread(() -> {

                if (selectedFile != null) {
                    UserProperties.getInstance().setWorkDir(selectedFile.getParentFile());

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
        showTextDialog("hostlist.txt", "Host List", true, true);
    }


    @FXML
    public void onEditUserSettings(){
        String title = "User settings";

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("user_dialog.fxml"));

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            ErrorMessage.getInstance().customMeassage("Couldn't load the dialog window");
            e.printStackTrace();
            return;
        }

        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            UserDialogController controller = fxmlLoader.getController();

            UserProperties.getInstance().setSessionCount(controller.getSessionCount());
            UserProperties.getInstance().setTabCount(controller.getTabCount());
            UserProperties.getInstance().setCredentials(controller.getUserName(), controller.getPassword());

            System.out.println("User setting edited");

        }

    }

    @FXML
    public void onHelpReadMeClicked(){
        showTextDialog("readme.txt", "Read Me", false, false);
    }

    @FXML
    public void onHelpErrorLogClicked(){
        showTextDialog("error.log", "Error Log", false, false);

    }

    @FXML
    public void onHelpAboutClicked(){
        showTextDialog("about.txt", "About program", false, false);
    }

    private void showTextDialog(String fileName, String title, Boolean isEditable, Boolean hasCancelButton) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());

        TextDialogSettings.getInstance().setEditable(isEditable);
        TextDialogSettings.getInstance().setFileToRead(fileName);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getClassLoader().getResource("text_dialog.fxml"));


        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            ErrorMessage.getInstance().customMeassage("Couldn't load the dialog window");
            e.printStackTrace();
            return;
        }

        TextDialogController controller = fxmlLoader.getController();
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
