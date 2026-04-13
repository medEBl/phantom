package Controllers.reponse;

import Controllers.agent.AgentDetailsController;
import entities.agent.Agent;
import entities.reponse.Reponse;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import services.reponse.ReponseService;

import java.util.Map;
import java.util.Optional;

public class ReponseCreateController {

    @FXML private Label lblSubtitle;
    @FXML private Button btnAnnuler, btnEnregistrer;

    @FXML private VBox boxQ1, boxQ2, boxQ3, boxQ4;
    @FXML private Label lblQ1, lblQ2, lblQ3, lblQ4;
    @FXML private TextArea taR1, taR2, taR3, taR4;

    private Agent currentAgent;
    private int currentQuestionnaireId = -1;
    private boolean isUpdate = false;

    private ReponseService reponseService;

    @FXML
    public void initialize() {
        reponseService = new ReponseService();
        btnAnnuler.setOnAction(e -> navigateToDetails());
        btnEnregistrer.setOnAction(e -> handleSave());
    }

    public void initData(Agent agent) {
        this.currentAgent = agent;
        lblSubtitle.setText(agent.getPseudo() + " • " + agent.getGame());

        loadQuestionsAndAnswers();
    }

    private void loadQuestionsAndAnswers() {
        // 1. Get Questions from your Service
        Map<String, Object> questions = reponseService.getQuestionnaireByGame(currentAgent.getGame());

        if (questions != null) {
            currentQuestionnaireId = (int) questions.get("id");

            lblQ1.setText((String) questions.get("ques1"));
            lblQ2.setText((String) questions.get("ques2"));

            if (questions.get("ques3") == null || ((String) questions.get("ques3")).isEmpty()) {
                boxQ3.setVisible(false); boxQ3.setManaged(false);
            } else {
                lblQ3.setText((String) questions.get("ques3"));
            }

            if (questions.get("ques4") == null || ((String) questions.get("ques4")).isEmpty()) {
                boxQ4.setVisible(false); boxQ4.setManaged(false);
            } else {
                lblQ4.setText((String) questions.get("ques4"));
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun questionnaire trouvé pour ce jeu.");
            btnEnregistrer.setDisable(true);
            return;
        }

        // 2. Check for existing answers using your Optional<Reponse>
        Optional<Reponse> existingReponse = reponseService.getReponseByAgentId(currentAgent.getId());

        if (existingReponse.isPresent()) {
            isUpdate = true;
            Reponse rep = existingReponse.get();
            taR1.setText(rep.getRep1());
            taR2.setText(rep.getRep2());
            taR3.setText(rep.getRep3() != null ? rep.getRep3() : "");
            taR4.setText(rep.getRep4() != null ? rep.getRep4() : "");
        }
    }

    private void handleSave() {
        if (taR1.getText().trim().isEmpty() || taR2.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez répondre au moins aux deux premières questions.");
            return;
        }

        try {
            // Build your Reponse entity
            Reponse rep = new Reponse(
                    currentAgent.getId(),
                    currentQuestionnaireId,
                    taR1.getText().trim(),
                    taR2.getText().trim(),
                    taR3.getText().trim().isEmpty() ? null : taR3.getText().trim(),
                    taR4.getText().trim().isEmpty() ? null : taR4.getText().trim()
            );

            // Let your service handle the logic!
            if (isUpdate) {
                reponseService.updateReponse(rep);
            } else {
                reponseService.createReponse(rep);
            }

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Les réponses ont été enregistrées !");
            navigateToDetails();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de sauvegarder les réponses.");
        }
    }

    private void navigateToDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AgentDetails.fxml"));
            Parent root = loader.load();

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