package Controllers.agent;

import entities.agent.Agent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import tools.Phantom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReponseCreateController {

    @FXML private Label lblSubtitle;
    @FXML private Button btnAnnuler, btnEnregistrer;

    @FXML private VBox boxQ1, boxQ2, boxQ3, boxQ4;
    @FXML private Label lblQ1, lblQ2, lblQ3, lblQ4;
    @FXML private TextArea taR1, taR2, taR3, taR4;

    private Agent currentAgent;
    private int currentQuestionnaireId = -1;
    private boolean isUpdate = false; // Tracks if we are creating or updating

    @FXML
    public void initialize() {
        btnAnnuler.setOnAction(e -> navigateToDetails());
        btnEnregistrer.setOnAction(e -> handleSave());
    }

    public void initData(Agent agent) {
        this.currentAgent = agent;
        lblSubtitle.setText(agent.getPseudo() + " • " + agent.getGame());

        loadQuestionsAndAnswers();
    }

    private void loadQuestionsAndAnswers() {
        Connection cnx = Phantom.getInstance().getCnx();

        // 1. Fetch Questions for this Game
        String sqlQuestions = "SELECT id, ques1, ques2, ques3, ques4 FROM questionnaire_agent WHERE game = ?";
        try (PreparedStatement psQ = cnx.prepareStatement(sqlQuestions)) {
            psQ.setString(1, currentAgent.getGame());
            ResultSet rsQ = psQ.executeQuery();

            if (rsQ.next()) {
                currentQuestionnaireId = rsQ.getInt("id");

                // Set Question Labels
                lblQ1.setText(rsQ.getString("ques1"));
                lblQ2.setText(rsQ.getString("ques2"));

                // Hide Q3/Q4 if they don't exist for this game
                if (rsQ.getString("ques3") == null || rsQ.getString("ques3").isEmpty()) {
                    boxQ3.setVisible(false); boxQ3.setManaged(false);
                } else {
                    lblQ3.setText(rsQ.getString("ques3"));
                }

                if (rsQ.getString("ques4") == null || rsQ.getString("ques4").isEmpty()) {
                    boxQ4.setVisible(false); boxQ4.setManaged(false);
                } else {
                    lblQ4.setText(rsQ.getString("ques4"));
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun questionnaire trouvé pour le jeu: " + currentAgent.getGame());
                btnEnregistrer.setDisable(true);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Fetch existing answers (if any) to pre-fill the form
        String sqlAnswers = "SELECT rep1, rep2, rep3, rep4 FROM reponse_questionnaire WHERE id_agent = ?";
        try (PreparedStatement psA = cnx.prepareStatement(sqlAnswers)) {
            psA.setInt(1, currentAgent.getId());
            ResultSet rsA = psA.executeQuery();

            if (rsA.next()) {
                isUpdate = true; // Answers exist, we will UPDATE instead of INSERT
                taR1.setText(rsA.getString("rep1"));
                taR2.setText(rsA.getString("rep2"));
                taR3.setText(rsA.getString("rep3"));
                taR4.setText(rsA.getString("rep4"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSave() {
        // Validation for required fields (assuming Q1 and Q2 are always mandatory)
        if (taR1.getText().trim().isEmpty() || taR2.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez répondre au moins aux deux premières questions.");
            return;
        }

        Connection cnx = Phantom.getInstance().getCnx();
        try {
            if (isUpdate) {
                // UPDATE existing response
                String sql = "UPDATE reponse_questionnaire SET rep1=?, rep2=?, rep3=?, rep4=? WHERE id_agent=?";
                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    ps.setString(1, taR1.getText().trim());
                    ps.setString(2, taR2.getText().trim());
                    ps.setString(3, taR3.getText() != null ? taR3.getText().trim() : null);
                    ps.setString(4, taR4.getText() != null ? taR4.getText().trim() : null);
                    ps.setInt(5, currentAgent.getId());
                    ps.executeUpdate();
                }
            } else {
                // INSERT new response
                String sql = "INSERT INTO reponse_questionnaire (id_agent, questionnaire_id, rep1, rep2, rep3, rep4) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    ps.setInt(1, currentAgent.getId());
                    ps.setInt(2, currentQuestionnaireId);
                    ps.setString(3, taR1.getText().trim());
                    ps.setString(4, taR2.getText().trim());
                    ps.setString(5, taR3.getText() != null ? taR3.getText().trim() : null);
                    ps.setString(6, taR4.getText() != null ? taR4.getText().trim() : null);
                    ps.executeUpdate();
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Les réponses ont été enregistrées !");
            navigateToDetails(); // Go back to profile to see the answers

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder les réponses.");
        }
    }

    private void navigateToDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/agent/fxml/AgentDetails.fxml"));
            Parent root = loader.load();

            // Pass the agent back to the details screen so it reloads correctly!
            AgentDetailsController detailsController = loader.getController();
            detailsController.initData(currentAgent);

            btnAnnuler.getScene().setRoot(root);
        } catch (Exception e) {
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