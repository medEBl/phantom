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

public class ListTrainingPlansController {

    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
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
            currentUserLabel.setText("USER: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("USER: NOT LOGGED IN");
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
            List<TrainingPlan> plans;
            
            if ("My Plans".equals(coachFilter.getValue()) && currentUser != null) {
                plans = trainingPlanService.getPlansByCoach(currentUser.getId());
            } else if ("My Teams".equals(teamFilter.getValue()) && currentUser != null) {
                // TODO: Get teams for current user and filter
                plans = trainingPlanService.getAllTrainingPlans();
            } else if (!"All".equals(difficultyFilter.getValue())) {
                plans = trainingPlanService.getPlansByDifficulty(difficultyFilter.getValue());
            } else if (!"All".equals(focusAreaFilter.getValue())) {
                plans = trainingPlanService.getPlansByFocusArea(focusAreaFilter.getValue());
            } else {
                plans = trainingPlanService.getAllTrainingPlans();
            }
            
            plansTable.getItems().setAll(plans);
            
            totalPages = (int) Math.ceil((double) plans.size() / pageSize);
            updatePaginationInfo();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load training plans: " + e.getMessage());
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
        loadPlans();
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        coachFilter.setValue("All");
        teamFilter.setValue("All");
        difficultyFilter.setValue("All");
        focusAreaFilter.setValue("All");
        loadPlans();
    }

    @FXML
    private void handleAddPlan() {
        try {
            System.out.println("handleAddPlan called - currentUser is: " + (currentUser != null ? currentUser.getFullName() : "null"));
            if (currentUser == null) {
                System.err.println("Error: No user logged in for training plan creation");
                showAlert("Error", "Please login first to create training plans");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/create_training_plan.fxml"));
            Parent root = loader.load();
            
            Controllers.trainingplan.CreateTrainingPlanController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) addPlanButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Create Training Plan - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot open create plan: " + e.getMessage());
        }
    }

    public void handleEditPlan(TrainingPlan plan) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/edit_training_plan.fxml"));
            Parent root = loader.load();
            
            Controllers.trainingplan.EditTrainingPlanController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            controller.setTrainingPlan(plan);
            
            Stage stage = (Stage) plansTable.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Edit Training Plan - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot open edit plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeletePlan(TrainingPlan plan) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Training Plan");
        confirmAlert.setContentText("Are you sure you want to delete this training plan?\n\n" +
                "Title: " + plan.getTitle() + "\n" +
                "Focus Area: " + plan.getFocusArea() + "\n" +
                "Difficulty: " + plan.getDifficultyLevel());

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                trainingPlanService.deleteTrainingPlan(plan.getId());
                loadPlans();
                showAlert("Success", "Training plan deleted successfully!");
            } catch (Exception e) {
                showAlert("Error", "Failed to delete training plan: " + e.getMessage());
            }
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
