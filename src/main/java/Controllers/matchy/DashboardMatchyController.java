package Controllers.matchy;

import entities.matchy.Matchy;
import entities.team.Team;
import entities.user.User;
import Iservices.matchy.IMatchyService;
import Iservices.team.ITeamService;
import javafx.stage.Modality;
import services.matchy.MatchyService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class DashboardMatchyController implements IDashboardMatchyController {
    
    private final IMatchyService matchyService = new MatchyService();
    private final ITeamService teamService = new TeamService();
    private ObservableList<Matchy> matchyList;
    private User currentUser;
    
    @FXML
    private TableView<Matchy> matchyTableView;
    
    @FXML
    private TableColumn<Matchy, Integer> idColumn;
    
    @FXML
    private TableColumn<Matchy, String> gameColumn;
    
    @FXML
    private TableColumn<Matchy, LocalDateTime> matchDateColumn;
    
    @FXML
    private TableColumn<Matchy, String> team1Column;
    
    @FXML
    private TableColumn<Matchy, String> team2Column;
    
    @FXML
    private TableColumn<Matchy, String> scoreColumn;
    
    @FXML
    private TableColumn<Matchy, String> locationColumn;
    
    @FXML
    private TableColumn<Matchy, String> winnerColumn;
    
    @FXML
    private TableColumn<Matchy, String> statusColumn;
    
    @FXML
    private TableColumn<Matchy, Void> actionsColumn;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> gameFilterComboBox;
    
    @FXML
    private ComboBox<String> statusFilterComboBox;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button createMatchButton;
    
    @FXML
    private Button editMatchButton;
    
    @FXML
    private Button deleteMatchButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Label totalMatchesLabel;
    
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadMatchesData();
        setupTableSelection();
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("game"));
        matchDateColumn.setCellValueFactory(new PropertyValueFactory<>("matchDate"));
        team1Column.setCellValueFactory(new PropertyValueFactory<>("team1Name"));
        team2Column.setCellValueFactory(new PropertyValueFactory<>("team2Name"));
        scoreColumn.setCellValueFactory(new PropertyValueFactory<>("score"));
        
        // Custom cell factory for score display
        scoreColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    Matchy match = getTableRow().getItem();
                    if (match.getScoreTeam1() != null && match.getScoreTeam2() != null) {
                        setText(match.getScoreTeam1() + " - " + match.getScoreTeam2());
                    } else {
                        setText("N/A");
                    }
                }
            }
        });
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        winnerColumn.setCellValueFactory(new PropertyValueFactory<>("winnerTeamName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format date column
        matchDateColumn.setCellFactory(column -> new TableCell<>() {
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
        
        // Color code status column
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status.toLowerCase()) {
                        case "planned":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        case "ongoing":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "finished":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                            break;
                        case "cancelled":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #ffffff;");
                    }
                }
            }
        });
        
        // Add action buttons to each row
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            
            {
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                
                viewButton.setOnAction(e -> {
                    Matchy match = getTableView().getItems().get(getIndex());
                    handleViewMatch(match);
                });
                editButton.setOnAction(e -> {
                    Matchy match = getTableView().getItems().get(getIndex());
                    handleEditMatch(match);
                });
                deleteButton.setOnAction(e -> {
                    Matchy match = getTableView().getItems().get(getIndex());
                    handleDeleteMatch(match);
                });
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
        gameFilterComboBox.setOnAction(e -> filterMatches());
        
        List<String> statuses = List.of("All", "planned", "ongoing", "finished", "cancelled");
        statusFilterComboBox.setItems(FXCollections.observableArrayList(statuses));
        statusFilterComboBox.setValue("All");
        statusFilterComboBox.setOnAction(e -> filterMatches());
        
        searchField.textProperty().addListener((obs, old, newVal) -> filterMatches());
    }
    
    private void setupTableSelection() {
        matchyTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editMatchButton.setDisable(newSelection == null);
                deleteMatchButton.setDisable(newSelection == null);
            });
    }
    
    private void loadMatchesData() {
        try {
            List<Matchy> matches = matchyService.getAllMatches();
            
            // Load team names for each match
            for (Matchy match : matches) {
                loadTeamNames(match);
            }
            
            matchyList = FXCollections.observableArrayList(matches);
            matchyTableView.setItems(matchyList);
            updateStats();
        } catch (Exception e) {
            showError("Error loading matches: " + e.getMessage());
        }
    }
    
    private void loadTeamNames(Matchy match) {
        try {
            Optional<Team> team1 = teamService.getTeamById(match.getTeam1Id());
            Optional<Team> team2 = teamService.getTeamById(match.getTeam2Id());
            
            team1.ifPresent(t -> match.setTeam1Name(t.getName()));
            team2.ifPresent(t -> match.setTeam2Name(t.getName()));
            
            if (match.getWinnerTeamId() != null) {
                Optional<Team> winner = teamService.getTeamById(match.getWinnerTeamId());
                winner.ifPresent(t -> match.setWinnerTeamName(t.getName()));
            }
        } catch (Exception e) {
            System.err.println("Error loading team names for match " + match.getId() + ": " + e.getMessage());
        }
    }
    
    private void filterMatches() {
        String searchText = searchField.getText().toLowerCase();
        String selectedGame = gameFilterComboBox.getValue();
        String selectedStatus = statusFilterComboBox.getValue();
        
        ObservableList<Matchy> filteredList = FXCollections.observableArrayList();
        
        for (Matchy match : matchyList) {
            boolean matchesSearch = searchText.isEmpty() || 
                match.getGame().toLowerCase().contains(searchText) ||
                (match.getTeam1Name() != null && match.getTeam1Name().toLowerCase().contains(searchText)) ||
                (match.getTeam2Name() != null && match.getTeam2Name().toLowerCase().contains(searchText));
            
            boolean matchesGame = "All".equals(selectedGame) || 
                match.getGame().equals(selectedGame);
            
            boolean matchesStatus = "All".equals(selectedStatus) || 
                match.getStatus().equals(selectedStatus);
            
            if (matchesSearch && matchesGame && matchesStatus) {
                filteredList.add(match);
            }
        }
        
        matchyTableView.setItems(filteredList);
        updateStats();
    }
    
    private void updateStats() {
        int totalMatches = matchyTableView.getItems().size();
        totalMatchesLabel.setText("Total Matches: " + totalMatches);
    }
    
    @FXML
    private void handleCreateMatch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/dashboardMatchyCreate.fxml"));
            Parent root = loader.load();
            
            DashboardMatchyCreateController controller = loader.getController();
            controller.setMatchyController(this);
            
            Stage stage = (Stage) createMatchButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Create New Match - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error opening create match screen: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditMatchFromTable() {
        Matchy selectedMatch = matchyTableView.getSelectionModel().getSelectedItem();
        if (selectedMatch == null) {
            showWarning("Please select a match to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/dashboardMatchyEdit.fxml"));
            Parent root = loader.load();
            
            DashboardMatchyEditController controller = loader.getController();
            controller.setMatch(selectedMatch);
            controller.setMatchyController(this);
            
            Stage stage = (Stage) editMatchButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Edit Match - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error opening edit match screen: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditMatch(Matchy match) {
        // This method handles edit requests from table row buttons
        // Redirect to the table-based edit method
        handleEditMatchFromTable();
    }
    
    @FXML
    private void handleDeleteMatch() {
        Matchy selectedMatch = matchyTableView.getSelectionModel().getSelectedItem();
        if (selectedMatch == null) {
            showWarning("Please select a match to delete.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/dashboardMatchyDelete.fxml"));
            Parent root = loader.load();
            
            DashboardMatchyDeleteController controller = loader.getController();
            controller.setMatch(selectedMatch);
            controller.setMatchyController(this);
            
            Stage stage = (Stage) deleteMatchButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Delete Match - Phantom App");
            stage.show();
        } catch (Exception e) {
            showError("Error opening delete match screen: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteMatch(Matchy match) {
        // This method handles delete requests from table row buttons
        // Redirect to the table-based delete method
        handleDeleteMatch();
    }
    
    @FXML
    private void handleViewMatch(Matchy match) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/view.fxml"));
            Parent root = loader.load();
            
            MatchyViewController controller = loader.getController();
            controller.setMatch(match);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Match Details - Phantom App");
            stage.showAndWait();
        } catch (Exception e) {
            showError("Error opening match details: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefresh() {
        loadMatchesData();
        searchField.clear();
        gameFilterComboBox.setValue("All");
        statusFilterComboBox.setValue("All");
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
    
    @Override
    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void setMatchyController(IDashboardMatchyController matchyController) {
        // This method is required by the interface but not used in the dashboard controller
        // The dashboard controller manages its own state
    }
    
    @Override
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public void loadMatches() {
        // This method is called from outside to refresh the matches list
        loadMatchesData();
    }
}
