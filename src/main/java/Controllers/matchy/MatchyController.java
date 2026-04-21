package Controllers.matchy;

import entities.matchy.Matchy;
import entities.user.User;
import javafx.stage.Modality;
import Iservices.matchy.IMatchyService;
import services.matchy.MatchyService;
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

public class MatchyController implements IDashboardMatchyController {
    
    private final IMatchyService matchyService = new MatchyService();
    private ObservableList<Matchy> matchyList = FXCollections.observableArrayList();
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
    private TableColumn<Matchy, String> statusColumn;
    
    @FXML
    private TableColumn<Matchy, String> team1Column;
    
    @FXML
    private TableColumn<Matchy, String> team2Column;
    
    @FXML
    private TableColumn<Matchy, Integer> score1Column;
    
    @FXML
    private TableColumn<Matchy, Integer> score2Column;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> gameFilterComboBox;
    
    @FXML
    private ComboBox<String> statusFilterComboBox;
    
    @FXML
    private Label totalMatchesLabel;
    
    @FXML
    private Button createMatchButton;
    
    @FXML
    private Button editMatchButton;
    
    @FXML
    private Button deleteMatchButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button backButton;
    
    public void initialize() {
        try {
            System.out.println("Initializing MatchyController...");
            
            setupTableColumns();
            System.out.println("✅ Table columns setup completed");
            
            setupFilters();
            System.out.println("✅ Filters setup completed");
            
            loadMatches();
            System.out.println("✅ Matches loaded successfully");
            
            setupTableSelection();
            System.out.println("✅ Table selection setup completed");
            
            setupSearchListener();
            System.out.println("✅ Search listener setup completed");
            
            System.out.println("✅ MatchyController initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing MatchyController: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to initialize matchy screen: " + e.getMessage());
        }
    }
    
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("game"));
        matchDateColumn.setCellValueFactory(new PropertyValueFactory<>("matchDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        team1Column.setCellValueFactory(new PropertyValueFactory<>("team1Name"));
        team2Column.setCellValueFactory(new PropertyValueFactory<>("team2Name"));
        score1Column.setCellValueFactory(new PropertyValueFactory<>("scoreTeam1"));
        score2Column.setCellValueFactory(new PropertyValueFactory<>("scoreTeam2"));
        
        // Format the match date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        matchDateColumn.setCellFactory(column -> new TableCell<Matchy, LocalDateTime>() {
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
        
        // Format scores to show "N/A" when null
        score1Column.setCellFactory(column -> new TableCell<Matchy, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("N/A");
                } else {
                    setText(String.valueOf(item));
                }
            }
        });
        
        score2Column.setCellFactory(column -> new TableCell<Matchy, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("N/A");
                } else {
                    setText(String.valueOf(item));
                }
            }
        });
        
        // Color code status
        statusColumn.setCellFactory(column -> new TableCell<Matchy, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    switch (item.toLowerCase()) {
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
    }
    
    private void setupTableSelection() {
        matchyTableView.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                editMatchButton.setDisable(newSelection == null);
                deleteMatchButton.setDisable(newSelection == null);
            });
    }
    
    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldText, newText) -> filterMatches());
    }
    
    public void loadMatches() {
        try {
            System.out.println("Loading matches...");
            List<Matchy> matches = matchyService.getAllMatches();
            System.out.println("Found " + matches.size() + " matches");
            
            matchyList.clear();
            matchyList.addAll(matches);
            matchyTableView.setItems(matchyList);
            updateStats();
            
            System.out.println("✅ Matches loaded successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error loading matches: " + e.getMessage());
            e.printStackTrace();
            
            // Créer une liste vide pour éviter les erreurs d'affichage
            matchyList.clear();
            matchyTableView.setItems(matchyList);
            totalMatchesLabel.setText("Total Matches: 0");
            
            showError("Error loading matches: " + e.getMessage());
        }
    }
    
    private void filterMatches() {
        String searchText = searchField.getText().toLowerCase();
        String selectedGame = gameFilterComboBox.getValue();
        String selectedStatus = statusFilterComboBox.getValue();
        
        ObservableList<Matchy> filteredList = FXCollections.observableArrayList();
        
        for (Matchy match : matchyList) {
            boolean matchesSearch = match.getGame().toLowerCase().contains(searchText) ||
                                   (match.getTeam1Name() != null && match.getTeam1Name().toLowerCase().contains(searchText)) ||
                                   (match.getTeam2Name() != null && match.getTeam2Name().toLowerCase().contains(searchText)) ||
                                   (match.getLocation() != null && match.getLocation().toLowerCase().contains(searchText));
            
            boolean matchesGame = "All".equals(selectedGame) || 
                                selectedGame.equals(match.getGame());
            
            boolean matchesStatus = "All".equals(selectedStatus) || 
                                  selectedStatus.equals(match.getStatus());
            
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/create.fxml"));
            Parent root = loader.load();
            
            MatchyCreateController controller = loader.getController();
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
    private void handleEditMatch() {
        Matchy selectedMatch = matchyTableView.getSelectionModel().getSelectedItem();
        if (selectedMatch == null) {
            showWarning("Please select a match to edit.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/edit.fxml"));
            Parent root = loader.load();
            
            MatchyEditController controller = loader.getController();
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
    private void handleDeleteMatch() {
        Matchy selectedMatch = matchyTableView.getSelectionModel().getSelectedItem();
        if (selectedMatch == null) {
            showWarning("Please select a match to delete.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/delete.fxml"));
            Parent root = loader.load();
            
            MatchyDeleteController controller = loader.getController();
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
    private void handleRefresh() {
        loadMatches();
        searchField.clear();
        gameFilterComboBox.setValue("All");
        statusFilterComboBox.setValue("All");
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
    private void handleViewMatch(MouseEvent event) {
        if (event.getClickCount() == 2) {
            Matchy selectedMatch = matchyTableView.getSelectionModel().getSelectedItem();
            if (selectedMatch != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/view.fxml"));
                    Parent root = loader.load();
                    
                    MatchyViewController controller = loader.getController();
                    controller.setMatch(selectedMatch);
                    
                    Stage stage = new Stage();
                    stage.setTitle("Match Details: " + selectedMatch.getGame());
                    stage.setScene(new Scene(root, 700, 600));
                    stage.setResizable(false);
                    stage.show();
                } catch (Exception e) {
                    showError("Error opening match details: " + e.getMessage());
                }
            }
        }
    }
    
    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
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
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @Override
    public void setMatchyController(IDashboardMatchyController matchyController) {
        // This method is required by the interface but not used in the original MatchyController
        // The dashboard controllers will handle their own controller references
    }
}
