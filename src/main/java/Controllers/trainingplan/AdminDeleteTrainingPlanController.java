package Controllers.trainingplan;

import entities.trainingplan.TrainingPlan;
import entities.user.User;
import services.trainingplan.TrainingPlanService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class AdminDeleteTrainingPlanController {

    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private User currentUser;
    private TrainingPlan currentPlan;

    @FXML
    private Button backButton;
    
    @FXML
    private Label currentUserLabel;

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label descriptionLabel;
    
    @FXML
    private Label focusAreaLabel;
    
    @FXML
    private Label difficultyLabel;
    
    @FXML
    private Button confirmDeleteButton;
    
    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        // Initialize with empty plan
    setCurrentUser(null);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        currentUserLabel.setText("ADMIN: " + (user != null ? user.getFullName().toUpperCase() : "LOADING..."));
    }

    public void setTrainingPlan(TrainingPlan plan) {
        this.currentPlan = plan;
        populateDetails();
    }

    private void populateDetails() {
        if (currentPlan != null) {
            titleLabel.setText("Title: " + currentPlan.getTitle());
            descriptionLabel.setText("Description: " + currentPlan.getDescription());
            focusAreaLabel.setText("Focus Area: " + currentPlan.getFocusArea());
            difficultyLabel.setText("Difficulty: " + currentPlan.getDifficultyLevel());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/adminlist_training_plans.fxml"));
            Parent root = loader.load();
            
            Controllers.trainingplan.AdminListTrainingPlanController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Admin Training Plans - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot go back to training plans: " + e.getMessage());
        }
    }

    @FXML
    private void handleConfirmDelete() {
        if (currentPlan == null) {
            showAlert("Error", "No training plan selected for deletion");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Training Plan");
        confirmAlert.setContentText("Are you sure you want to permanently delete this training plan?\n\n" +
                "Title: " + currentPlan.getTitle() + "\n" +
                "Focus Area: " + currentPlan.getFocusArea() + "\n" +
                "Difficulty: " + currentPlan.getDifficultyLevel() + "\n\n" +
                "This action cannot be undone!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                trainingPlanService.deleteTrainingPlan(currentPlan.getId());
                
                showAlert("Success", "Training plan deleted successfully!");
                handleBack();
                
            } catch (Exception e) {
                showAlert("Error", "Failed to delete training plan: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleCancel() {
        handleBack();
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
