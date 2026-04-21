package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import tools.Phantom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class QuestionnaireEditController {

    @FXML private Label lblSubtitle;
    @FXML private TextField tfGame, tfQ1, tfQ2, tfQ3, tfQ4;
    @FXML private Button btnRetour, btnEnregistrer;

    private Questionnaire currentQ;

    @FXML
    public void initialize() {
        // --- REAL-TIME VALIDATION ---
        setupRealTimeValidation();

        btnRetour.setOnAction(e -> navigateToList());
        btnEnregistrer.setOnAction(e -> handleUpdate());
    }

    public void initData(Questionnaire q) {
        this.currentQ = q;
        lblSubtitle.setText("Édition du questionnaire #" + q.getId());

        tfGame.setText(q.getGame());
        tfQ1.setText(q.getQues1());
        tfQ2.setText(q.getQues2());
        tfQ3.setText(q.getQues3() != null ? q.getQues3() : "");
        tfQ4.setText(q.getQues4() != null ? q.getQues4() : "");
    }

    private void setupRealTimeValidation() {
        if (tfGame != null) {
            tfGame.textProperty().addListener((obs, oldVal, newVal) -> applyValidationStyle(tfGame, !newVal.trim().isEmpty()));
        }
        if (tfQ1 != null) {
            tfQ1.textProperty().addListener((obs, oldVal, newVal) -> applyValidationStyle(tfQ1, !newVal.trim().isEmpty() && newVal.trim().endsWith("?")));
        }
        if (tfQ2 != null) {
            tfQ2.textProperty().addListener((obs, oldVal, newVal) -> applyValidationStyle(tfQ2, !newVal.trim().isEmpty() && newVal.trim().endsWith("?")));
        }
        if (tfQ3 != null) {
            tfQ3.textProperty().addListener((obs, oldVal, newVal) -> applyValidationStyle(tfQ3, newVal.trim().isEmpty() || newVal.trim().endsWith("?")));
        }
        if (tfQ4 != null) {
            tfQ4.textProperty().addListener((obs, oldVal, newVal) -> applyValidationStyle(tfQ4, newVal.trim().isEmpty() || newVal.trim().endsWith("?")));
        }
    }

    private void applyValidationStyle(TextField field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #2a2a35; -fx-border-radius: 8; -fx-background-radius: 8;");
        } else {
            field.setStyle("-fx-border-color: #ff3b3f; -fx-border-radius: 8; -fx-background-radius: 8;");
        }
    }

    // Vérifie si le jeu existe DÉJÀ pour un AUTRE questionnaire
    private boolean gameExistsForOtherQuestionnaire(String game, int currentId) {
        String sql = "SELECT 1 FROM questionnaire_agent WHERE game = ? AND id != ?";
        Connection cnx = Phantom.getInstance().getCnx();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, game);
            ps.setInt(2, currentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }


    private void handleUpdate() {
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

        if (gameExistsForOtherQuestionnaire(game, currentQ.getId())) {
            applyValidationStyle(tfGame, false);
            showAlert(Alert.AlertType.ERROR, "Doublon détecté", "Un autre questionnaire utilise déjà le jeu : " + game);
            return;
        }

        // 2. Validation Q1 & Q2
        if (q1.isEmpty() || !q1.endsWith("?")) {
            applyValidationStyle(tfQ1, false);
            showAlert(Alert.AlertType.WARNING, "Question 1 invalide", "La question 1 est obligatoire et doit se terminer par un '?'.");
            return;
        }
        if (q2.isEmpty() || !q2.endsWith("?")) {
            applyValidationStyle(tfQ2, false);
            showAlert(Alert.AlertType.WARNING, "Question 2 invalide", "La question 2 est obligatoire et doit se terminer par un '?'.");
            return;
        }

        // 3. Validation Q3 & Q4
        if (!q3.isEmpty() && !q3.endsWith("?")) {
            applyValidationStyle(tfQ3, false);
            showAlert(Alert.AlertType.WARNING, "Question 3 invalide", "Si vous remplissez la question 3, elle doit se terminer par un '?'.");
            return;
        }
        if (!q4.isEmpty() && !q4.endsWith("?")) {
            applyValidationStyle(tfQ4, false);
            showAlert(Alert.AlertType.WARNING, "Question 4 invalide", "Si vous remplissez la question 4, elle doit se terminer par un '?'.");
            return;
        }

        // Mise à jour de la base de données
        String sql = "UPDATE questionnaire_agent SET game=?, ques1=?, ques2=?, ques3=?, ques4=? WHERE id=?";
        try (Connection cnx = Phantom.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, game);
            ps.setString(2, q1);
            ps.setString(3, q2);
            ps.setString(4, q3.isEmpty() ? null : q3);
            ps.setString(5, q4.isEmpty() ? null : q4);
            ps.setInt(6, currentQ.getId());

            ps.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le questionnaire a été mis à jour !");
            navigateToList();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le questionnaire.");
        }
    }

    private void navigateToList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/agent/fxml/ListQuestionnaires.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}