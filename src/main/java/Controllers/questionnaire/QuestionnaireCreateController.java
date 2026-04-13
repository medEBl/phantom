package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import services.questionnaire.QuestionnaireService;

import java.io.IOException;

public class QuestionnaireCreateController {

    @FXML private TextField tfGame, tfQ1, tfQ2, tfQ3, tfQ4;
    @FXML private Button btnRetour, btnCreer;

    private QuestionnaireService service;

    @FXML
    public void initialize() {
        service = new QuestionnaireService();
        btnRetour.setOnAction(e -> navigateToList());
        btnCreer.setOnAction(e -> handleCreate());
    }

    private void handleCreate() {
        if (tfGame.getText().trim().isEmpty() || tfQ1.getText().trim().isEmpty() || tfQ2.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le jeu et les questions 1 et 2 sont obligatoires.");
            return;
        }

        try {
            Questionnaire q = new Questionnaire(
                    0, // ID is auto-generated
                    tfGame.getText().trim(),
                    tfQ1.getText().trim(),
                    tfQ2.getText().trim(),
                    tfQ3.getText().trim().isEmpty() ? null : tfQ3.getText().trim(),
                    tfQ4.getText().trim().isEmpty() ? null : tfQ4.getText().trim()
            );

            // Note: We pass null for idAgent because this is a base template!
            q.setIdAgent(null);

            service.createQuestionnaire(q);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le nouveau questionnaire a été créé !");
            navigateToList();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le questionnaire.");
        }
    }

    private void navigateToList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListQuestionnaires.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}