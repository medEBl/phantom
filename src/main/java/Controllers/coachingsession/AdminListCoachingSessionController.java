package Controllers.coachingsession;

import entities.coachingsession.CoachingSession;
import entities.user.User;
import entities.team.Team;
import services.coachingsession.CoachingSessionService;
import services.user.UserService;
import services.team.TeamService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AdminListCoachingSessionController {

    private final CoachingSessionService coachingSessionService = new CoachingSessionService();
    private final UserService userService = new UserService();
    private final TeamService teamService = new TeamService();
    private User currentUser;

    @FXML private Button backButton;
    @FXML private Label currentUserLabel;
    @FXML private TableView<CoachingSession> sessionsTable;
    @FXML private TableColumn<CoachingSession, Integer> idColumn;
    @FXML private TableColumn<CoachingSession, String> coachColumn;
    @FXML private TableColumn<CoachingSession, String> teamColumn;
    @FXML private TableColumn<CoachingSession, String> dateColumn;
    @FXML private TableColumn<CoachingSession, String> durationColumn;
    @FXML private TableColumn<CoachingSession, String> trainingPlanColumn;
    @FXML private TableColumn<CoachingSession, String> statusColumn;
    @FXML private TableColumn<CoachingSession, Void> actionsColumn;
    @FXML private ComboBox<String> coachFilter;
    @FXML private ComboBox<String> teamFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearFiltersButton;
    @FXML private Button addSessionButton;
    @FXML private Label totalSessionsLabel;
    @FXML private Label upcomingSessionsLabel;
    @FXML private Label completedSessionsLabel;
    @FXML private Button prevPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label pageInfoLabel;

    private List<CoachingSession> allSessions;
    private List<CoachingSession> filteredSessions;
    private int currentPage = 1;
    private final int itemsPerPage = 10;

    @FXML
    public void initialize() {
        System.out.println("=== AdminListCoachingSessionController initialized ===");
        setupTableColumns();
        setupFilters();
        loadSessions();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUserLabel != null && user != null) {
            currentUserLabel.setText("ADMIN: " + user.getFullName().toUpperCase());
        }
        System.out.println("Current user set: " + (user != null ? user.getFullName() : "null"));
    }

    private void setupTableColumns() {
        // ID Column
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Coach Column
        coachColumn.setCellValueFactory(param -> {
            CoachingSession session = param.getValue();
            String coachName = session.getCoachName() != null ? session.getCoachName() : "Unknown";
            return new javafx.beans.property.SimpleStringProperty(coachName);
        });

        // Team Column
        teamColumn.setCellValueFactory(param -> {
            CoachingSession session = param.getValue();
            String teamName = session.getTeamName() != null ? session.getTeamName() : "Unknown";
            return new javafx.beans.property.SimpleStringProperty(teamName);
        });

        // Date Column
        dateColumn.setCellValueFactory(param -> {
            CoachingSession session = param.getValue();
            String formattedDate = "";
            if (session.getSessionDate() != null) {
                formattedDate = session.getSessionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            return new javafx.beans.property.SimpleStringProperty(formattedDate);
        });

        // Duration Column
        durationColumn.setCellValueFactory(param -> {
            CoachingSession session = param.getValue();
            String duration = session.getDuration() + " min";
            return new javafx.beans.property.SimpleStringProperty(duration);
        });

        // Training Plan Column
        trainingPlanColumn.setCellValueFactory(param -> {
            CoachingSession session = param.getValue();
            String planTitle = session.getTrainingPlanTitle() != null ? session.getTrainingPlanTitle() : "No Plan";
            return new javafx.beans.property.SimpleStringProperty(planTitle);
        });

        // Status Column
        statusColumn.setCellValueFactory(param -> {
            CoachingSession session = param.getValue();
            String status = getSessionStatus(session);
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        // Actions Column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<CoachingSession, Void>() {
            private final Button viewButton = new Button("VIEW");
            private final Button editButton = new Button("EDIT");
            private final Button deleteButton = new Button("DELETE");
            private final HBox buttons = new HBox(8, viewButton, editButton, deleteButton);

            {
                // Style des boutons
                String buttonStyle = "-fx-cursor: hand; -fx-font-weight: bold; -fx-padding: 5 10 5 10;";
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;" + buttonStyle);
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;" + buttonStyle);
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;" + buttonStyle);

                // Actions des boutons
                viewButton.setOnAction(event -> {
                    System.out.println("=== VIEW BUTTON CLICKED ===");
                    CoachingSession session = getTableView().getItems().get(getIndex());
                    if (session != null) {
                        System.out.println("Viewing session ID: " + session.getId());
                        handleViewSession(session);
                    } else {
                        System.err.println("Session is NULL!");
                        showAlert("Error", "Cannot view session: session data is null");
                    }
                });

                editButton.setOnAction(event -> {
                    System.out.println("=== EDIT BUTTON CLICKED ===");
                    CoachingSession session = getTableView().getItems().get(getIndex());
                    if (session != null) {
                        System.out.println("Editing session ID: " + session.getId());
                        handleEditSession(session);
                    } else {
                        System.err.println("Session is NULL!");
                        showAlert("Error", "Cannot edit session: session data is null");
                    }
                });

                deleteButton.setOnAction(event -> {
                    System.out.println("=== DELETE BUTTON CLICKED ===");
                    CoachingSession session = getTableView().getItems().get(getIndex());
                    if (session != null) {
                        System.out.println("Deleting session ID: " + session.getId());
                        handleDeleteSession(session);
                    } else {
                        System.err.println("Session is NULL!");
                        showAlert("Error", "Cannot delete session: session data is null");
                    }
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

        System.out.println("Table columns setup complete");
    }

    private void setupFilters() {
        try {
            // Load coaches
            List<User> coaches = userService.getUsersByRole("COACH");
            coachFilter.getItems().add("All Coaches");
            for (User coach : coaches) {
                coachFilter.getItems().add(coach.getFullName());
            }
            coachFilter.setValue("All Coaches");

            // Load teams
            List<Team> teams = teamService.getAllTeams();
            teamFilter.getItems().add("All Teams");
            for (Team team : teams) {
                teamFilter.getItems().add(team.getName());
            }
            teamFilter.setValue("All Teams");

            // Status filter
            statusFilter.getItems().addAll("All Status", "Upcoming", "In Progress", "Completed", "Cancelled");
            statusFilter.setValue("All Status");

            System.out.println("Filters setup complete");
        } catch (Exception e) {
            System.err.println("Error setting up filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSessions() {
        try {
            System.out.println("Loading sessions...");
            allSessions = coachingSessionService.getAllCoachingSessions();
            System.out.println("Loaded " + (allSessions != null ? allSessions.size() : 0) + " sessions");
            applyFilters();
            updateStatistics();
        } catch (Exception e) {
            System.err.println("Failed to load sessions: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to load coaching sessions: " + e.getMessage());
        }
    }

    private void applyFilters() {
        if (allSessions == null) {
            filteredSessions = null;
            return;
        }

        filteredSessions = allSessions;

        // Apply coach filter
        String selectedCoach = coachFilter.getValue();
        if (selectedCoach != null && !selectedCoach.equals("All Coaches")) {
            filteredSessions = filteredSessions.stream()
                    .filter(session -> session.getCoachName() != null &&
                            session.getCoachName().equals(selectedCoach))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply team filter
        String selectedTeam = teamFilter.getValue();
        if (selectedTeam != null && !selectedTeam.equals("All Teams")) {
            filteredSessions = filteredSessions.stream()
                    .filter(session -> session.getTeamName() != null &&
                            session.getTeamName().equals(selectedTeam))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply status filter
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("All Status")) {
            filteredSessions = filteredSessions.stream()
                    .filter(session -> getSessionStatus(session).equals(selectedStatus))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply search filter
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.toLowerCase().trim();
            filteredSessions = filteredSessions.stream()
                    .filter(session -> {
                        String coach = session.getCoachName() != null ? session.getCoachName().toLowerCase() : "";
                        String team = session.getTeamName() != null ? session.getTeamName().toLowerCase() : "";
                        String plan = session.getTrainingPlanTitle() != null ? session.getTrainingPlanTitle().toLowerCase() : "";
                        return coach.contains(search) || team.contains(search) || plan.contains(search);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        System.out.println("Filtered sessions: " + filteredSessions.size());
        updateTable();
    }

    private void updateTable() {
        if (filteredSessions == null || filteredSessions.isEmpty()) {
            sessionsTable.getItems().clear();
            pageInfoLabel.setText("Page 0 of 0");
            prevPageButton.setDisable(true);
            nextPageButton.setDisable(true);
            return;
        }

        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, filteredSessions.size());

        if (fromIndex >= filteredSessions.size()) {
            currentPage = 1;
            fromIndex = 0;
            toIndex = Math.min(itemsPerPage, filteredSessions.size());
        }

        List<CoachingSession> pageData = filteredSessions.subList(fromIndex, toIndex);
        sessionsTable.getItems().setAll(pageData);

        int totalPages = (int) Math.ceil((double) filteredSessions.size() / itemsPerPage);
        pageInfoLabel.setText("Page " + currentPage + " of " + Math.max(1, totalPages));

        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);

        System.out.println("Table updated - Page " + currentPage + ", showing " + pageData.size() + " items");
    }

    private String getSessionStatus(CoachingSession session) {
        if (session == null || session.getSessionDate() == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sessionEnd = session.getSessionDate().plusMinutes(session.getDuration());

        if (now.isBefore(session.getSessionDate())) {
            return "Upcoming";
        } else if (now.isAfter(sessionEnd)) {
            return "Completed";
        } else {
            return "In Progress";
        }
    }

    private void updateStatistics() {
        if (filteredSessions == null) {
            totalSessionsLabel.setText("0");
            upcomingSessionsLabel.setText("0");
            completedSessionsLabel.setText("0");
            return;
        }

        int total = filteredSessions.size();
        int upcoming = 0;
        int completed = 0;

        for (CoachingSession session : filteredSessions) {
            String status = getSessionStatus(session);
            switch (status) {
                case "Upcoming":
                    upcoming++;
                    break;
                case "Completed":
                    completed++;
                    break;
            }
        }

        totalSessionsLabel.setText(String.valueOf(total));
        upcomingSessionsLabel.setText(String.valueOf(upcoming));
        completedSessionsLabel.setText(String.valueOf(completed));

        System.out.println("Statistics updated - Total: " + total + ", Upcoming: " + upcoming + ", Completed: " + completed);
    }

    @FXML
    private void handleSearch() {
        System.out.println("=== SEARCH ===");
        currentPage = 1;
        applyFilters();
        updateStatistics();
    }

    @FXML
    private void handleClear() {
        System.out.println("=== CLEAR FILTERS ===");
        coachFilter.setValue("All Coaches");
        teamFilter.setValue("All Teams");
        statusFilter.setValue("All Status");
        searchField.clear();
        currentPage = 1;
        applyFilters();
        updateStatistics();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            updateTable();
        }
    }

    @FXML
    private void handleNextPage() {
        int totalPages = (int) Math.ceil((double) filteredSessions.size() / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            updateTable();
        }
    }

    @FXML
    private void handleAddSession() {
        System.out.println("=== ADD SESSION ===");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/adminadd_coaching_session.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminAddCoachingSessionController) {
                ((AdminAddCoachingSessionController) controller).setCurrentUser(currentUser);
            }

            Stage stage = (Stage) addSessionButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Create Coaching Session - Admin");

        } catch (IOException e) {
            System.err.println("Cannot open create session: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot open create session: " + e.getMessage());
        }
    }

    @FXML
    public void handleViewSession(CoachingSession session) {
        System.out.println("=== VIEW SESSION: " + session.getId() + " ===");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/adminedit_coaching_session.fxml"));
            Parent root = loader.load();

            AdminEditCoachingSessionController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setCoachingSession(session);
            controller.setViewMode(true); // Mode vue uniquement

            Stage stage = (Stage) sessionsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("View Coaching Session - Admin");

        } catch (IOException e) {
            System.err.println("Cannot open view session: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot open view session: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditSession(CoachingSession session) {
        System.out.println("=== EDIT SESSION: " + session.getId() + " ===");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/adminedit_coaching_session.fxml"));
            Parent root = loader.load();

            AdminEditCoachingSessionController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setCoachingSession(session);
            controller.setEditMode(true);

            Stage stage = (Stage) sessionsTable.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Edit Coaching Session - Admin");

        } catch (IOException e) {
            System.err.println("Cannot open edit session: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot open edit session: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSession(CoachingSession session) {
        System.out.println("=== DELETE SESSION: " + session.getId() + " ===");

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Coaching Session");
        confirmAlert.setContentText("Are you sure you want to delete this coaching session?\n\n" +
                "Session ID: " + session.getId() + "\n" +
                "Coach: " + (session.getCoachName() != null ? session.getCoachName() : "Unknown") + "\n" +
                "Team: " + (session.getTeamName() != null ? session.getTeamName() : "Unknown"));

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                coachingSessionService.deleteCoachingSession(session.getId());
                showAlert("Success", "Coaching session deleted successfully!");
                loadSessions(); // Refresh the table
            } catch (Exception e) {
                System.err.println("Failed to delete session: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "Failed to delete coaching session: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent root = loader.load();

            Controllers.admin.DashboardController controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Admin Dashboard - Phantom App");

        } catch (IOException e) {
            System.err.println("Cannot go back to dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}