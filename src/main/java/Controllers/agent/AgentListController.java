package Controllers.agent;

import entities.agent.Agent;
import entities.user.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
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

/**
 * Controller for the Agent List view.
 */
public class AgentListController {

    @FXML private Button btnNavQuestionnaires;
    @FXML private Button btnDisconnect;
    @FXML private Button btnCreateAgent;
    @FXML private Button btnBack;
    @FXML private Button btnBackToHome;
    @FXML private TextField tfSearch;
    @FXML private TableView<Agent> agentTable;

    @FXML private TableColumn<Agent, String> colPseudo;
    @FXML private TableColumn<Agent, String> colJeu;
    @FXML private TableColumn<Agent, String> colRang;
    @FXML private TableColumn<Agent, String> colStatut;
    @FXML private TableColumn<Agent, String> colActions;

    private AgentService serviceAgent;
    private ObservableList<Agent> agentObservableList;
    private FilteredList<Agent> filteredData; // Used for search
    private User currentUser;

    @FXML
    public void initialize() {
        serviceAgent = new AgentService();

        // 1. Initialize the lists for Search and Sort
        agentObservableList = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(agentObservableList, b -> true);

        // 2. Wrap the FilteredList in a SortedList
        SortedList<Agent> sortedData = new SortedList<>(filteredData);

        // 3. Bind the SortedList comparator to the TableView comparator
        // (This enables sorting when clicking column headers!)
        sortedData.comparatorProperty().bind(agentTable.comparatorProperty());

        // 4. Add sorted (and filtered) data to the table
        agentTable.setItems(sortedData);

        // 5. Setup Search Bar Listener
        setupSearchFilter();

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

        // Configuration des boutons d'action (Thème Phantom avec Tooltips)
        colActions.setCellFactory(column -> new TableCell<Agent, String>() {

            // 1. TEXT BUTTONS (Plus clair et lisible)
            final Button btnDetails = new Button("VIEW");
            final Button btnRemplir = new Button("FILL");
            final Label lblComplete = new Label("DONE");
            final Label lblNonRempli = new Label("TODO");
            final Button btnModifier = new Button("EDIT");
            final Button btnSupprimer = new Button("DELETE");

            {
                // 2. AJOUT DES TOOLTIPS (Pour la compréhension de l'utilisateur)
                btnDetails.setTooltip(new Tooltip("Voir les détails"));
                btnRemplir.setTooltip(new Tooltip("Remplir le questionnaire"));
                lblComplete.setTooltip(new Tooltip("Questionnaire rempli"));
                lblNonRempli.setTooltip(new Tooltip("Questionnaire non rempli"));
                btnModifier.setTooltip(new Tooltip("Modifier le profil"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer l'agent"));

                // 3. STYLES (Utilisation de votre dark-theme.css + ajustements)
                btnDetails.getStyleClass().addAll("btn-action-small", "btn-profil");
                btnModifier.getStyleClass().addAll("btn-action-small", "btn-edit");
                btnSupprimer.getStyleClass().addAll("btn-action-small", "btn-delete");

                // Style spécifique pour le bouton Remplir et les labels de statut
                btnRemplir.getStyleClass().add("btn-action-small");
                btnRemplir.setStyle("-fx-border-color: #ff3b3f; -fx-text-fill: #ff3b3f;");
                lblComplete.setStyle("-fx-text-fill: #2dff8b; -fx-font-size: 14px; -fx-padding: 4 5;");
                lblNonRempli.setStyle("-fx-text-fill: #8a8a98; -fx-font-size: 14px; -fx-padding: 4 5;");

                // 4. ACTIONS DES BOUTONS
                btnDetails.setOnAction(event -> openDetailsScreen(getTableView().getItems().get(getIndex())));
                btnRemplir.setOnAction(event -> openReponseScreen(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(event -> openEditScreen(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(event -> {
                    serviceAgent.deleteAgent(getTableView().getItems().get(getIndex()).getId());
                    loadAgents(); // Recharge les données
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Agent agent = getTableView().getItems().get(getIndex());

                    // Espacement réduit à 8px (au lieu de 10) pour bien rentrer dans la colonne
                    HBox actionBox = new HBox(8);
                    actionBox.setAlignment(Pos.CENTER);

                    boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
                    boolean aRepondu = checkReponseExists(agent.getId());

                    // Logique de rendu (Garde votre logique d'affichage Admin/User intacte !)
                    if (isAdmin) {
                        if (aRepondu) {
                            actionBox.getChildren().addAll(lblComplete, btnDetails, btnModifier, btnSupprimer);
                        } else {
                            actionBox.getChildren().addAll(lblNonRempli, btnDetails, btnModifier, btnSupprimer);
                        }
                    } else {
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

        // Charger les données de la base
        loadAgents();

        // Setup boutons existants
        if (btnCreateAgent != null) {
            btnCreateAgent.setOnAction(event -> {
                System.out.println("=== CREATE AGENT BUTTON CLICKED ===");
                try {
                    boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
                    String fxmlPath = isAdmin ? "/agent/fxml/AgentCreateBack.fxml" : "/agent/fxml/AgentCreate.fxml";
                    System.out.println("Loading FXML: " + fxmlPath);
                    System.out.println("Resource URL: " + getClass().getResource(fxmlPath));
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();
                    AgentCreateController createController = loader.getController();
                    createController.setAdminMode(isAdmin);
                    btnCreateAgent.getScene().setRoot(root);
                } catch (IOException e) {
                    System.out.println("ERROR loading FXML: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        if (btnBack != null) {
            btnBack.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
                    Parent root = loader.load();

                    // Get the dashboard controller and pass current user context
                    Controllers.admin.DashboardController dashboardController = loader.getController();
                    if (dashboardController != null) {
                        dashboardController.setCurrentUser(currentUser); // Pass the current user context
                        System.out.println("Passed user context back to dashboard controller: " + (currentUser != null ? currentUser.getFullName() : "null"));
                    }

                    btnBack.getScene().setRoot(root);
                } catch (IOException e) {
                    System.err.println("ERROR loading dashboard screen: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        if (btnBackToHome != null) {
            btnBackToHome.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
                    Parent root = loader.load();

                    // Get the home controller and pass current user context
                    Controllers.home.HomeController homeController = loader.getController();
                    if (homeController != null) {
                        homeController.setCurrentUser(currentUser); // Pass the current user context
                        System.out.println("Passed user context back to home controller: " + (currentUser != null ? currentUser.getFullName() : "null"));
                    }

                    btnBackToHome.getScene().setRoot(root);

                } catch (IOException e) {
                    System.err.println("ERROR loading home screen: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }

        if (btnNavQuestionnaires != null) {
            btnNavQuestionnaires.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/agent/fxml/ListQuestionnaires.fxml"));
                    Parent root = loader.load();
                    btnNavQuestionnaires.getScene().setRoot(root);
                } catch (IOException ex) { ex.printStackTrace(); }
            });
        }
    }

    // --- RECHERCHE EN TEMPS RÉEL (PSEUDO SEULEMENT) ---
    private void setupSearchFilter() {
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(agent -> {
                    // Si le champ est vide, on affiche tout
                    if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                        return true;
                    }

                    // On met tout en minuscules pour comparer facilement
                    String lowerCaseFilter = newValue.toLowerCase();

                    // Recherche UNIQUEMENT par Pseudo
                    if (agent.getPseudo() != null && agent.getPseudo().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }

                    // Si le pseudo ne correspond pas, on cache la ligne
                    return false;
                });
            });
        }
    }

    private void loadAgents() {
        try {
            List<Agent> agentsFromDB = serviceAgent.getAllAgents();
            // On met à jour la liste source. Le FilteredList et SortedList se mettront à jour tout seuls !
            agentObservableList.setAll(agentsFromDB);
        } catch (Exception e) { e.printStackTrace(); }
    }

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

    private void openReponseScreen(Agent agent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/agent/fxml/ReponseCreate.fxml"));
            Parent root = loader.load();
            Controllers.reponse.ReponseCreateController controller = loader.getController();
            controller.initData(agent);
            agentTable.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openEditScreen(Agent agent) {
        try {
            boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
            String fxmlPath = isAdmin ? "/agent/fxml/AgentEditBack.fxml" : "/agent/fxml/AgentEdit.fxml";
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
            String fxmlPath = isAdmin ? "/agent/fxml/AgentDetailsBack.fxml" : "/agent/fxml/AgentDetails.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            AgentDetailsController detailsController = loader.getController();
            detailsController.initData(agent);
            agentTable.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("AgentListController.setCurrentUser called with user: " + (user != null ? user.getFullName() : "null"));
        System.out.println("User role: " + (user != null ? user.getRole() : "null"));
    }
}