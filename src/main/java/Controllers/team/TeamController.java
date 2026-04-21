package Controllers.team;

import entities.team.Team;
import entities.user.User;
import javafx.stage.Modality;
import Iservices.team.ITeamService;
import services.team.TeamService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class TeamController implements IDashboardTeamController {
    
    private final ITeamService teamService = new TeamService();
    private ObservableList<Team> teamList = FXCollections.observableArrayList();
    private User currentUser;
    
    @FXML
    private TableView<Team> teamTableView;
    
    @FXML
    private TableColumn<Team, Integer> idColumn;
    
    @FXML
    private TableColumn<Team, String> nameColumn;
    
    @FXML
    private TableColumn<Team, String> gameColumn;
    
    @FXML
    private TableColumn<Team, LocalDateTime> creationDateColumn;
    
    @FXML
    private TableColumn<Team, String> coachNameColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> gameFilterComboBox;
    
    @FXML
    private Label totalTeamsLabel;
    
    @FXML
    private Button createTeamButton;
    
    @FXML
    private Button editTeamButton;
    
    @FXML
    private Button deleteTeamButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button backButton;
    
    public void initialize() {
        setupTableColumns();
        setupGameFilter();
        loadTeams();
        setupTableSelection();
        setupSearchListener();
    }
    
        
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("game"));
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        coachNameColumn.setCellValueFactory(new PropertyValueFactory<>("coachName"));
        
        // Format the creation date
        creationDateColumn.setCellFactory(column -> new TableCell<Team, LocalDateTime>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
    }
    
    private void setupGameFilter() {
        List<String> games = List.of("All", "League of Legends", "Valorant", "CS:GO", "Dota 2", "Overwatch");
        gameFilterComboBox.setItems(FXCollections.observableArrayList(games));
        gameFilterComboBox.setValue("All");
        gameFilterComboBox.setOnAction(e -> filterTeams());
    }
    
    private void setupTableSelection() {
        teamTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editTeamButton.setDisable(newSelection == null);
                deleteTeamButton.setDisable(newSelection == null);
            });
    }
    
    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldText, newText) -> filterTeams());
    }
    
    public void loadTeams() {
        try {
            List<Team> teams = teamService.getAllTeams();
            teamList.clear();
            teamList.addAll(teams);
            teamTableView.setItems(teamList);
            updateStats();
        } catch (Exception e) {
            showError("Error loading teams: " + e.getMessage());
        }
    }
    
    public ITeamService getTeamService() {
        return teamService;
    }
    
    private void filterTeams() {
        String searchText = searchField.getText().toLowerCase();
        String selectedGame = gameFilterComboBox.getValue();
        
        ObservableList<Team> filteredList = FXCollections.observableArrayList();
        
        for (Team team : teamList) {
            boolean matchesSearch = team.getName().toLowerCase().contains(searchText) ||
                                   team.getCoachName().toLowerCase().contains(searchText);
            
            boolean matchesGame = "All".equals(selectedGame) || 
                                selectedGame.equals(team.getGame());
            
            if (matchesSearch && matchesGame) {
                filteredList.add(team);
            }
        }
        
        teamTableView.setItems(filteredList);
        updateStats();
    }
    
    private void updateStats() {
        int totalTeams = teamTableView.getItems().size();
        totalTeamsLabel.setText("Total Teams: " + totalTeams);
    }
    
    @FXML
    private void handleCreateTeam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/create.fxml"));
            Parent root = loader.load();
            
            TeamCreateController controller = loader.getController();
            controller.setTeamController(this);
            
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
        Team selectedTeam = teamTableView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showWarning("Please select a team to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/edit.fxml"));
            Parent root = loader.load();
            
            TeamEditController controller = loader.getController();
            controller.setTeam(selectedTeam);
            controller.setTeamController(this);
            
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
        Team selectedTeam = teamTableView.getSelectionModel().getSelectedItem();
        if (selectedTeam == null) {
            showWarning("Please select a team to delete.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/delete.fxml"));
            Parent root = loader.load();
            
            TeamDeleteController controller = loader.getController();
            controller.setTeam(selectedTeam);
            controller.setTeamController(this);
            
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
    private void handleRefresh() {
        loadTeams();
        searchField.clear();
        gameFilterComboBox.setValue("All");
    }
    
    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
            Parent root = loader.load();
            
            // Pass current user back to home controller
            Controllers.home.HomeController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Home - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error returning to home: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewTeam(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Team selectedTeam = teamTableView.getSelectionModel().getSelectedItem();
            if (selectedTeam != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/view.fxml"));
                    Parent root = loader.load();
                    
                    TeamViewController controller = loader.getController();
                    controller.setTeam(selectedTeam);
                    
                    Stage stage = new Stage();
                    stage.setTitle("Team Details: " + selectedTeam.getName());
                    stage.setScene(new Scene(root, 600, 500));
                    stage.setResizable(false);
                    stage.show();
                } catch (Exception e) {
                    showError("Error opening team details: " + e.getMessage());
                }
            }
        }
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
    public void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setTeamController(IDashboardTeamController teamController) {
        // This method is required by the interface but not used in the original TeamController
        // The dashboard controllers will handle their own controller references
    }
    
    @Override
    public void refreshTeams() {
        loadTeams();
    }
}
