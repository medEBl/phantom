package Controllers.trainingplan;

import entities.trainingplan.TrainingPlan;
import entities.user.User;
import services.trainingplan.TrainingPlanService;
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
import java.util.List;
import java.util.Optional;

public class AdminListTrainingPlanController {

    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private User currentUser;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize = 10;
    private List<TrainingPlan> allPlans;
    private List<TrainingPlan> filteredPlans;

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
    private ComboBox<String> difficultyFilter;
    
    @FXML
    private ComboBox<String> focusAreaFilter;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button addPlanButton;
    
    @FXML
    private TableView<TrainingPlan> plansTable;
    
    @FXML
    private TableColumn<TrainingPlan, Integer> idColumn;
    
    @FXML
    private TableColumn<TrainingPlan, String> titleColumn;
    
    @FXML
    private TableColumn<TrainingPlan, String> coachColumn;
    
    @FXML
    private TableColumn<TrainingPlan, String> teamColumn;
    
    @FXML
    private TableColumn<TrainingPlan, String> focusAreaColumn;
    
    @FXML
    private TableColumn<TrainingPlan, String> difficultyColumn;
    
    @FXML
    private TableColumn<TrainingPlan, Void> actionsColumn;
    
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
        loadPlans();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            currentUserLabel.setText("ADMIN: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("ADMIN: NOT LOGGED IN");
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        coachColumn.setCellValueFactory(new PropertyValueFactory<>("coachName"));
        teamColumn.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        focusAreaColumn.setCellValueFactory(new PropertyValueFactory<>("focusArea"));
        difficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficultyLevel"));
        
        actionsColumn.setCellFactory(param -> new TableCell<TrainingPlan, Void>() {
            private final Button editButton = new Button("EDIT");
            private final Button deleteButton = new Button("DELETE");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
                
                editButton.setOnAction(event -> {
                    TrainingPlan plan = getTableView().getItems().get(getIndex());
                    handleEditPlan(plan);
                });
                
                deleteButton.setOnAction(event -> {
                    TrainingPlan plan = getTableView().getItems().get(getIndex());
                    handleDeletePlan(plan);
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
        coachFilter.getItems().addAll("All", "My Plans");
        coachFilter.setValue("All");
        
        teamFilter.getItems().addAll("All", "My Teams");
        teamFilter.setValue("All");
        
        difficultyFilter.getItems().addAll("All", "Débutant", "Intermédiaire", "Avancé");
        difficultyFilter.setValue("All");
        
        focusAreaFilter.getItems().addAll("All", "Attaque", "Défense", "Tactique", "Physique", "Mental");
        focusAreaFilter.setValue("All");
    }

    private void loadPlans() {
        try {
            allPlans = trainingPlanService.getAllTrainingPlans();
            applyFilters();
            updatePaginationInfo();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load training plans: " + e.getMessage());
        }
    }
    
    private void applyFilters() {
        if (allPlans == null) {
            filteredPlans = null;
            return;
        }

        filteredPlans = allPlans;

        // Apply coach filter
        String selectedCoach = coachFilter.getValue();
        if (selectedCoach != null && !selectedCoach.equals("All")) {
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> plan.getCoachName() != null &&
                            plan.getCoachName().toLowerCase().contains(selectedCoach.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply team filter
        String selectedTeam = teamFilter.getValue();
        if (selectedTeam != null && !selectedTeam.equals("All")) {
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> plan.getTeamName() != null &&
                            plan.getTeamName().toLowerCase().contains(selectedTeam.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply difficulty filter
        String selectedDifficulty = difficultyFilter.getValue();
        if (selectedDifficulty != null && !selectedDifficulty.equals("All")) {
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> plan.getDifficultyLevel() != null &&
                            plan.getDifficultyLevel().equals(selectedDifficulty))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply focus area filter
        String selectedFocusArea = focusAreaFilter.getValue();
        if (selectedFocusArea != null && !selectedFocusArea.equals("All")) {
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> plan.getFocusArea() != null &&
                            plan.getFocusArea().equals(selectedFocusArea))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Apply search filter
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String search = searchText.toLowerCase().trim();
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> {
                        String title = plan.getTitle() != null ? plan.getTitle().toLowerCase() : "";
                        String coach = plan.getCoachName() != null ? plan.getCoachName().toLowerCase() : "";
                        String team = plan.getTeamName() != null ? plan.getTeamName().toLowerCase() : "";
                        String focus = plan.getFocusArea() != null ? plan.getFocusArea().toLowerCase() : "";
                        String difficulty = plan.getDifficultyLevel() != null ? plan.getDifficultyLevel().toLowerCase() : "";
                        String description = plan.getDescription() != null ? plan.getDescription().toLowerCase() : "";
                        return title.contains(search) || coach.contains(search) || team.contains(search) || 
                               focus.contains(search) || difficulty.contains(search) || description.contains(search);
                    })
                    .collect(java.util.stream.Collectors.toList());
        }

        System.out.println("Filtered plans: " + filteredPlans.size());
        updateTable();
    }
    
    private void updateTable() {
        if (filteredPlans != null) {
            plansTable.getItems().setAll(filteredPlans);
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

    @FXML
    private void handleSearch() {
        System.out.println("=== SEARCH TRAINING PLANS ===");
        currentPage = 1;
        applyFilters();
        updatePaginationInfo();
    }

    @FXML
    private void handleClear() {
        System.out.println("=== CLEAR TRAINING PLAN FILTERS ===");
        searchField.clear();
        coachFilter.setValue("All");
        teamFilter.setValue("All");
        difficultyFilter.setValue("All");
        focusAreaFilter.setValue("All");
        currentPage = 1;
        applyFilters();
        updatePaginationInfo();
    }

    @FXML
    private void handleAddPlan() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/admin_add_training_plan.fxml"));
            Parent root = loader.load();
            
            Controllers.trainingplan.AdminAddTrainingPlanController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) addPlanButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Add Training Plan - Admin");
            
        } catch (IOException e) {
            System.err.println("Cannot open add plan: " + e.getMessage());
        }
    }

    @FXML
    public void handleEditPlan(TrainingPlan plan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/admin_edit_training_plan.fxml"));
            Parent root = loader.load();
            
            Controllers.trainingplan.AdminEditTrainingPlanController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setTrainingPlan(plan);
            
            Stage stage = (Stage) plansTable.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Edit Training Plan - Admin");
            
        } catch (IOException e) {
            System.err.println("Cannot open edit plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeletePlan(TrainingPlan plan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/admin_delete_training_plan.fxml"));
            Parent root = loader.load();
            
            Controllers.trainingplan.AdminDeleteTrainingPlanController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setTrainingPlan(plan);
            
            Stage stage = (Stage) plansTable.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Delete Training Plan - Admin");
            
        } catch (IOException e) {
            System.err.println("Cannot open delete plan: " + e.getMessage());
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadPlans();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadPlans();
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
