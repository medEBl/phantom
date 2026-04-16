package Controllers.team;

import entities.team.Team;
import Iservices.team.ITeamService;
import javafx.stage.Modality;
import services.team.TeamService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Optional;

public class DashboardTeamsController implements IDashboardTeamController {
    
    private final ITeamService teamService = new TeamService();
    private ObservableList<Team> teamsList;
    
    @FXML
    private TableView<Team> teamsTableView;
    
    @FXML
    private TableColumn<Team, Integer> idColumn;
    
    @FXML
    private TableColumn<Team, String> nameColumn;
    
    @FXML
    private TableColumn<Team, String> gameColumn;
    
    @FXML
    private TableColumn<Team, Integer> coachColumn;
    
    @FXML
    private TableColumn<Team, Void> actionsColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> gameFilterComboBox;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button createTeamButton;
    
    @FXML
    private Button editTeamButton;
    
    @FXML
    private Button deleteTeamButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label totalTeamsLabel;
    
    @FXML
    private Label activeTeamsLabel;
    
    @FXML
    private Label gamesLabel;
    
    @FXML
    private Label coachesLabel;
    
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadTeams();
        setupTableSelection();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("game"));
        coachColumn.setCellValueFactory(new PropertyValueFactory<>("coachId"));
        
        // Add action buttons to each row
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                viewButton.getStyleClass().addAll("table-action-button", "view");
                editButton.getStyleClass().addAll("table-action-button", "edit");
                deleteButton.getStyleClass().addAll("table-action-button", "delete");
                
                viewButton.setOnAction(e -> handleViewTeam(getTableView().getItems().get(getIndex())));
                editButton.setOnAction(e -> handleEditTeam(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(e -> handleDeleteTeam(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5);
                    buttons.getChildren().addAll(viewButton, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void setupFilters() {
        List<String> games = List.of("All", "League of Legends", "Valorant", "CS:GO", "Dota 2", "Overwatch", "FIFA", "Rocket League");
        gameFilterComboBox.setItems(FXCollections.observableArrayList(games));
        gameFilterComboBox.setValue("All");
        gameFilterComboBox.setOnAction(e -> filterTeams());
        
        searchField.textProperty().addListener((obs, old, newVal) -> filterTeams());
    }
    
    private void setupTableSelection() {
        teamsTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editTeamButton.setDisable(newSelection == null);
                deleteTeamButton.setDisable(newSelection == null);
            });
    }
    
    private void loadTeams() {
        try {
            List<Team> teams = teamService.getAllTeams();
            teamsList = FXCollections.observableArrayList(teams);
            teamsTableView.setItems(teamsList);
            updateStats();
        } catch (Exception e) {
            showError("Error loading teams: " + e.getMessage());
        }
    }
    
    private void filterTeams() {
        String searchText = searchField.getText().toLowerCase();
        String selectedGame = gameFilterComboBox.getValue();
        
        ObservableList<Team> filteredList = FXCollections.observableArrayList();
        
        for (Team team : teamsList) {
            boolean matchesSearch = searchText.isEmpty() || 
                team.getName().toLowerCase().contains(searchText) ||
                team.getGame().toLowerCase().contains(searchText);
            
            boolean matchesGame = "All".equals(selectedGame) || 
                team.getGame().equals(selectedGame);
            
            if (matchesSearch && matchesGame) {
                filteredList.add(team);
            }
        }
        
        teamsTableView.setItems(filteredList);
        updateStats();
    }
    
    private void updateStats() {
        int totalTeams = teamsTableView.getItems().size();
        int activeTeams = totalTeams; // Assuming all teams are active
        int uniqueGames = (int) teamsTableView.getItems().stream()
                .map(Team::getGame)
                .distinct()
                .count();
        int uniqueCoaches = (int) teamsTableView.getItems().stream()
                .map(Team::getCoachId)
                .distinct()
                .count();
        
        totalTeamsLabel.setText(String.valueOf(totalTeams));
        activeTeamsLabel.setText(String.valueOf(activeTeams));
        gamesLabel.setText(String.valueOf(uniqueGames));
        coachesLabel.setText(String.valueOf(uniqueCoaches));
    }
    
    @FXML
    private void handleCreateTeam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/dashboardCreateTeam.fxml"));
            Parent root = loader.load();
            
            DashboardTeamCreateController controller = loader.getController();
            controller.setTeamsController(this);
            
            Stage stage = (Stage) createTeamButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Create New Team - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error opening create team screen: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditTeam() {
        Team selectedTeam = teamsTableView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showWarning("Please select a team to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/dashboardEditTeam.fxml"));
            Parent root = loader.load();
            
            DashboardTeamEditController controller = loader.getController();
            controller.setTeam(selectedTeam);
            controller.setTeamsController(this);
            
            Stage stage = (Stage) editTeamButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Edit Team - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error opening edit team screen: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteTeam() {
        Team selectedTeam = teamsTableView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showWarning("Please select a team to delete.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/dashboardDeleteTeam.fxml"));
            Parent root = loader.load();
            
            DashboardTeamDeleteController controller = loader.getController();
            controller.setTeam(selectedTeam);
            controller.setTeamsController(this);
            
            Stage stage = (Stage) deleteTeamButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Delete Team - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error opening delete team screen: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditTeam(Team team) {
        // This method handles edit requests from table row buttons
        // Redirect to the table-based edit method
        handleEditTeam();
    }
    
    @FXML
    private void handleDeleteTeam(Team team) {
        // This method handles delete requests from table row buttons
        // Redirect to the table-based delete method
        handleDeleteTeam();
    }
    
    @FXML
    private void handleViewTeam(Team team) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/view.fxml"));
            Parent root = loader.load();
            
            TeamViewController controller = loader.getController();
            controller.setTeam(team);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Team Details - Phantom App");
            stage.showAndWait();
        } catch (Exception e) {
            showError("Error opening team details: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadTeams();
        searchField.clear();
        gameFilterComboBox.setValue("All");
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Dashboard - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error returning to dashboard: " + e.getMessage());
        }
    }
    
    @Override
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void setTeamController(IDashboardTeamController teamController) {
        // This method is required by the interface but not used in the dashboard controller
        // The dashboard controller manages its own state
    }
    
    @Override
    public void refreshTeams() {
        loadTeams();
    }
}
