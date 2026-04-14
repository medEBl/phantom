package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import services.questionnaire.QuestionnaireService;
import tools.Phantom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QuestionnaireCreateController {

    @FXML private TextField tfGame, tfQ1, tfQ2, tfQ3, tfQ4;
    @FXML private Button btnRetour, btnCreer;

    private QuestionnaireService service;

    @FXML
    public void initialize() {
        service = new QuestionnaireService();

        // --- REAL-TIME VALIDATION ---
        setupRealTimeValidation();

        btnRetour.setOnAction(e -> navigateToList());
        btnCreer.setOnAction(e -> handleCreate());
    }

    private void setupRealTimeValidation() {
        // Validation Jeu (Not Blank)
        if (tfGame != null) {
            tfGame.textProperty().addListener((obs, oldVal, newVal) -> {
                applyValidationStyle(tfGame, !newVal.trim().isEmpty());
            });
        }

        // Validation Q1 & Q2 (Not Blank + Ends with '?')
        if (tfQ1 != null) {
            tfQ1.textProperty().addListener((obs, oldVal, newVal) -> {
                String text = newVal.trim();
                applyValidationStyle(tfQ1, !text.isEmpty() && text.endsWith("?"));
            });
        }
        if (tfQ2 != null) {
            tfQ2.textProperty().addListener((obs, oldVal, newVal) -> {
                String text = newVal.trim();
                applyValidationStyle(tfQ2, !text.isEmpty() && text.endsWith("?"));
            });
        }

        // Validation Q3 & Q4 (Nullable, BUT if filled, must end with '?')
        if (tfQ3 != null) {
            tfQ3.textProperty().addListener((obs, oldVal, newVal) -> {
                String text = newVal.trim();
                applyValidationStyle(tfQ3, text.isEmpty() || text.endsWith("?"));
            });
        }
        if (tfQ4 != null) {
            tfQ4.textProperty().addListener((obs, oldVal, newVal) -> {
                String text = newVal.trim();
                applyValidationStyle(tfQ4, text.isEmpty() || text.endsWith("?"));
            });
        }
    }

    private void applyValidationStyle(TextField field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #2a2a35; -fx-border-radius: 8; -fx-background-radius: 8;");
        } else {
            field.setStyle("-fx-border-color: #ff3b3f; -fx-border-radius: 8; -fx-background-radius: 8;");
        }
    }

    // NOUVEAU : Vérifie si un template existe déjà pour ce jeu (@UniqueEntity)
    private boolean questionnaireExistsForGame(String game) {
        String sql = "SELECT 1 FROM questionnaire_agent WHERE game = ?";
        // Remove Connection from the try() block!
        Connection cnx = Phantom.getInstance().getCnx();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, game);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // True si trouvé
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true; // Bloque par sécurité en cas d'erreur
        }
    }

    private void handleCreate() {
        String game = tfGame.getText().trim();
        String q1 = tfQ1.getText().trim();
        String q2 = tfQ2.getText().trim();
        String q3 = tfQ3.getText().trim();
        String q4 = tfQ4.getText().trim();

        // 1. Validation Jeu
        if (game.isEmpty()) {
            applyValidationStyle(tfGame, false);
            showAlert(Alert.AlertType.WARNING, "Jeu manquant", "Le nom du jeu est obligatoire.");
            return;
        }

        // --- Vérification @UniqueEntity(fields: ['game']) ---
        if (questionnaireExistsForGame(game)) {
            applyValidationStyle(tfGame, false);
            showAlert(Alert.AlertType.ERROR, "Doublon détecté", "Un questionnaire existe déjà pour le jeu : " + game);
            return;
        }

        // 2. Validation Q1
        if (q1.isEmpty() || !q1.endsWith("?")) {
            applyValidationStyle(tfQ1, false);
            showAlert(Alert.AlertType.WARNING, "Question 1 invalide", "La question 1 est obligatoire et doit se terminer par un '?'.");
            return;
        }

        // 3. Validation Q2
        if (q2.isEmpty() || !q2.endsWith("?")) {
            applyValidationStyle(tfQ2, false);
            showAlert(Alert.AlertType.WARNING, "Question 2 invalide", "La question 2 est obligatoire et doit se terminer par un '?'.");
            return;
        }

        // 4. Validation Q3 (Optionnelle)
        if (!q3.isEmpty() && !q3.endsWith("?")) {
            applyValidationStyle(tfQ3, false);
            showAlert(Alert.AlertType.WARNING, "Question 3 invalide", "Si vous remplissez la question 3, elle doit se terminer par un '?'.");
            return;
        }

        // 5. Validation Q4 (Optionnelle)
        if (!q4.isEmpty() && !q4.endsWith("?")) {
            applyValidationStyle(tfQ4, false);
            showAlert(Alert.AlertType.WARNING, "Question 4 invalide", "Si vous remplissez la question 4, elle doit se terminer par un '?'.");
            return;
        }

        // Tout est valide, on sauvegarde
        try {
            Questionnaire q = new Questionnaire(
                    0,
                    game,
                    q1,
                    q2,
                    q3.isEmpty() ? null : q3,
                    q4.isEmpty() ? null : q4
            );
            q.setIdAgent(null); // Template de base

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