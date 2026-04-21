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
import java.util.regex.Pattern;

public class ReponseCreateController {

    @FXML private Label lblSubtitle;
    @FXML private Button btnAnnuler, btnEnregistrer;
    @FXML private VBox boxQ3, boxQ4;
    @FXML private Label lblQ1, lblQ2, lblQ3, lblQ4;
    @FXML private TextArea taR1, taR2, taR3, taR4;

    private Agent currentAgent;
    private int currentQuestionnaireId = -1;
    private ReponseService reponseService;

    // Matches PHP Assert\Regex logic
    private final Pattern BAD_WORDS_PATTERN = Pattern.compile(".*\\b(badword|insult|stupid)\\b.*", Pattern.CASE_INSENSITIVE);

    @FXML
    public void initialize() {
        reponseService = new ReponseService();
        setupRealTimeValidation();
        btnAnnuler.setOnAction(e -> navigateToDetails());
        btnEnregistrer.setOnAction(e -> handleSave());
    }

    public void initData(Agent agent) {
        this.currentAgent = agent;
        lblSubtitle.setText("Nouveau Questionnaire • " + agent.getPseudo());
        loadQuestions();
    }

    // Matches PHP Assert\Length(min: 5) and NotBlank
    private boolean isValidResponse(String text) {
        if (text == null) return false;
        String t = text.trim();
        return t.length() >= 5
                && !t.equalsIgnoreCase("Aucune réponse")
                && !t.equalsIgnoreCase("Aucune reponse");
    }

    private void applyValidationStyle(TextArea area, boolean isValid) {
        String color = isValid ? "#2a2a35" : "#ff3b3f"; // Red border if invalid
        area.setStyle("-fx-control-inner-background: #1e1e28; -fx-text-fill: white; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: " + color + ";");
    }

    private void loadQuestions() {
        Map<String, Object> questions = reponseService.getQuestionnaireByGame(currentAgent.getGame());
        if (questions != null) {
            currentQuestionnaireId = (int) questions.get("id");
            lblQ1.setText((String) questions.get("ques1"));
            lblQ2.setText((String) questions.get("ques2"));

            if (questions.get("ques3") == null || ((String) questions.get("ques3")).trim().isEmpty()) {
                boxQ3.setVisible(false); boxQ3.setManaged(false);
                lblQ3.setText("");
            } else lblQ3.setText((String) questions.get("ques3"));

            if (questions.get("ques4") == null || ((String) questions.get("ques4")).trim().isEmpty()) {
                boxQ4.setVisible(false); boxQ4.setManaged(false);
                lblQ4.setText("");
            } else lblQ4.setText((String) questions.get("ques4"));
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun questionnaire trouvé.");
            btnEnregistrer.setDisable(true);
        }
    }

    private void setupRealTimeValidation() {
        taR1.textProperty().addListener((obs, oldV, newV) -> applyValidationStyle(taR1, isValidResponse(newV) && !BAD_WORDS_PATTERN.matcher(newV).matches()));
        taR2.textProperty().addListener((obs, oldV, newV) -> applyValidationStyle(taR2, isValidResponse(newV)));
        taR3.textProperty().addListener((obs, oldV, newV) -> applyValidationStyle(taR3, isValidResponse(newV)));
        taR4.textProperty().addListener((obs, oldV, newV) -> applyValidationStyle(taR4, isValidResponse(newV)));
    }

    private void handleSave() {
        String r1 = taR1.getText();
        String r2 = taR2.getText();
        String r3 = taR3.getText();
        String r4 = taR4.getText();

        boolean hasQ3 = lblQ3.getText() != null && !lblQ3.getText().trim().isEmpty();
        boolean hasQ4 = lblQ4.getText() != null && !lblQ4.getText().trim().isEmpty();

        // --- CONTRÔLE DE SAISIE: QUESTION 1 ---
        if (!isValidResponse(r1)) {
            applyValidationStyle(taR1, false);
            showAlert(Alert.AlertType.WARNING, "Erreur de saisie", "La réponse à la question 1 est trop courte. Veuillez écrire au moins 5 caractères.");
            return;
        }
        if (BAD_WORDS_PATTERN.matcher(r1).matches()) {
            applyValidationStyle(taR1, false);
            showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez être professionnel. Le langage inapproprié n'est pas autorisé (Question 1).");
            return;
        }

        // --- CONTRÔLE DE SAISIE: QUESTION 2 ---
        if (!isValidResponse(r2)) {
            applyValidationStyle(taR2, false);
            showAlert(Alert.AlertType.WARNING, "Erreur de saisie", "La réponse à la question 2 est trop courte. Veuillez écrire au moins 5 caractères.");
            return;
        }

        // --- CONTRÔLE DE SAISIE: QUESTION 3 (Si elle existe) ---
        if (hasQ3 && !isValidResponse(r3)) {
            applyValidationStyle(taR3, false);
            showAlert(Alert.AlertType.WARNING, "Erreur de saisie", "La réponse à la question 3 est trop courte. Veuillez écrire au moins 5 caractères.");
            return;
        }

        // --- CONTRÔLE DE SAISIE: QUESTION 4 (Si elle existe) ---
        if (hasQ4 && !isValidResponse(r4)) {
            applyValidationStyle(taR4, false);
            showAlert(Alert.AlertType.WARNING, "Erreur de saisie", "La réponse à la question 4 est trop courte. Veuillez écrire au moins 5 caractères.");
            return;
        }

        // Si tout est valide, on enregistre
        try {
            Reponse rep = new Reponse(currentAgent.getId(), currentQuestionnaireId,
                    r1.trim(), r2.trim(),
                    hasQ3 ? r3.trim() : null,
                    hasQ4 ? r4.trim() : null);

            reponseService.createReponse(rep);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Les réponses ont été enregistrées avec succès !");
            navigateToDetails();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/agent/fxml/AgentDetails.fxml"));
            Parent root = loader.load();
            loader.<AgentDetailsController>getController().initData(currentAgent);
            btnAnnuler.getScene().setRoot(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}