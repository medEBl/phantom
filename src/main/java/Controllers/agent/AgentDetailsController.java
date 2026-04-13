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
import java.text.SimpleDateFormat;

public class AgentDetailsController {

    @FXML private Button btnRetour;
    @FXML private Label lblPseudo, lblJeu, lblRang, lblStatut, lblDate, lblLien;
    @FXML private VBox aiBanner;
    @FXML private VBox qaContainer;

    private Agent currentAgent;

    @FXML
    public void initialize() {
        btnRetour.setOnAction(event -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListAgents.fxml"));
                btnRetour.getScene().setRoot(root);
            } catch (IOException e) {
                System.err.println("Erreur de retour: " + e.getMessage());
            }
        });
    }

    public void initData(Agent agent) {
        this.currentAgent = agent;

        // Fill left column
        lblPseudo.setText(agent.getPseudo());
        lblJeu.setText(agent.getGame());
        lblRang.setText(agent.getRank());
        lblStatut.setText(agent.getStatus());

        if (agent.getStatus().equalsIgnoreCase("active")) {
            lblStatut.setTextFill(Color.web("#00e676"));
        } else if (agent.getStatus().equalsIgnoreCase("banned")) {
            lblStatut.setTextFill(Color.web("#ff4444"));
        } else {
            lblStatut.setTextFill(Color.web("#a0a0a0"));
        }

        if (agent.getDateOfCreation() != null) {
            lblDate.setText(new SimpleDateFormat("dd MMM yyyy").format(agent.getDateOfCreation()));
        }

        lblLien.setText(agent.getSocialsLink() != null ? agent.getSocialsLink() : "Aucun lien social");

        // Load right column
        loadQuestionnaireData();
    }

    private void loadQuestionnaireData() {
        qaContainer.getChildren().clear();

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
                    aiBanner.setVisible(true);
                    aiBanner.setManaged(true);

                    // Add the Q&A boxes
                    if (rs.getString("ques1") != null) qaContainer.getChildren().add(createQABox(rs.getString("ques1"), rs.getString("rep1")));
                    if (rs.getString("ques2") != null) qaContainer.getChildren().add(createQABox(rs.getString("ques2"), rs.getString("rep2")));
                    if (rs.getString("ques3") != null) qaContainer.getChildren().add(createQABox(rs.getString("ques3"), rs.getString("rep3")));
                    if (rs.getString("ques4") != null) qaContainer.getChildren().add(createQABox(rs.getString("ques4"), rs.getString("rep4")));

                    // --- NEW: Add Edit/Delete Buttons for the Responses ---
                    HBox actionBox = new HBox(15);
                    actionBox.setAlignment(Pos.CENTER_RIGHT);
                    actionBox.setPadding(new Insets(15, 0, 0, 0));

                    Button btnEditRes = new Button("✎ Modifier");
                    btnEditRes.setStyle("-fx-background-color: transparent; -fx-border-color: #444455; -fx-text-fill: white; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
                    btnEditRes.setOnAction(e -> openReponseScreen());

                    Button btnDeleteRes = new Button("🗑 Supprimer");
                    btnDeleteRes.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3b3f; -fx-text-fill: #ff3b3f; -fx-border-radius: 6; -fx-cursor: hand; -fx-padding: 8 20;");
                    btnDeleteRes.setOnAction(e -> deleteResponses());

                    actionBox.getChildren().addAll(btnEditRes, btnDeleteRes);
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

            ReponseCreateController formController = loader.getController();
            formController.initData(currentAgent); // Passes agent so form pre-fills with old answers!

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

                // Immediately refresh the view to show the empty "Remplir" state
                loadQuestionnaireData();
            } catch (Exception e) {
                System.err.println("Erreur suppression: " + e.getMessage());
            }
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

        Label lblA = new Label(answer != null ? answer : "Aucune réponse");
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
        aiBanner.setVisible(false);
        aiBanner.setManaged(false);

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

        Button btnRemplir = new Button("✎ Remplir le Questionnaire");
        btnRemplir.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3344; -fx-text-fill: #ff3344; -fx-border-radius: 6; -fx-padding: 8 20; -fx-cursor: hand;");

        btnRemplir.setOnAction(e -> openReponseScreen());

        emptyBox.getChildren().addAll(lblIcon, lblTitle, lblDesc, btnRemplir);
        qaContainer.getChildren().add(emptyBox);
    }
}