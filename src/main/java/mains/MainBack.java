package mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class MainBack extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("⏳ 1. Démarrage et chargement du FXML (WebView & Base de données)...");

        // Loads the Questionnaire list directly
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/ListQuestionnaires.fxml")));

        System.out.println("✅ 2. FXML et données chargés avec succès !");

        primaryStage.setTitle("Phantom Admin - Dashboard (Back)");
        primaryStage.setScene(new Scene(root, 1200, 800));

        System.out.println("🚀 3. Affichage de la fenêtre à l'écran...");
        primaryStage.show();

        System.out.println("🎯 4. Terminé ! L'application tourne normalement.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}