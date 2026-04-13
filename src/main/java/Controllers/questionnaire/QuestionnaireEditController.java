package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.questionnaire.QuestionnaireService;
import tools.Phantom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class QuestionnaireEditController {

    @FXML private Label lblSubtitle;
    @FXML private TextField tfGame, tfQ1, tfQ2, tfQ3, tfQ4;
    @FXML private Button btnRetour, btnEnregistrer;

    private Questionnaire currentQ;

    @FXML
    public void initialize() {
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

    private void handleUpdate() {
        if (tfQ1.getText().trim().isEmpty() || tfQ2.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Les questions 1 et 2 sont obligatoires.");
            return;
        }

        // We update directly here (or you can add updateQuestionnaire to your service!)
        String sql = "UPDATE questionnaire_agent SET ques1=?, ques2=?, ques3=?, ques4=? WHERE id=?";
        Connection cnx = Phantom.getInstance().getCnx();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setString(1, tfQ1.getText().trim());
            ps.setString(2, tfQ2.getText().trim());
            ps.setString(3, tfQ3.getText().trim().isEmpty() ? null : tfQ3.getText().trim());
            ps.setString(4, tfQ4.getText().trim().isEmpty() ? null : tfQ4.getText().trim());
            ps.setInt(5, currentQ.getId());

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
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListQuestionnaires.fxml"));
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