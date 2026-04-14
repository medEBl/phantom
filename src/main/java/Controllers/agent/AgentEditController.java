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
        // Initialiser la ComboBox des statuts
        if (cbStatus != null) {
            cbStatus.getItems().addAll("active", "banned", "pending");
        }

        // Actions des boutons
        btnRetour.setOnAction(e -> navigateBack());
        btnAnnuler.setOnAction(e -> navigateBack());
        btnEnregistrer.setOnAction(e -> handleUpdate());
    }

    public void initData(Agent agent) {
        this.currentAgent = agent;

        // Définir le sous-titre pour aider la navigation au retour
        // Si on charge AgentEditBack.fxml, le FXML a déjà "Édition Administrative" par défaut
        if (lblSubtitle.getText().equals("Pseudo • Game")) {
            lblSubtitle.setText(agent.getPseudo() + " • " + agent.getGame());
        }

        tfPseudo.setText(agent.getPseudo());
        tfGame.setText(agent.getGame());
        tfRank.setText(agent.getRank());
        tfSocials.setText(agent.getSocialsLink());
        cbStatus.setValue(agent.getStatus());
    }

    private void handleUpdate() {
        if (tfPseudo.getText().trim().isEmpty() || tfRank.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Le pseudo et le rang sont obligatoires.");
            return;
        }

        try {
            currentAgent.setPseudo(tfPseudo.getText().trim());
            currentAgent.setRank(tfRank.getText().trim());
            currentAgent.setStatus(cbStatus.getValue());
            currentAgent.setSocialsLink(tfSocials.getText().trim());

            service.updateAgent(currentAgent);

            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le profil a été mis à jour !");
            navigateBack();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de mettre à jour le profil.");
        }
    }

    private void navigateBack() {
        try {
            // LOGIQUE DE RETOUR :
            // Si le texte contient "Admin", on retourne vers ListAgentsBack.fxml
            String fxmlPath = "/fxml/ListAgents.fxml";

            if (lblSubtitle.getText().contains("Admin")) {
                fxmlPath = "/fxml/ListAgentsBack.fxml";
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