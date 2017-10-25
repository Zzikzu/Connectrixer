package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{


        URL resource = getClass().getClassLoader().getResource("main.fxml");      //enhanced MVN J8 solution
        if (resource != null){
            Parent root = FXMLLoader.load(resource);
            primaryStage.setTitle("Connectrixer");
            primaryStage.setScene(new Scene(root, 450, 375));
            primaryStage.setResizable(false);

            primaryStage.show();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
