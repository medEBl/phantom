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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class AdminEditTrainingPlanController {

    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private User currentUser;
    private TrainingPlan currentPlan;

    @FXML
    private Button backButton;
    
    @FXML
    private Label currentUserLabel;

    @FXML
    private TextField titleField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private ComboBox<String> focusAreaComboBox;
    
    @FXML
    private ComboBox<String> difficultyComboBox;
    
    @FXML
    private ComboBox<String> coachComboBox;
    
    @FXML
    private ComboBox<String> teamComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button clearButton;

    @FXML
    public void initialize() {
        setupComboBoxes();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            currentUserLabel.setText("ADMIN: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("ADMIN: NOT LOGGED IN");
        }
    }

    public void setTrainingPlan(TrainingPlan plan) {
        this.currentPlan = plan;
        populateFields();
    }

    private void setupComboBoxes() {
        focusAreaComboBox.getItems().addAll("Attaque", "Défense", "Tactique", "Physique", "Mental");
        difficultyComboBox.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        // TODO: Load coaches and teams from service
    }

    private void populateFields() {
        if (currentPlan != null) {
            titleField.setText(currentPlan.getTitle());
            descriptionArea.setText(currentPlan.getDescription());
            focusAreaComboBox.setValue(currentPlan.getFocusArea());
            difficultyComboBox.setValue(currentPlan.getDifficultyLevel());
            
            // TODO: Set coach and team values
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
    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            currentPlan.setTitle(titleField.getText().trim());
            currentPlan.setDescription(descriptionArea.getText().trim());
            currentPlan.setFocusArea(focusAreaComboBox.getValue());
            currentPlan.setDifficultyLevel(difficultyComboBox.getValue());
            
            // TODO: Set coach and team IDs
            
            trainingPlanService.updateTrainingPlan(currentPlan);
            
            showAlert("Success", "Training plan updated successfully!");
            
        } catch (Exception e) {
            showAlert("Error", "Failed to update training plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        titleField.clear();
        descriptionArea.clear();
        focusAreaComboBox.setValue("Attaque");
        difficultyComboBox.setValue("Intermédiaire");
        coachComboBox.setValue(null);
        teamComboBox.setValue(null);
    }

    private boolean validateForm() {
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        String focusArea = focusAreaComboBox.getValue();
        String difficulty = difficultyComboBox.getValue();
        
        if (title.isEmpty()) {
            showAlert("Validation Error", "Title is required");
            return false;
        }
        
        if (description.isEmpty()) {
            showAlert("Validation Error", "Description is required");
            return false;
        }
        
        if (focusArea == null) {
            showAlert("Validation Error", "Focus area is required");
            return false;
        }
        
        if (difficulty == null) {
            showAlert("Validation Error", "Difficulty level is required");
            return false;
        }
        
        return true;
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
