package Controllers.trainingplan;

import entities.trainingplan.TrainingPlan;
import entities.user.User;
import entities.team.Team;
import services.trainingplan.TrainingPlanService;
import services.user.UserService;
import services.team.TeamService;
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

public class AdminAddTrainingPlanController {

    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private User currentUser;

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

    private void setupComboBoxes() {
        focusAreaComboBox.getItems().addAll("Attaque", "Défense", "Tactique", "Physique", "Mental");
        focusAreaComboBox.setValue("Attaque");
        
        difficultyComboBox.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
        difficultyComboBox.setValue("Intermédiaire");
        
        // Load coaches from user service (since Coach entity doesn't exist)
        try {
            UserService userService = new UserService();
            coachComboBox.getItems().clear();
            coachComboBox.getItems().addAll(userService.getAllUsers().stream()
                .filter(user -> "COACH".equalsIgnoreCase(user.getRole()))
                .map(user -> user.getFullName() + " (ID: " + user.getId() + ")")
                .toArray(String[]::new));
        } catch (Exception e) {
            System.err.println("Error loading coaches: " + e.getMessage());
            coachComboBox.getItems().addAll("Coach 1", "Coach 2", "Coach 3");
        }
        
        // Load teams from service
        try {
            TeamService teamService = new TeamService();
            teamComboBox.getItems().clear();
            teamComboBox.getItems().addAll(teamService.getAllTeams().stream()
                .map(team -> team.getName() + " (ID: " + team.getId() + ")")
                .toArray(String[]::new));
        } catch (Exception e) {
            System.err.println("Error loading teams: " + e.getMessage());
            teamComboBox.getItems().addAll("Team 1", "Team 2", "Team 3");
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
            TrainingPlan plan = new TrainingPlan();
            plan.setTitle(titleField.getText().trim());
            plan.setDescription(descriptionArea.getText().trim());
            plan.setFocusArea(focusAreaComboBox.getValue());
            plan.setDifficultyLevel(difficultyComboBox.getValue());
            
            // Set coach and team IDs from selection
            if (coachComboBox.getValue() != null) {
                String coachSelection = coachComboBox.getValue();
                // Extract coach ID from selection format "Name (ID: X)"
                if (coachSelection.contains("(ID:")) {
                    int startIndex = coachSelection.indexOf("(ID:") + 4;
                    int endIndex = coachSelection.indexOf(")", startIndex);
                    if (endIndex > startIndex) {
                        String coachIdStr = coachSelection.substring(startIndex, endIndex);
                        try {
                            int coachId = Integer.parseInt(coachIdStr.trim());
                            plan.setCoachId(coachId);
                            System.out.println("Set coach ID: " + coachId);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid coach ID format: " + coachIdStr);
                        }
                    }
                }
            }
            
            if (teamComboBox.getValue() != null) {
                String teamSelection = teamComboBox.getValue();
                // Extract team ID from selection format "Name (ID: X)"
                if (teamSelection.contains("(ID:")) {
                    int startIndex = teamSelection.indexOf("(ID:") + 4;
                    int endIndex = teamSelection.indexOf(")", startIndex);
                    if (endIndex > startIndex) {
                        String teamIdStr = teamSelection.substring(startIndex, endIndex);
                        try {
                            int teamId = Integer.parseInt(teamIdStr.trim());
                            plan.setTeamId(teamId);
                            System.out.println("Set team ID: " + teamId);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid team ID format: " + teamIdStr);
                        }
                    }
                }
            }
            
            trainingPlanService.createTrainingPlan(plan);
            
            showAlert("Success", "Training plan created successfully!");
            handleClear();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to create training plan: " + e.getMessage());
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
