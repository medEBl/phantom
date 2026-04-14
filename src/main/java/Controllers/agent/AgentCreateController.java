package Controllers.agent;

import entities.agent.Agent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import services.agent.AgentService;

import java.io.IOException;

public class AgentCreateController {

    @FXML private Button btnRetour, btnAnnuler, btnCreer;
    @FXML private ComboBox<String> cbPlayer, cbTeam, cbGame, cbStatus;
    @FXML private TextField tfPseudo, tfRank, tfSocials;

    private final AgentService service = new AgentService();

    // --- LE FLAG DE SÉCURITÉ ABSOLUE ---
    private boolean isAdminMode = false;

    // Cette méthode sera appelée avant d'afficher la page
    public void setAdminMode(boolean isAdmin) {
        this.isAdminMode = isAdmin;
    }

    @FXML
    public void initialize() {
        if (cbStatus != null) cbStatus.getItems().addAll("active", "banned", "pending");
        if (cbGame != null) cbGame.getItems().addAll("League of Legends", "Valorant", "Apex Legends", "Overwatch 2");
        if (cbPlayer != null) cbPlayer.getItems().addAll("Joueur 1", "Joueur 2");
        if (cbTeam != null) cbTeam.getItems().addAll("Team A", "Team B", "Free Agent");

        if (btnRetour != null) btnRetour.setOnAction(e -> navigateBack());
        if (btnAnnuler != null) btnAnnuler.setOnAction(e -> navigateBack());
        if (btnCreer != null) btnCreer.setOnAction(e -> handleCreate());
    }

    private void handleCreate() {
        if (tfPseudo.getText().trim().isEmpty() || tfRank.getText().trim().isEmpty() || cbGame.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez remplir les champs obligatoires.");
            return;
        }

        try {
            Agent agent = new Agent();
            agent.setPseudo(tfPseudo.getText().trim());
            agent.setGame(cbGame.getValue());
            agent.setRank(tfRank.getText().trim());
            agent.setStatus(cbStatus != null && cbStatus.getValue() != null ? cbStatus.getValue() : "active");
            agent.setSocialsLink(tfSocials != null ? tfSocials.getText().trim() : "");

            service.createAgent(agent);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil agent créé avec succès !");
            navigateBack();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Un problème est survenu.");
        }
    }

    private void navigateBack() {
        try {
            // On utilise le flag garanti pour choisir la bonne destination
            String fxmlPath = this.isAdminMode ? "/fxml/ListAgentsBack.fxml" : "/fxml/ListAgents.fxml";
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
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