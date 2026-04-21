package Controllers.coachingsession;

import entities.coachingsession.CoachingSession;
import entities.user.User;
import entities.team.Team;
import entities.trainingplan.TrainingPlan;
import services.coachingsession.CoachingSessionService;
import services.user.UserService;
import services.team.TeamService;
import services.trainingplan.TrainingPlanService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class CreateCoachingSessionController {

    private final CoachingSessionService coachingSessionService = new CoachingSessionService();
    private final UserService userService = new UserService();
    private final TeamService teamService = new TeamService();
    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private User currentUser;

    @FXML
    private Button backButton;
    
    @FXML
    private Label currentUserLabel;

    @FXML
    private ComboBox<User> coachComboBox;
    
    @FXML
    private ComboBox<Team> teamComboBox;
    
    @FXML
    private DatePicker sessionDatePicker;
    
    @FXML
    private ComboBox<String> sessionHourCombo;
    
    @FXML
    private ComboBox<String> sessionMinuteCombo;
    
    @FXML
    private TextField durationField;
    
    @FXML
    private TextArea notesArea;
    
    @FXML
    private ComboBox<TrainingPlan> trainingPlanComboBox;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button clearButton;

    @FXML
    public void initialize() {
        setupTimeComboBoxes();
        loadDropdownData();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            currentUserLabel.setText("USER: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("USER: NOT LOGGED IN");
        }
        
        // Auto-select current user if they are a coach
        if (currentUser != null && "COACH".equals(currentUser.getRole())) {
            // Find and select the current user in the coach dropdown
            for (User coach : coachComboBox.getItems()) {
                if (coach.getId() == currentUser.getId()) {
                    coachComboBox.setValue(coach);
                    coachComboBox.setDisable(true);
                    break;
                }
            }
        }
    }

    private void setupTimeComboBoxes() {
        // Setup hours (0-23)
        for (int i = 0; i < 24; i++) {
            sessionHourCombo.getItems().add(String.format("%02d", i));
        }
        
        // Setup minutes (0, 15, 30, 45)
        sessionMinuteCombo.getItems().addAll("00", "15", "30", "45");
        
        // Set default values
        sessionHourCombo.setValue("09");
        sessionMinuteCombo.setValue("00");
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
            
            // Load training plans
            List<TrainingPlan> plans = trainingPlanService.getAllTrainingPlans();
            trainingPlanComboBox.getItems().addAll(plans);
            
            // Setup training plan display
            trainingPlanComboBox.setCellFactory(param -> new ListCell<TrainingPlan>() {
                @Override
                protected void updateItem(TrainingPlan plan, boolean empty) {
                    super.updateItem(plan, empty);
                    if (empty || plan == null) {
                        setText(null);
                    } else {
                        setText(plan.getTitle() + " - " + plan.getFocusArea() + " (" + plan.getDifficultyLevel() + ")");
                    }
                }
            });
            trainingPlanComboBox.setButtonCell(new ListCell<TrainingPlan>() {
                @Override
                protected void updateItem(TrainingPlan plan, boolean empty) {
                    super.updateItem(plan, empty);
                    if (empty || plan == null) {
                        setText(null);
                    } else {
                        setText(plan.getTitle() + " - " + plan.getFocusArea() + " (" + plan.getDifficultyLevel() + ")");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/list_coaching_sessions.fxml"));
            Parent root = loader.load();
            
            Controllers.coachingsession.ListCoachingSessionsController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Coaching Sessions - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot go back to sessions list: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            // Parse date and time
            LocalDateTime sessionDate = LocalDateTime.of(
                sessionDatePicker.getValue(),
                java.time.LocalTime.of(
                    Integer.parseInt(sessionHourCombo.getValue()),
                    Integer.parseInt(sessionMinuteCombo.getValue())
                )
            );

            // Validate dropdown selections
            if (coachComboBox.getValue() == null) {
                showAlert("Validation Error", "Please select a coach.");
                return;
            }
            if (teamComboBox.getValue() == null) {
                showAlert("Validation Error", "Please select a team.");
                return;
            }
            if (trainingPlanComboBox.getValue() == null) {
                showAlert("Validation Error", "Please select a training plan.");
                return;
            }
            
            // Create coaching session
            CoachingSession session = new CoachingSession();
            session.setCoachId(coachComboBox.getValue().getId());
            session.setTeamId(teamComboBox.getValue().getId());
            session.setSessionDate(sessionDate);
            session.setDuration(Integer.parseInt(durationField.getText()));
            session.setNotes(notesArea.getText());
            session.setTrainingPlanId(trainingPlanComboBox.getValue().getId());

            coachingSessionService.createCoachingSession(session);
            
            showAlert("Success", "Coaching session created successfully!");
            handleBack(); // Go back to list

        } catch (Exception e) {
            showAlert("Error", "Failed to create coaching session: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        coachComboBox.setValue(null);
        teamComboBox.setValue(null);
        trainingPlanComboBox.setValue(null);
        sessionDatePicker.setValue(null);
        sessionHourCombo.setValue("09");
        sessionMinuteCombo.setValue("00");
        durationField.clear();
        notesArea.clear();
    }

    private boolean validateInput() {
        StringBuilder errorMessage = new StringBuilder();

        // Validate Coach Selection
        if (coachComboBox.getValue() == null) {
            errorMessage.append("Coach selection is required.\n");
        }

        // Validate Team Selection
        if (teamComboBox.getValue() == null) {
            errorMessage.append("Team selection is required.\n");
        }

        // Validate Date
        if (sessionDatePicker.getValue() == null) {
            errorMessage.append("Session date is required.\n");
        } else if (sessionDatePicker.getValue().isBefore(java.time.LocalDate.now())) {
            errorMessage.append("Session date cannot be in the past.\n");
        }

        // Validate Duration
        if (durationField.getText().trim().isEmpty()) {
            errorMessage.append("Duration is required.\n");
        } else {
            try {
                int duration = Integer.parseInt(durationField.getText().trim());
                if (duration <= 0) {
                    errorMessage.append("Duration must be positive.\n");
                } else if (duration > 480) { // 8 hours max
                    errorMessage.append("Duration cannot exceed 8 hours (480 minutes).\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Duration must be a number.\n");
            }
        }

        // Validate Training Plan Selection
        if (trainingPlanComboBox.getValue() == null) {
            errorMessage.append("Training Plan selection is required.\n");
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
