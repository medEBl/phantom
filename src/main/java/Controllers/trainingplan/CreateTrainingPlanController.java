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
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class CreateTrainingPlanController {

    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private final UserService userService = new UserService();
    private final TeamService teamService = new TeamService();
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
    private ComboBox<User> coachComboBox;
    
    @FXML
    private ComboBox<Team> teamComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button clearButton;

    @FXML
    public void initialize() {
        setupComboBoxes();
        loadDropdownData();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            currentUserLabel.setText("USER: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("USER: NOT LOGGED IN");
        }
    }

    private void setupComboBoxes() {
        // Setup focus areas
        focusAreaComboBox.getItems().addAll("Attaque", "Défense", "Tactique", "Physique", "Mental");
        
        // Setup difficulty levels
        difficultyComboBox.getItems().addAll("Débutant", "Intermédiaire", "Avancé");
    }
    
    private void loadDropdownData() {
        try {
            // Load coaches
            List<User> coaches = userService.getUsersByRole("COACH");
            coachComboBox.getItems().addAll(coaches);
            
            // Setup coach display
            coachComboBox.setCellFactory(param -> new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getFullName() + " (" + user.getUsername() + ")");
                    }
                }
            });
            coachComboBox.setButtonCell(new ListCell<User>() {
                @Override
                protected void updateItem(User user, boolean empty) {
                    super.updateItem(user, empty);
                    if (empty || user == null) {
                        setText(null);
                    } else {
                        setText(user.getFullName() + " (" + user.getUsername() + ")");
                    }
                }
            });
            
            // Load teams
            List<Team> teams = teamService.getAllTeams();
            teamComboBox.getItems().addAll(teams);
            
            // Setup team display
            teamComboBox.setCellFactory(param -> new ListCell<Team>() {
                @Override
                protected void updateItem(Team team, boolean empty) {
                    super.updateItem(team, empty);
                    if (empty || team == null) {
                        setText(null);
                    } else {
                        setText(team.getName() + " (" + team.getGame() + ")");
                    }
                }
            });
            teamComboBox.setButtonCell(new ListCell<Team>() {
                @Override
                protected void updateItem(Team team, boolean empty) {
                    super.updateItem(team, empty);
                    if (empty || team == null) {
                        setText(null);
                    } else {
                        setText(team.getName() + " (" + team.getGame() + ")");
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error loading dropdown data: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/list_training_plans.fxml"));
            Parent root = loader.load();
            
            Controllers.trainingplan.ListTrainingPlansController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Training Plans - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot go back to training plans: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            // Create new training plan
            TrainingPlan plan = new TrainingPlan();
            plan.setTitle(titleField.getText().trim());
            plan.setDescription(descriptionArea.getText().trim());
            plan.setFocusArea(focusAreaComboBox.getValue());
            plan.setDifficultyLevel(difficultyComboBox.getValue());
            plan.setCoachId(coachComboBox.getValue().getId());
            plan.setTeamId(teamComboBox.getValue().getId());

            trainingPlanService.createTrainingPlan(plan);
            
            showAlert("Success", "Training plan created successfully!");
            handleBack(); // Go back to list

        } catch (Exception e) {
            showAlert("Error", "Failed to create training plan: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        titleField.clear();
        descriptionArea.clear();
        focusAreaComboBox.setValue(null);
        difficultyComboBox.setValue(null);
        coachComboBox.setValue(null);
        teamComboBox.setValue(null);
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        // Validate Title
        if (titleField.getText().trim().isEmpty()) {
            errorMessage.append("Title is required.\n");
        }

        // Validate Focus Area
        if (focusAreaComboBox.getValue() == null) {
            errorMessage.append("Focus area selection is required.\n");
        }

        // Validate Difficulty
        if (difficultyComboBox.getValue() == null) {
            errorMessage.append("Difficulty level selection is required.\n");
        }

        // Validate Coach Selection
        if (coachComboBox.getValue() == null) {
            errorMessage.append("Coach selection is required.\n");
        }

        // Validate Team Selection
        if (teamComboBox.getValue() == null) {
            errorMessage.append("Team selection is required.\n");
        }

        if (errorMessage.length() > 0) {
            showAlert("Validation Error", errorMessage.toString());
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
