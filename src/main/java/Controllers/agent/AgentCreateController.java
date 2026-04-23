package Controllers.agent;

import entities.agent.Agent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ButtonBar;
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

        // Load ALL dynamic data from the database immediately
        loadDynamicData();

        // --- REAL-TIME VALIDATION LISTENERS ---
        setupRealTimeValidation();

        if (btnRetour != null) btnRetour.setOnAction(e -> navigateBack());
        if (btnAnnuler != null) btnAnnuler.setOnAction(e -> navigateBack());
        if (btnCreer != null) btnCreer.setOnAction(e -> handleCreate());
    }

    private void setupRealTimeValidation() {
        if (tfPseudo != null) {
            tfPseudo.textProperty().addListener((obs, oldText, newText) -> {
                String text = newText == null ? "" : newText.trim();
                applyValidationStyle(tfPseudo, text.length() >= 3 && text.length() <= 50);
            });
        }

        if (tfRank != null) {
            tfRank.textProperty().addListener((obs, oldText, newText) -> {
                String text = newText == null ? "" : newText.trim();
                // OBLIGATOIRE: Doit contenir UNIQUEMENT des chiffres (donc pas de nombres négatifs ni de lettres)
                boolean isValid = !text.isEmpty() && text.matches("^\\d+$");
                applyValidationStyle(tfRank, isValid);
            });
        }

        if (tfSocials != null) {
            tfSocials.textProperty().addListener((obs, oldText, newText) -> {
                String text = newText == null ? "" : newText.trim();
                String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
                // OBLIGATOIRE: Doit ne pas être vide ET doit être un format URL valide
                boolean isValid = !text.isEmpty() && text.matches(urlRegex);
                applyValidationStyle(tfSocials, isValid);
            });
        }
    }

    private void applyValidationStyle(TextField field, boolean isValid) {
        if (isValid) {
            field.setStyle("-fx-border-color: #2a2a35; -fx-border-radius: 8; -fx-background-radius: 8; -fx-background-color: rgba(30, 30, 40, 0.9); -fx-text-fill: white;");
        } else {
            field.setStyle("-fx-border-color: #ff3b3f; -fx-border-radius: 8; -fx-background-radius: 8; -background-color: rgba(30, 30, 40, 0.9); -fx-text-fill: white;");
        }
    }

    private void loadDynamicData() {
        try {
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
                rsPlayers.close();
            }

            // 2. Load Teams
            if (cbTeam != null) {
                cbTeam.getItems().add("Free Agent (Aucune)");
                ResultSet rsTeams = st.executeQuery("SELECT id, name FROM team");
                while (rsTeams.next()) {
                    String name = rsTeams.getString("name");
                    int id = rsTeams.getInt("id");
                    teamMap.put(name, id);
                    cbTeam.getItems().add(name);
                }
                rsTeams.close();
            }

            // 3. Load ALL Games directly into the ComboBox
            if (cbGame != null) {
                ResultSet rsGames = st.executeQuery("SELECT DISTINCT game FROM questionnaire_agent");
                while (rsGames.next()) {
                    cbGame.getItems().add(rsGames.getString("game"));
                }
                rsGames.close();
                cbGame.setPromptText("Choisissez un jeu");
            }

            st.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur BDD", "Impossible de charger les données.");
        }
    }

    private void handleCreate() {
        System.out.println("--- BOUTON CRÉER CLIQUÉ ---");

        String pseudo = tfPseudo != null ? tfPseudo.getText().trim() : "";
        String rank = tfRank != null ? tfRank.getText().trim() : "";
        String socials = tfSocials != null ? tfSocials.getText().trim() : "";
        String game = cbGame != null ? cbGame.getValue() : null;
        String player = cbPlayer != null ? cbPlayer.getValue() : null;
        String status = cbStatus != null && cbStatus.getValue() != null ? cbStatus.getValue() : "active";

        // 1. Validation Player
        if (player == null || player.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Joueur manquant", "Veuillez sélectionner un joueur dans la liste.");
            return;
        }

        // 2. Validation Game
        if (game == null || game.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Jeu manquant", "Veuillez sélectionner un jeu.");
            return;
        }

        // 3. Validation Pseudo
        if (pseudo.isEmpty() || pseudo.length() < 3 || pseudo.length() > 50) {
            if (tfPseudo != null) applyValidationStyle(tfPseudo, false);
            showAlert(Alert.AlertType.WARNING, "Pseudo invalide", "Le pseudo doit contenir entre 3 et 50 caractères.");
            return;
        }

        // 4. Validation Rank (Obligatoire et doit être un nombre positif)
        if (rank.isEmpty() || !rank.matches("^\\d+$")) {
            if (tfRank != null) applyValidationStyle(tfRank, false);
            showAlert(Alert.AlertType.WARNING, "Rang invalide", "Le champ rang est obligatoire et doit être un nombre positif.");
            return;
        }

        // 5. Validation Socials (OBLIGATOIRE et doit être une URL)
        String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
        if (socials.isEmpty() || !socials.matches(urlRegex)) {
            if (tfSocials != null) applyValidationStyle(tfSocials, false);
            showAlert(Alert.AlertType.WARNING, "Lien social invalide", "Le lien social est obligatoire et doit être une URL valide (ex: https://...).");
            return;
        }

        // Save if everything is valid
        try {
            Integer selectedPlayerId = playerMap.get(player);
            if (selectedPlayerId == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur Joueur", "Le joueur sélectionné est invalide.");
                return;
            }

            if (service.agentExistsForPlayerAndGame(selectedPlayerId, game)) {
                showAlert(Alert.AlertType.ERROR, "Doublon détecté", "Vous avez déjà créé un Agent pour ce jeu !");
                return;
            }

            Agent agent = new Agent();
            agent.setPseudo(pseudo);
            agent.setGame(game);
            agent.setRank(rank);
            agent.setStatus(status);
            agent.setSocialsLink(socials);
            agent.setIdPlayer(selectedPlayerId);

            if (cbTeam != null && cbTeam.getValue() != null && !cbTeam.getValue().equals("Free Agent (Aucune)")) {
                Integer teamId = teamMap.get(cbTeam.getValue());
                agent.setIdTeam(teamId);
            } else {
                agent.setIdTeam(null);
            }

            service.createAgent(agent);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Profil agent créé avec succès !");
            navigateBack();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur serveur", "Un problème est survenu lors de la création.");
        }
    }

    private void navigateBack() {
        try {
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

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1a1a24; -fx-border-color: #2a2a35; -fx-border-width: 2;");

        if (dialogPane.lookup(".content.label") != null) {
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #e6e6e6; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
        if (dialogPane.lookup(".header-panel") != null) {
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #1a1a24;");
        }

        ButtonBar buttonBar = (ButtonBar) dialogPane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.getButtons().forEach(b -> b.setStyle("-fx-background-color: #ff2d2d; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;"));
        }

        alert.showAndWait();
    }
}