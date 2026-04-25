package Controllers.agent;

import entities.agent.Agent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tools.Phantom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import services.ai.GeminiService;
import tools.AiEvaluationResult;
import entities.questionnaire.Questionnaire;
import entities.reponse.Reponse;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import services.discordscout.DiscordScoutService;

public class AgentDetailsController {

    // --- Sidebar Elements (Pour la détection Front/Back) ---
    @FXML private Button btnNavQuestionnaires;

    // --- Main UI Elements ---
    @FXML private Button btnRetour;
    @FXML private Label lblPseudo, lblJeu, lblRang, lblStatut, lblDate, lblLien;
    @FXML private VBox aiBanner;
    @FXML private VBox qaContainer;
    // --- AI Elements ---
    @FXML private Button btnRunAi; // N'oubliez pas de mettre cet ID sur votre bouton rouge dans SceneBuilder !

    private GeminiService aiService = new GeminiService();
    private Questionnaire currentQuestionnaire;
    private Reponse currentReponse;

    private Agent currentAgent;

    @FXML
    public void initialize() {
        btnRetour.setOnAction(event -> navigateBack());
    }

    public void initData(Agent agent) {
        this.currentAgent = agent;

        // Remplissage sécurisé de la colonne de gauche
        if (lblPseudo != null) lblPseudo.setText(agent.getPseudo());
        if (lblJeu != null) lblJeu.setText(agent.getGame());
        if (lblRang != null) lblRang.setText(agent.getRank());

        if (lblStatut != null) {
            lblStatut.setText(agent.getStatus());
            if (agent.getStatus().equalsIgnoreCase("active")) {
                lblStatut.setTextFill(Color.web("#00e676"));
            } else if (agent.getStatus().equalsIgnoreCase("banned")) {
                lblStatut.setTextFill(Color.web("#ff4444"));
            } else {
                lblStatut.setTextFill(Color.web("#a0a0a0"));
            }
        }

        if (lblDate != null) {
            lblDate.setText(agent.getDateOfCreation() != null ? String.valueOf(agent.getDateOfCreation()) : "Non renseigné");
        }

        if (lblLien != null) {
            lblLien.setText(agent.getSocialsLink() != null && !agent.getSocialsLink().isEmpty() ? agent.getSocialsLink() : "Aucun lien social");
        }

        // Chargement de la colonne de droite (Questionnaire)
        loadQuestionnaireData();
    }

    private void loadQuestionnaireData() {
        qaContainer.getChildren().clear();

        // On vérifie si on est dans l'espace Admin (Back-Office)
        boolean isAdmin = (btnNavQuestionnaires != null);

        String sql = "SELECT q.id AS q_id, q.ques1, q.ques2, q.ques3, q.ques4, " +
                "r.rep1, r.rep2, r.rep3, r.rep4 " +
                "FROM questionnaire_agent q " +
                "LEFT JOIN reponse_questionnaire r ON q.id = r.questionnaire_id AND r.id_agent = ? " +
                "WHERE q.game = ?";

        Connection cnx = Phantom.getInstance().getCnx();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, currentAgent.getId());
            ps.setString(2, currentAgent.getGame());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean hasAnswers = rs.getString("rep1") != null;

                if (hasAnswers) {
                    // 1. Afficher la bannière IA
                    if (aiBanner != null) {
                        aiBanner.setVisible(true);
                        aiBanner.setManaged(true);
                    }

                    // 2. Préparation des objets pour Gemini
                    currentQuestionnaire = new Questionnaire();
                    currentQuestionnaire.setGame(currentAgent.getGame());
                    currentQuestionnaire.setQues1(rs.getString("ques1"));
                    currentQuestionnaire.setQues2(rs.getString("ques2"));
                    currentQuestionnaire.setQues3(rs.getString("ques3"));
                    currentQuestionnaire.setQues4(rs.getString("ques4"));

                    currentReponse = new Reponse();
                    currentReponse.setRep1(rs.getString("rep1"));
                    currentReponse.setRep2(rs.getString("rep2"));
                    currentReponse.setRep3(rs.getString("rep3"));
                    currentReponse.setRep4(rs.getString("rep4"));

                    // 3. Connecter le bouton IA
                    if (btnRunAi != null) {
                        btnRunAi.setOnAction(e -> runAiAnalysis());
                    }

                    // 4. Affichage des questions/réponses sur l'interface
                    if (rs.getString("ques1") != null) qaContainer.getChildren().add(createQABox(rs.getString("ques1"), rs.getString("rep1")));
                    if (rs.getString("ques2") != null) qaContainer.getChildren().add(createQABox(rs.getString("ques2"), rs.getString("rep2")));
                    if (rs.getString("ques3") != null && !rs.getString("ques3").trim().isEmpty()) qaContainer.getChildren().add(createQABox(rs.getString("ques3"), rs.getString("rep3")));
                    if (rs.getString("ques4") != null && !rs.getString("ques4").trim().isEmpty()) qaContainer.getChildren().add(createQABox(rs.getString("ques4"), rs.getString("rep4")));

                    // 5. Boutons d'Action (Modifier / Supprimer)
                    HBox actionBox = new HBox(15);
                    actionBox.setAlignment(Pos.CENTER_RIGHT);
                    actionBox.setPadding(new Insets(15, 0, 0, 0));

                    if (!isAdmin) {
                        Button btnEditRes = new Button("✎ Modifier");
                        btnEditRes.setStyle("-fx-background-color: transparent; -fx-border-color: #444455; -fx-text-fill: white; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
                        btnEditRes.setOnAction(e -> openReponseEditScreen());
                        actionBox.getChildren().add(btnEditRes);
                    }

                    Button btnDeleteRes = new Button("🗑 Supprimer");
                    btnDeleteRes.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3b3f; -fx-text-fill: #ff3b3f; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
                    btnDeleteRes.setOnAction(e -> deleteResponses());
                    actionBox.getChildren().add(btnDeleteRes);

                    qaContainer.getChildren().add(actionBox);

                } else {
                    showEmptyState();
                }
            } else {
                showEmptyState("Aucun questionnaire disponible pour " + currentAgent.getGame());
            }

        } catch (Exception e) {
            System.err.println("Erreur chargement questionnaire: " + e.getMessage());
        }
    }

    // --- Action Methods ---

    private void openReponseScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReponseCreate.fxml"));
            Parent root = loader.load();

            // 1. Get the controller using the correct package
            Controllers.reponse.ReponseCreateController controller = loader.getController();

            // 2. Pass the data to it
            controller.initData(currentAgent);

            // 3. Switch the scene
            btnRetour.getScene().setRoot(root);

        } catch (Exception ex) {
            System.err.println("Erreur de navigation vers ReponseCreate: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private void deleteResponses() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer les réponses de cet agent ?", ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText(null);
        confirm.showAndWait();

        if (confirm.getResult() == ButtonType.YES) {
            String sql = "DELETE FROM reponse_questionnaire WHERE id_agent = ?";
            try {
                Connection cnx = Phantom.getInstance().getCnx();
                PreparedStatement ps = cnx.prepareStatement(sql);
                ps.setInt(1, currentAgent.getId());
                ps.executeUpdate();

                // Rafraîchir la vue
                loadQuestionnaireData();
            } catch (Exception e) {
                System.err.println("Erreur suppression: " + e.getMessage());
            }
        }
    }

    private void navigateBack() {
        try {
            // Retour intelligent : Back ou Front
            boolean isAdmin = (btnNavQuestionnaires != null);
            String fxmlPath = isAdmin ? "/fxml/ListAgentsBack.fxml" : "/fxml/ListAgents.fxml";

            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            btnRetour.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de retour: " + e.getMessage());
        }
    }

    // --- UI Helper Methods ---

    private VBox createQABox(String question, String answer) {
        VBox box = new VBox();
        box.setStyle("-fx-background-color: #22222d; -fx-background-radius: 8; -fx-padding: 15;");

        Label lblQ = new Label(question.toUpperCase());
        lblQ.setTextFill(Color.web("#a0a0a0"));
        lblQ.setFont(Font.font("System", 11));
        lblQ.setWrapText(true);
        VBox.setMargin(lblQ, new Insets(0, 0, 5, 0));

        Label lblA = new Label(answer != null && !answer.trim().isEmpty() ? answer : "Aucune réponse");
        lblA.setTextFill(Color.WHITE);
        lblA.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblA.setWrapText(true);

        box.getChildren().addAll(lblQ, lblA);
        return box;
    }

    private void showEmptyState() {
        showEmptyState("Cet agent n'a pas encore rempli le questionnaire.");
    }

    private void showEmptyState(String description) {
        if (aiBanner != null) {
            aiBanner.setVisible(false);
            aiBanner.setManaged(false);
        }

        boolean isAdmin = (btnNavQuestionnaires != null);

        VBox emptyBox = new VBox(10);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setStyle("-fx-background-color: #111116; -fx-border-color: #2a2a36; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 30;");

        Label lblIcon = new Label("ℹ");
        lblIcon.setTextFill(Color.web("#a0a0a0"));
        lblIcon.setFont(Font.font("System", FontWeight.BOLD, 24));

        Label lblTitle = new Label("Pas encore de réponses");
        lblTitle.setTextFill(Color.WHITE);
        lblTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

        Label lblDesc = new Label(description);
        lblDesc.setTextFill(Color.web("#a0a0a0"));
        lblDesc.setWrapText(true);

        emptyBox.getChildren().addAll(lblIcon, lblTitle, lblDesc);

        // Le bouton "Remplir" n'apparaît QUE pour le joueur
        if (!isAdmin) {
            Button btnRemplir = new Button("✎ Remplir le Questionnaire");
            btnRemplir.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3344; -fx-text-fill: #ff3344; -fx-border-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");
            btnRemplir.setOnAction(e -> openReponseScreen());
            emptyBox.getChildren().add(btnRemplir);
        }

        qaContainer.getChildren().add(emptyBox);
    }
    private void openReponseEditScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReponseEdit.fxml"));
            Parent root = loader.load();

            // 1. Get the Edit controller
            Controllers.reponse.ReponseEditController controller = loader.getController();

            // 2. Pass the data to it
            controller.initData(currentAgent);

            // 3. Switch the scene
            btnRetour.getScene().setRoot(root);

        } catch (Exception ex) {
            System.err.println("Erreur de navigation vers ReponseEdit: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    // --- AI ANALYSIS METHODS ---

    private void runAiAnalysis() {
        if (currentQuestionnaire == null || currentReponse == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Les données du questionnaire sont introuvables.");
            alert.show();
            return;
        }

        // 1. État de chargement (Feedback visuel)
        btnRunAi.setText("⏳ Analyse en cours...");
        btnRunAi.setDisable(true);
        btnRunAi.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 6;");

        // 2. Lancer l'appel API dans un Thread séparé pour ne pas bloquer l'interface
        CompletableFuture.supplyAsync(() -> {
            return aiService.evaluate(currentQuestionnaire, currentReponse);
        }).thenAccept(result -> {
            // 3. Réponse reçue ! On revient sur le Thread de l'interface graphique
            Platform.runLater(() -> {
                // Rétablir le bouton à son état d'origine
                btnRunAi.setText("✨ Run AI Analysis");
                btnRunAi.setStyle("-fx-background-color: #ff3b3f; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnRunAi.setDisable(false);

                // --- 🚨 DÉCLENCHEUR WEBHOOK DISCORD 🚨 ---
                if (result.getScore() >= 80) {
                    // Sécurité : on vérifie que le lien social n'est pas null
                    String lienSocial = "https://phantom-esport.com"; // Lien par défaut
                    if (currentAgent.getSocialsLink() != null && !currentAgent.getSocialsLink().trim().isEmpty()) {
                        lienSocial = currentAgent.getSocialsLink();
                    }

                    // Appel du service (Assurez-vous que le package est le bon, ici "services")
                    services.discordscout.DiscordScoutService.sendHighScorerAlert(
                            currentAgent.getPseudo(),
                            currentQuestionnaire.getGame(),
                            result.getScore(),
                            lienSocial
                    );
                }
                // -----------------------------------------

                // Afficher la popup avec les résultats
                showAiResultDialog(result);
            });
        }).exceptionally(ex -> {
            // 4. En cas d'erreur réseau
            Platform.runLater(() -> {
                btnRunAi.setText("❌ Erreur de connexion");
                btnRunAi.setStyle("-fx-background-color: #ff3b3f; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                btnRunAi.setDisable(false);
                ex.printStackTrace();
            });
            return null;
        });
    }

    private void showAiResultDialog(AiEvaluationResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Coach IA - Rapport d'Analyse");

        // Un titre dynamique selon le score
        String appreciation = result.getScore() >= 80 ? "Excellent" : (result.getScore() >= 50 ? "Moyen" : "À améliorer");
        alert.setHeaderText("Score global : " + result.getScore() + " / 100 (" + appreciation + ")");

        StringBuilder content = new StringBuilder();
        content.append("📝 AVIS GLOBAL :\n");
        content.append(result.getGlobalFeedback()).append("\n\n");

        content.append("💡 CONSEILS SPÉCIFIQUES :\n");
        for (Map.Entry<String, String> suggestion : result.getSuggestions().entrySet()) {
            content.append("• ").append(suggestion.getValue()).append("\n\n");
        }

        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        // Taille de la fenêtre popup
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(600, 450);

        alert.showAndWait();
    }

}