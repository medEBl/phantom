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

import tools.Phantom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AgentCreateController {

    @FXML private Button btnRetour, btnAnnuler, btnCreer;
    @FXML private ComboBox<String> cbPlayer, cbTeam, cbGame, cbStatus;
    @FXML private TextField tfPseudo, tfRank, tfSocials;

    private final AgentService service = new AgentService();

    private boolean isAdminMode = false;

    // Dictionaries to link the Names displayed in the UI to their Database IDs
    private Map<String, Integer> playerMap = new HashMap<>();
    private Map<String, Integer> teamMap = new HashMap<>();

    public void setAdminMode(boolean isAdmin) {
        this.isAdminMode = isAdmin;
    }

    @FXML
    public void initialize() {
        if (cbStatus != null) cbStatus.getItems().addAll("active", "banned", "pending");
        if (cbGame != null) cbGame.getItems().addAll("League of Legends", "Valorant", "Apex Legends", "Overwatch 2", "Counter-Strike 2", "Rocket League");

        // Load dynamic data from the database
        loadDynamicData();

        if (btnRetour != null) btnRetour.setOnAction(e -> navigateBack());
        if (btnAnnuler != null) btnAnnuler.setOnAction(e -> navigateBack());
        if (btnCreer != null) btnCreer.setOnAction(e -> handleCreate());
    }

    private void loadDynamicData() {
        try {
            // Use your Singleton connection
            Connection cnx = Phantom.getInstance().getCnx();
            Statement st = cnx.createStatement();

            // 1. Load Players
            if (cbPlayer != null) {
                ResultSet rsPlayers = st.executeQuery("SELECT id, name FROM player");
                while (rsPlayers.next()) {
                    String name = rsPlayers.getString("name");
                    int id = rsPlayers.getInt("id");

                    playerMap.put(name, id);
                    cbPlayer.getItems().add(name);
                }
            }

            // 2. Load Teams
            if (cbTeam != null) {
                cbTeam.getItems().add("Free Agent (Aucune)"); // Allow null team option

                ResultSet rsTeams = st.executeQuery("SELECT id, name FROM team");
                while (rsTeams.next()) {
                    String name = rsTeams.getString("name");
                    int id = rsTeams.getInt("id");

                    teamMap.put(name, id);
                    cbTeam.getItems().add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "Impossible de charger les joueurs et les équipes.");
        }
    }

    private void handleCreate() {
        // We also check if a Player is selected since id_player is NOT NULL in your SQL
        if (tfPseudo.getText().trim().isEmpty() || tfRank.getText().trim().isEmpty() || cbGame.getValue() == null || cbPlayer.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez remplir les champs obligatoires (Pseudo, Rang, Jeu, Player).");
            return;
        }

        try {
            Agent agent = new Agent();
            agent.setPseudo(tfPseudo.getText().trim());
            agent.setGame(cbGame.getValue());
            agent.setRank(tfRank.getText().trim());
            agent.setStatus(cbStatus != null && cbStatus.getValue() != null ? cbStatus.getValue() : "active");
            agent.setSocialsLink(tfSocials != null ? tfSocials.getText().trim() : "");

            // --- LINKING THE DYNAMIC IDs ---
            // Fetch the ID corresponding to the selected Player name
            agent.setIdPlayer(playerMap.get(cbPlayer.getValue()));

            // Handle the Team (It can be null according to your SQL dump)
            if (cbTeam != null && cbTeam.getValue() != null && !cbTeam.getValue().equals("Free Agent (Aucune)")) {
                agent.setIdTeam(teamMap.get(cbTeam.getValue()));
            } else {
                agent.setIdTeam(null);
            }

            service.createAgent(agent);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil agent créé avec succès !");
            navigateBack();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Un problème est survenu lors de la création.");
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