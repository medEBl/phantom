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
import java.sql.Timestamp;

public class AgentCreateController {

    @FXML private ComboBox<String> cbPlayer;
    @FXML private ComboBox<String> cbTeam;
    @FXML private TextField tfPseudo;
    @FXML private ComboBox<String> cbGame;
    @FXML private TextField tfRank;
    @FXML private ComboBox<String> cbStatus;
    @FXML private TextField tfSocials;
    @FXML private Button btnCreer;
    @FXML private Button btnAnnuler;
    @FXML private Button btnRetour;

    private AgentService agentService;

    @FXML
    public void initialize() {
        agentService = new AgentService();

        // Load dynamic data from database
        loadComboBoxData();

        // Button Actions
        btnCreer.setOnAction(event -> handleCreateAgent());
        btnAnnuler.setOnAction(event -> clearForm());
        btnRetour.setOnAction(event -> navigateToList());
    }

    private void loadComboBoxData() {
        System.out.println("--- DÉBUT DU CHARGEMENT DES DONNÉES ---");

        // 1. Check Connection
        Connection cnx = Phantom.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("❌ ERREUR CRITIQUE : La connexion à la base de données est NULL !");
            return;
        }
        System.out.println("✅ Connexion DB réussie.");

        // 2. Load Players
        try {
            System.out.println("⏳ Chargement des joueurs...");
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name FROM player");
            int count = 0;
            while (rs.next()) {
                cbPlayer.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
                count++;
            }
            System.out.println("✅ " + count + " joueurs chargés !");
        } catch (Exception e) {
            System.err.println("❌ Erreur Joueurs : " + e.getMessage());
            e.printStackTrace();
        }

        // 3. Load Teams
        try {
            System.out.println("⏳ Chargement des équipes...");
            cbTeam.getItems().add("No Team (Free Agent)");
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT id, name FROM team");
            int count = 0;
            while (rs.next()) {
                cbTeam.getItems().add(rs.getInt("id") + " - " + rs.getString("name"));
                count++;
            }
            System.out.println("✅ " + count + " équipes chargées !");
        } catch (Exception e) {
            System.err.println("❌ Erreur Equipes : " + e.getMessage());
            e.printStackTrace();
        }

        // 4. Load Games
        try {
            System.out.println("⏳ Chargement des jeux...");
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery("SELECT DISTINCT game FROM questionnaire_agent");
            int count = 0;
            while (rs.next()) {
                String game = rs.getString("game");
                if (game != null && !game.trim().isEmpty()) {
                    cbGame.getItems().add(game);
                    count++;
                }
            }
            System.out.println("✅ " + count + " jeux chargés !");
        } catch (Exception e) {
            System.err.println("❌ Erreur Jeux : " + e.getMessage());
            e.printStackTrace();
        }

        cbStatus.getItems().addAll("active", "inactive", "banned");
        System.out.println("--- FIN DU CHARGEMENT ---");
    }

    private void handleCreateAgent() {
        // Validation
        if (tfPseudo.getText().isEmpty() || cbPlayer.getValue() == null || cbGame.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir les champs obligatoires (Joueur, Pseudo, Jeu).");
            return;
        }

        try {
            // Create Agent and safely use setters
            Agent newAgent = new Agent();
            newAgent.setPseudo(tfPseudo.getText());
            newAgent.setGame(cbGame.getValue());
            newAgent.setRank(tfRank.getText().isEmpty() ? "Unranked" : tfRank.getText());
            newAgent.setStatus(cbStatus.getValue() == null ? "active" : cbStatus.getValue());
            newAgent.setSocialsLink(tfSocials.getText());
            newAgent.setDateOfCreation(new Timestamp(System.currentTimeMillis()));

            // Extract IDs
            newAgent.setIdPlayer(Integer.parseInt(cbPlayer.getValue().split(" - ")[0]));
            if (cbTeam.getValue() != null && !cbTeam.getValue().equals("No Team (Free Agent)")) {
                newAgent.setIdTeam(Integer.parseInt(cbTeam.getValue().split(" - ")[0]));
            } else {
                newAgent.setIdTeam(null);
            }

            // Save to DB using your Service
            agentService.createAgent(newAgent);

            // Show Success Alert
            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'agent a été créé avec succès !");

            // Go back to the list to see the new agent!
            navigateToList();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer l'agent : " + e.getMessage());
        }
    }

    private void clearForm() {
        cbPlayer.setValue(null);
        cbTeam.setValue("No Team (Free Agent)");
        tfPseudo.clear();
        cbGame.setValue(null);
        tfRank.clear();
        cbStatus.setValue(null);
        tfSocials.clear();
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