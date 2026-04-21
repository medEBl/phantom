package Controllers.coachingsession;

import entities.coachingsession.CoachingSession;
import entities.user.User;
import services.coachingsession.CoachingSessionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ListCoachingSessionsController {

    private final CoachingSessionService coachingSessionService = new CoachingSessionService();
    private User currentUser;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize = 10;

    @FXML
    private Button backButton;
    
    @FXML
    private Label currentUserLabel;

    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox<String> coachFilter;
    
    @FXML
    private ComboBox<String> teamFilter;
    
    @FXML
    private ComboBox<String> statusFilter;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button addSessionButton;
    
    @FXML
    private TableView<CoachingSession> sessionsTable;
    
    @FXML
    private TableColumn<CoachingSession, Integer> idColumn;
    
    @FXML
    private TableColumn<CoachingSession, String> coachColumn;
    
    @FXML
    private TableColumn<CoachingSession, String> teamColumn;
    
    @FXML
    private TableColumn<CoachingSession, String> dateColumn;
    
    @FXML
    private TableColumn<CoachingSession, String> durationColumn;
    
    @FXML
    private TableColumn<CoachingSession, String> trainingPlanColumn;
    
    @FXML
    private TableColumn<CoachingSession, Void> actionsColumn;
    
    @FXML
    private Button prevPageButton;
    
    @FXML
    private Label pageInfoLabel;
    
    @FXML
    private Button nextPageButton;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadSessions();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            currentUserLabel.setText("USER: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("USER: NOT LOGGED IN");
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        coachColumn.setCellValueFactory(new PropertyValueFactory<>("coachName"));
        teamColumn.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        trainingPlanColumn.setCellValueFactory(new PropertyValueFactory<>("trainingPlanTitle"));
        
        dateColumn.setCellValueFactory(cellData -> {
            CoachingSession session = cellData.getValue();
            String formattedDate = session.getFormattedDate();
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });
        
        durationColumn.setCellValueFactory(cellData -> {
            CoachingSession session = cellData.getValue();
            String formattedDuration = session.getFormattedDuration();
            return new javafx.beans.property.SimpleStringProperty(formattedDuration);
        });
        
        actionsColumn.setCellFactory(param -> new TableCell<CoachingSession, Void>() {
            private final Button editButton = new Button("EDIT");
            private final Button deleteButton = new Button("DELETE");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                
                editButton.setOnAction(event -> {
                    CoachingSession session = getTableView().getItems().get(getIndex());
                    handleEditSession(session);
                });
                
                deleteButton.setOnAction(event -> {
                    CoachingSession session = getTableView().getItems().get(getIndex());
                    handleDeleteSession(session);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupFilters() {
        coachFilter.getItems().addAll("All", "My Sessions");
        coachFilter.setValue("All");
        
        teamFilter.getItems().addAll("All", "My Teams");
        teamFilter.setValue("All");
        
        statusFilter.getItems().addAll("All", "Upcoming", "Past");
        statusFilter.setValue("All");
    }

    private void loadSessions() {
        try {
            List<CoachingSession> sessions;
            
            if ("My Sessions".equals(coachFilter.getValue()) && currentUser != null) {
                sessions = coachingSessionService.getSessionsByCoach(currentUser.getId());
            } else if ("My Teams".equals(teamFilter.getValue()) && currentUser != null) {
                // TODO: Get teams for current user and filter
                sessions = coachingSessionService.getAllCoachingSessions();
            } else if ("Upcoming".equals(statusFilter.getValue())) {
                sessions = coachingSessionService.getUpcomingSessions();
            } else if ("Past".equals(statusFilter.getValue())) {
                sessions = coachingSessionService.getPastSessions();
            } else {
                sessions = coachingSessionService.getAllCoachingSessions();
            }
            
            sessionsTable.getItems().setAll(sessions);
            
            totalPages = (int) Math.ceil((double) sessions.size() / pageSize);
            updatePaginationInfo();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load coaching sessions: " + e.getMessage());
        }
    }

    private void updatePaginationInfo() {
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
            Parent root = loader.load();
            
            Controllers.home.HomeController controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Home - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot go back to home: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            // TODO: Implement search functionality
            System.out.println("Searching for: " + searchText);
        }
        loadSessions();
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        coachFilter.setValue("All");
        teamFilter.setValue("All");
        statusFilter.setValue("All");
        loadSessions();
    }

    @FXML
    private void handleAddSession() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/create_coaching_session.fxml"));
            Parent root = loader.load();
            
            Controllers.coachingsession.CreateCoachingSessionController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) addSessionButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Create Coaching Session - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot open create session: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditSession(CoachingSession session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/edit_coaching_session.fxml"));
            Parent root = loader.load();
            
            Controllers.coachingsession.EditCoachingSessionController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setCoachingSession(session);
            
            Stage stage = (Stage) sessionsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Edit Coaching Session - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot open edit session: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSession(CoachingSession session) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Coaching Session");
        confirmAlert.setContentText("Are you sure you want to delete this coaching session?\n\n" +
                "Date: " + session.getFormattedDate() + "\n" +
                "Coach: " + session.getCoachName() + "\n" +
                "Team: " + session.getTeamName());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                coachingSessionService.deleteCoachingSession(session.getId());
                loadSessions();
                showAlert("Success", "Coaching session deleted successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to delete coaching session: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadSessions();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadSessions();
        }
    }

    @FXML
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
