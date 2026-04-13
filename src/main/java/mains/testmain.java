package mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class testmain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Comment out the old ListAgents line
        // Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/ListAgents.fxml")));

        // Load the Admin Dashboard instead!
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/ListQuestionnaires.fxml")));

        primaryStage.setTitle("Phantom Admin - Dashboard");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}