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
        // Loads the CREATE screen directly for testing
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/ListAgents.fxml")));

        primaryStage.setTitle("Phantom Force - Créer un Agent");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}