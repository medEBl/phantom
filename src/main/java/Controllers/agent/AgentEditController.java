package Controllers.agent;

import entities.agent.Agent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import services.agent.AgentService;

import java.io.IOException;

public class AgentEditController {

    @FXML private Label lblSubtitle;
    @FXML private TextField tfPseudo;
    @FXML private TextField tfGame;
    @FXML private TextField tfRank;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField tfSocials;

    @FXML private Button btnEnregistrer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnRetour;

    private AgentService agentService;
    private Agent currentAgent; // Stores the agent being edited

    @FXML
    public void initialize() {
        agentService = new AgentService();

        // Populate Status Dropdown
        cbStatus.getItems().addAll("active", "inactive", "banned");

        // Button Actions
        btnEnregistrer.setOnAction(event -> handleUpdateAgent());
        btnAnnuler.setOnAction(event -> navigateToList());
        btnRetour.setOnAction(event -> navigateToList());
    }

    /**
     * CRITICAL METHOD: This receives the selected agent from the List screen
     * and fills all the text fields with their current data!
     */
    public void initData(Agent agent) {
        this.currentAgent = agent;

        // Set the dynamic subtitle
        lblSubtitle.setText(agent.getPseudo() + " • " + agent.getGame());

        // Fill the fields
        tfPseudo.setText(agent.getPseudo());
        tfGame.setText(agent.getGame()); // This field is locked in FXML
        tfRank.setText(agent.getRank());
        cbStatus.setValue(agent.getStatus());
        tfSocials.setText(agent.getSocialsLink());
    }

    private void handleUpdateAgent() {
        // 1. Controle de saisie (Validation)
        if (tfPseudo.getText() == null || tfPseudo.getText().trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le Pseudo ne peut pas être vide.");
            return;
        }

        try {
            // 2. Update the existing agent object with new values
            currentAgent.setPseudo(tfPseudo.getText().trim());
            currentAgent.setRank(tfRank.getText().trim());
            currentAgent.setStatus(cbStatus.getValue());
            currentAgent.setSocialsLink(tfSocials.getText().trim());

            // 3. Save to database using the service
            agentService.updateAgent(currentAgent);

            // 4. Show success and return to list
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil Agent mis à jour avec succès !");
            navigateToList();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier l'agent : " + e.getMessage());
        }
    }

    private void navigateToList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListAgents.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de navigation : " + e.getMessage());
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