package Controllers.agent;

import entities.agent.Agent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.agent.AgentService;

import java.io.IOException;
import java.util.List;

public class AgentListController {

    // --- Sidebar Buttons (Back-Office Only) ---
    @FXML private Button btnNavQuestionnaires;
    @FXML private Button btnDisconnect;

    // --- Main Content ---
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

        // 1. Mapping des colonnes
        colPseudo.setCellValueFactory(new PropertyValueFactory<>("pseudo"));
        colJeu.setCellValueFactory(new PropertyValueFactory<>("game"));
        colRang.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Couleurs dynamiques pour le statut
        colStatut.setCellFactory(column -> new TableCell<Agent, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.equalsIgnoreCase("active")) {
                        setStyle("-fx-text-fill: #00e676; -fx-font-weight: bold; -fx-border-width: 0;");
                    } else if (status.equalsIgnoreCase("banned")) {
                        setStyle("-fx-text-fill: #ff3344; -fx-font-weight: bold; -fx-border-width: 0;");
                    } else {
                        setStyle("-fx-text-fill: #8a8a98; -fx-border-width: 0;");
                    }
                }
            }
        });

        // 3. Configuration des boutons d'actionF
        colActions.setCellFactory(column -> new TableCell<Agent, String>() {
            final Button btnDetails = new Button("Voir Profil");
            final Button btnModifier = new Button("Modifier");
            final Button btnSupprimer = new Button("Supprimer");
            final HBox actionButtons = new HBox(10, btnDetails, btnModifier, btnSupprimer);

            {
                btnDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #4da6ff; -fx-text-fill: #4da6ff; -fx-cursor: hand; -fx-border-radius: 4;");
                btnModifier.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
                btnSupprimer.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3b3f; -fx-text-fill: #ff3b3f; -fx-cursor: hand; -fx-border-radius: 4;");
                actionButtons.setStyle("-fx-alignment: center;");

                btnDetails.setOnAction(event -> {
                    Agent selectedAgent = getTableView().getItems().get(getIndex());
                    openDetailsScreen(selectedAgent);
                });

                btnModifier.setOnAction(event -> {
                    Agent selectedAgent = getTableView().getItems().get(getIndex());
                    openEditScreen(selectedAgent);
                });

                btnSupprimer.setOnAction(event -> {
                    Agent selectedAgent = getTableView().getItems().get(getIndex());
                    serviceAgent.deleteAgent(selectedAgent.getId());
                    loadAgents();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(actionButtons);
                }
                setStyle("-fx-border-width: 0;");
            }
        });

        // 4. Chargement des données
        loadAgents();

        // 5. Navigation "Créer un Agent"
        if (btnCreateAgent != null) {
            btnCreateAgent.setOnAction(event -> {
                try {
                    // 1. Détection de l'endroit où l'on se trouve
                    boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);
                    String fxmlPath = isAdmin ? "/fxml/AgentCreateBack.fxml" : "/fxml/AgentCreate.fxml";

                    // 2. Chargement du fichier
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                    Parent root = loader.load();

                    // 3. --- L'INJECTION DE SÉCURITÉ ---
                    AgentCreateController createController = loader.getController();
                    createController.setAdminMode(isAdmin); // On force l'info !

                    // 4. Affichage
                    btnCreateAgent.getScene().setRoot(root);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        // 6. Navigation Sidebar (Back-Office)
        if (btnNavQuestionnaires != null) {
            btnNavQuestionnaires.setOnAction(e -> {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListQuestionnaires.fxml"));
                    btnNavQuestionnaires.getScene().setRoot(root);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (btnDisconnect != null) {
            btnDisconnect.setOnAction(e -> System.out.println("Déconnexion !"));
        }
    }

    private void loadAgents() {
        try {
            List<Agent> agentsFromDB = serviceAgent.getAllAgents();
            agentObservableList = FXCollections.observableArrayList(agentsFromDB);
            agentTable.setItems(agentObservableList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- METHODES DE NAVIGATION ---

    private void openEditScreen(Agent agent) {
        try {
            // Détection robuste : Si l'un des boutons de la barre latérale est présent, on est en Admin !
            boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);

            // Choix dynamique du fichier FXML
            String fxmlPath = isAdmin ? "/fxml/AgentEditBack.fxml" : "/fxml/AgentEdit.fxml";

            // Petite trace pour vous aider à débugger dans la console
            System.out.println("Navigation vers Modifier. Mode Admin ? " + isAdmin + " -> Charge: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // On passe les données au contrôleur
            AgentEditController editController = loader.getController();
            editController.initData(agent);

            // On change l'écran
            agentTable.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers Edit : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openDetailsScreen(Agent agent) {
        try {
            // Détection robuste pour les détails aussi
            boolean isAdmin = (btnNavQuestionnaires != null || btnDisconnect != null);

            // Choix dynamique du fichier FXML
            String fxmlPath = isAdmin ? "/fxml/AgentDetailsBack.fxml" : "/fxml/AgentDetails.fxml";

            System.out.println("Navigation vers Details. Mode Admin ? " + isAdmin + " -> Charge: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            AgentDetailsController detailsController = loader.getController();
            detailsController.initData(agent);

            agentTable.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers Details : " + e.getMessage());
            e.printStackTrace();
        }
    }
}