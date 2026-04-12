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

    @FXML private Button btnCreateAgent;
    @FXML private TextField tfSearch;
    @FXML private TableView<Agent> agentTable;

    @FXML private TableColumn<Agent, String> colPseudo;
    @FXML private TableColumn<Agent, String> colJeu;
    @FXML private TableColumn<Agent, String> colRang;
    @FXML private TableColumn<Agent, String> colStatut;
    @FXML private TableColumn<Agent, String> colActions; // This was missing before!

    private AgentService serviceAgent;
    private ObservableList<Agent> agentObservableList;

    @FXML
    public void initialize() {
        serviceAgent = new AgentService();

        // 1. Tell columns where to get their data
        colPseudo.setCellValueFactory(new PropertyValueFactory<>("pseudo"));
        colJeu.setCellValueFactory(new PropertyValueFactory<>("game"));
        colRang.setCellValueFactory(new PropertyValueFactory<>("rank"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 2. Dynamic Colors for Status
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

        // 3. Setup Action Buttons (Modifier & Supprimer)
        colActions.setCellFactory(column -> new TableCell<Agent, String>() {
            final Button btnModifier = new Button("Modifier");
            final Button btnSupprimer = new Button("Supprimer");
            final HBox actionButtons = new HBox(10, btnModifier, btnSupprimer);

            {
                btnModifier.setStyle("-fx-background-color: #2a2a35; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 4;");
                btnSupprimer.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3b3f; -fx-text-fill: #ff3b3f; -fx-cursor: hand; -fx-border-radius: 4;");

                btnModifier.setOnAction(event -> {
                    Agent selectedAgent = getTableView().getItems().get(getIndex());
                    openEditScreen(selectedAgent);
                });

                btnSupprimer.setOnAction(event -> {
                    Agent selectedAgent = getTableView().getItems().get(getIndex());
                    serviceAgent.deleteAgent(selectedAgent.getId());
                    loadAgents(); // Refresh the table
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(actionButtons);
                }
                setStyle("-fx-border-width: 0;");
            }
        });

        // 4. Load Data into Table
        loadAgents();

        // 5. Setup the "Créer un Agent +" Button Navigation
        btnCreateAgent.setOnAction(event -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/AgentCreate.fxml"));
                btnCreateAgent.getScene().setRoot(root);
            } catch (IOException e) {
                System.err.println("Erreur navigation création: " + e.getMessage());
            }
        });
    }

    private void loadAgents() {
        try {
            List<Agent> agentsFromDB = serviceAgent.getAllAgents();
            agentObservableList = FXCollections.observableArrayList(agentsFromDB);
            agentTable.setItems(agentObservableList);
        } catch (Exception e) {
            System.err.println("Error loading agents: " + e.getMessage());
        }
    }

    // This method was missing from your code! It opens the Edit screen.
    private void openEditScreen(Agent agent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AgentEdit.fxml"));
            Parent root = loader.load();

            // Pass the data!
            AgentEditController editController = loader.getController();
            editController.initData(agent);

            // Change scene
            btnCreateAgent.getScene().setRoot(root);
        } catch (IOException e) {
            System.err.println("Erreur de navigation vers Edit : " + e.getMessage());
            e.printStackTrace();
        }
    }
}