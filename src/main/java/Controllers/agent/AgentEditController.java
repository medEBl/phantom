package Controllers.agent;

import entities.agent.Agent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import services.agent.AgentService;

import java.io.IOException;

public class AgentEditController {

    @FXML private Label lblSubtitle;
    @FXML private TextField tfPseudo, tfGame, tfRank, tfSocials;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnRetour, btnAnnuler, btnEnregistrer;

    private Agent currentAgent;
    private final AgentService service = new AgentService();

    @FXML
    public void initialize() {
        if (cbStatus != null) {
            cbStatus.getItems().addAll("active", "banned", "pending");
        }

        // --- REAL-TIME VALIDATION LISTENERS ---
        setupRealTimeValidation();

        btnRetour.setOnAction(e -> navigateBack());
        btnAnnuler.setOnAction(e -> navigateBack());
        btnEnregistrer.setOnAction(e -> handleUpdate());
    }

    public void initData(Agent agent) {
        this.currentAgent = agent;

        if (lblSubtitle.getText().equals("Pseudo • Game")) {
            lblSubtitle.setText(agent.getPseudo() + " • " + agent.getGame());
        }

        tfPseudo.setText(agent.getPseudo());
        tfGame.setText(agent.getGame()); // Game is usually read-only in Edit
        tfRank.setText(agent.getRank());
        tfSocials.setText(agent.getSocialsLink());
        cbStatus.setValue(agent.getStatus());
    }

    private void setupRealTimeValidation() {
        // Validation Pseudo
        if (tfPseudo != null) {
            tfPseudo.textProperty().addListener((obs, oldText, newText) -> {
                String text = newText == null ? "" : newText.trim();
                boolean isValid = text.length() >= 3 && text.length() <= 50;
                applyValidationStyle(tfPseudo, isValid);
            });
        }

        // Validation Rank
        if (tfRank != null) {
            tfRank.textProperty().addListener((obs, oldText, newText) -> {
                String text = newText == null ? "" : newText.trim();
                boolean isValid = !text.isEmpty();
                if (isValid) {
                    try {
                        if (Double.parseDouble(text) < 0) isValid = false;
                    } catch (NumberFormatException e) { }
                }
                applyValidationStyle(tfRank, isValid);
            });
        }

        // Validation Socials
        if (tfSocials != null) {
            tfSocials.textProperty().addListener((obs, oldText, newText) -> {
                String text = newText == null ? "" : newText.trim();
                String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
                boolean isValid = !text.isEmpty() && text.matches(urlRegex);
                applyValidationStyle(tfSocials, isValid);
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

    private void handleUpdate() {
        String pseudo = tfPseudo != null ? tfPseudo.getText().trim() : "";
        String rank = tfRank != null ? tfRank.getText().trim() : "";
        String socials = tfSocials != null ? tfSocials.getText().trim() : "";
        String status = cbStatus != null ? cbStatus.getValue() : null;

        // 1. Validation Pseudo
        if (pseudo.isEmpty() || pseudo.length() < 3 || pseudo.length() > 50) {
            if (tfPseudo != null) applyValidationStyle(tfPseudo, false);
            showAlert(Alert.AlertType.WARNING, "Pseudo invalide", "Le pseudo doit contenir entre 3 et 50 caractères.");
            return;
        }

        // 2. Validation Rank
        if (rank.isEmpty()) {
            if (tfRank != null) applyValidationStyle(tfRank, false);
            showAlert(Alert.AlertType.WARNING, "Rang manquant", "Le champ rang est obligatoire.");
            return;
        }
        try {
            if (Double.parseDouble(rank) < 0) {
                if (tfRank != null) applyValidationStyle(tfRank, false);
                showAlert(Alert.AlertType.WARNING, "Rang invalide", "Le rang ne peut pas être négatif.");
                return;
            }
        } catch (NumberFormatException e) { }

        // 3. Validation Status
        if (status == null) {
            showAlert(Alert.AlertType.WARNING, "Statut manquant", "Veuillez sélectionner un statut.");
            return;
        }

        // 4. Validation Socials
        String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
        if (socials.isEmpty() || !socials.matches(urlRegex)) {
            if (tfSocials != null) applyValidationStyle(tfSocials, false);
            showAlert(Alert.AlertType.WARNING, "Lien social invalide", "Veuillez entrer une URL valide.");
            return;
        }

        // Update if valid
        try {
            currentAgent.setPseudo(pseudo);
            currentAgent.setRank(rank);
            currentAgent.setStatus(status);
            currentAgent.setSocialsLink(socials);

            service.updateAgent(currentAgent);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le profil a été mis à jour !");
            navigateBack();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur serveur", "Impossible de mettre à jour le profil.");
        }
    }

    private void navigateBack() {
        try {
            String fxmlPath = "/agent/fxml/ListAgents.fxml";

            if (lblSubtitle != null && lblSubtitle.getText().contains("Admin")) {
                fxmlPath = "/agent/fxml/ListAgentsBack.fxml";
            }

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            btnRetour.getScene().setRoot(root);

        } catch (IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
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