package Controllers.agent;

import entities.agent.Agent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.agent.AgentService;
import tools.Phantom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class AgentListController {

    @FXML private Button btnNavQuestionnaires;
    @FXML private Button btnDisconnect;
    @FXML private Button btnCreateAgent;
    @FXML private TextField tfSearch;
    @FXML private TableView<Agent> agentTable;

    @FXML private TableColumn<Agent, String> colPseudo;
    @FXML private TableColumn<Agent, String> colJeu;
    @FXML private TableColumn<Agent, String> colRang;
    @FXML private TableColumn<Agent, String> colStatut;
    @FXML private TableColumn<Agent, String> colActions;

    private AgentService serviceAgent;
    private ObservableList<Agent> agentObservableList;

    @FXML
    public void initialize() {
        serviceAgent = new AgentService();

        colPseudo.setCellValueFactory(new PropertyValueFactory<>("pseudo"));
        colJeu.setCellValueFactory(new PropertyValueFactory<>("game"));
        colRang.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Thème des textes de statut
        colStatut.setCellFactory(column -> new TableCell<Agent, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toUpperCase());
                    if (status.equalsIgnoreCase("active")) {
                        setStyle("-fx-text-fill: #00ffff; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                    } else if (status.equalsIgnoreCase("banned")) {
                        setStyle("-fx-text-fill: #ff3b3f; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #8a8a98; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

        // Configuration des boutons d'action (Thème Phantom)
        colActions.setCellFactory(column -> new TableCell<Agent, String>() {

            final Button btnDetails = new Button("👁 Profil");
            final Button btnRemplir = new Button("📝 Remplir");
            final Label lblComplete = new Label("✅ Complété");
            final Label lblNonRempli = new Label("❌ Non rempli"); // Admin text for missing questionnaire

            // On utilise juste des icônes pour gagner de la place !
            final Button btnModifier = new Button("✎");
            final Button btnSupprimer = new Button("🗑");

            {
                // Application stricte de votre palette de couleurs
                btnDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #00ffff; -fx-text-fill: #00ffff; -fx-cursor: hand; -fx-border-radius: 4; -fx-padding: 5 10;");

                // Remplir (Rouge) vs Complété (Vert) vs Non Rempli (Gris pour Admin)
                btnRemplir.setStyle("-fx-background-color: #ff3b3f; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 5 10;");
                lblComplete.setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold; -fx-padding: 5 10;");
                lblNonRempli.setStyle("-fx-text-fill: #8a8a98; -fx-font-weight: bold; -fx-padding: 5 10;");

                // Boutons d'édition (Plus petits)
                btnModifier.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4; -fx-padding: 5 10;");
                btnSupprimer.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3b3f; -fx-text-fill: #ff3b3f; -fx-cursor: hand; -fx-border-radius: 4; -fx-padding: 5 10;");

                btnDetails.setOnAction(event -> openDetailsScreen(getTableView().getItems().get(getIndex())));
                btnRemplir.setOnAction(event -> openReponseScreen(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(event -> openEditScreen(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(event -> {
                    serviceAgent.deleteAgent(getTableView().getItems().get(getIndex()).getId());
                    loadAgents();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Agent agent = getTableView().getItems().get(getIndex());
                    HBox actionBox = new HBox(10);
                    actionBox.setAlignment(Pos.CENTER);

                    // Determine if we are in Admin Mode based on the presence of the sidebar buttons
                    boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
                    boolean aRepondu = checkReponseExists(agent.getId());

                    if (isAdmin) {
                        // BACK-OFFICE (Admin)
                        if (aRepondu) {
                            actionBox.getChildren().addAll(lblComplete, btnDetails, btnModifier, btnSupprimer);
                        } else {
                            actionBox.getChildren().addAll(lblNonRempli, btnDetails, btnModifier, btnSupprimer);
                        }
                    } else {
                        // FRONT-OFFICE (User)
                        if (aRepondu) {
                            actionBox.getChildren().addAll(lblComplete, btnDetails, btnModifier, btnSupprimer);
                        } else {
                            actionBox.getChildren().addAll(btnRemplir, btnDetails, btnModifier, btnSupprimer);
                        }
                    }

                    setGraphic(actionBox);
                }
            }
        });

        // RESTORED CODE: Actually load the data into the table
        loadAgents();

        // RESTORED CODE: Setup buttons
        if (btnCreateAgent != null) {
            btnCreateAgent.setOnAction(event -> {
                try {
                    boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
                    String fxmlPath = isAdmin ? "/fxml/AgentCreateBack.fxml" : "/fxml/AgentCreate.fxml";
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    AgentCreateController createController = loader.getController();
                    createController.setAdminMode(isAdmin);
                    btnCreateAgent.getScene().setRoot(root);
                } catch (IOException e) { e.printStackTrace(); }
            });
        }

        if (btnNavQuestionnaires != null) {
            btnNavQuestionnaires.setOnAction(e -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListQuestionnaires.fxml"));
                    btnNavQuestionnaires.getScene().setRoot(root);
                } catch (IOException ex) { ex.printStackTrace(); }
            });
        }
    } // <-- This closing bracket was missing

    private void loadAgents() {
        try {
            List<Agent> agentsFromDB = serviceAgent.getAllAgents();
            agentObservableList = FXCollections.observableArrayList(agentsFromDB);
            agentTable.setItems(agentObservableList);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- REQUÊTE POUR SAVOIR SI LE JOUEUR A RÉPONDU ---
    private boolean checkReponseExists(int agentId) {
        String sql = "SELECT 1 FROM reponse_questionnaire WHERE id_agent = ?";
        try {
            Connection cnx = Phantom.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(sql);

            ps.setInt(1, agentId);
            ResultSet rs = ps.executeQuery();

            boolean exists = rs.next();

            rs.close();
            ps.close();

            return exists;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- METHODES DE NAVIGATION ---

    private void openReponseScreen(Agent agent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReponseCreate.fxml"));
            Parent root = loader.load();
            Controllers.reponse.ReponseCreateController controller = loader.getController(); // <--- CORRECT PACKAGE
            controller.initData(agent);
            agentTable.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openEditScreen(Agent agent) {
        try {
            boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
            String fxmlPath = isAdmin ? "/fxml/AgentEditBack.fxml" : "/fxml/AgentEdit.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            AgentEditController editController = loader.getController();
            editController.initData(agent);
            agentTable.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openDetailsScreen(Agent agent) {
        try {
            boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
            String fxmlPath = isAdmin ? "/fxml/AgentDetailsBack.fxml" : "/fxml/AgentDetails.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            AgentDetailsController detailsController = loader.getController();
            detailsController.initData(agent);
            agentTable.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}