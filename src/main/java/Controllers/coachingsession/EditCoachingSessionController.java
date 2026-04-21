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
import java.time.LocalDateTime;
import java.io.IOException;
import java.util.List;

public class EditCoachingSessionController {

    private final CoachingSessionService coachingSessionService = new CoachingSessionService();
    private final UserService userService = new UserService();
    private final TeamService teamService = new TeamService();
    private final TrainingPlanService trainingPlanService = new TrainingPlanService();
    private User currentUser;
    private CoachingSession currentSession;

    @FXML
    private Button backButton;
    
    @FXML
    private Label currentUserLabel;

    @FXML
    private TextField sessionIdField;
    
    @FXML
    private TextField coachIdField;
    
    @FXML
    private TextField teamIdField;
    
    @FXML
    private TextField trainingPlanIdField;
    
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
    private Button saveButton;
    
    @FXML
    private Button clearButton;

    @FXML
    public void initialize() {
        setupTimeComboBoxes();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            currentUserLabel.setText("USER: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("USER: NOT LOGGED IN");
        }
    }

    public void setCoachingSession(CoachingSession session) {
        this.currentSession = session;
        populateFields();
    }

    private void setupTimeComboBoxes() {
        // Setup hours (0-23)
        for (int i = 0; i < 24; i++) {
            sessionHourCombo.getItems().add(String.format("%02d", i));
        }
        
        // Setup minutes (0, 15, 30, 45)
        sessionMinuteCombo.getItems().addAll("00", "15", "30", "45");
    }
    
    
    private void populateFields() {
        System.out.println("=== POPULATE FIELDS START ===");
        if (currentSession == null) {
            System.out.println("ERROR: currentSession is null");
            return;
        }

        System.out.println("Session ID: " + currentSession.getId());
        System.out.println("Coach ID: " + currentSession.getCoachId());
        System.out.println("Team ID: " + currentSession.getTeamId());
        System.out.println("Training Plan ID: " + currentSession.getTrainingPlanId());

        sessionIdField.setText(String.valueOf(currentSession.getId()));
        coachIdField.setText(String.valueOf(currentSession.getCoachId()));
        teamIdField.setText(String.valueOf(currentSession.getTeamId()));
        trainingPlanIdField.setText(String.valueOf(currentSession.getTrainingPlanId()));
        notesArea.setText(currentSession.getNotes() != null ? currentSession.getNotes() : "");
        durationField.setText(String.valueOf(currentSession.getDuration()));

        if (currentSession.getSessionDate() != null) {
            sessionDatePicker.setValue(currentSession.getSessionDate().toLocalDate());
            sessionHourCombo.setValue(String.format("%02d", currentSession.getSessionDate().getHour()));
            sessionMinuteCombo.setValue(String.format("%02d", currentSession.getSessionDate().getMinute()));
        }

        // Disable session ID field
        sessionIdField.setDisable(true);
        System.out.println("=== POPULATE FIELDS END ===");
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
        System.out.println("=== HANDLE SAVE START ===");
        
        if (!validateInput()) {
            System.out.println("Validation failed");
            return;
        }

        try {
            System.out.println("Current session ID: " + (currentSession != null ? currentSession.getId() : "null"));
            
            // Parse date and time
            LocalDateTime sessionDate = LocalDateTime.of(
                sessionDatePicker.getValue(),
                java.time.LocalTime.of(
                    Integer.parseInt(sessionHourCombo.getValue()),
                    Integer.parseInt(sessionMinuteCombo.getValue())
                )
            );

            // Parse IDs from text fields
            int coachId = Integer.parseInt(coachIdField.getText().trim());
            int teamId = Integer.parseInt(teamIdField.getText().trim());
            int trainingPlanId = Integer.parseInt(trainingPlanIdField.getText().trim());
            
            System.out.println("Coach ID: " + coachId);
            System.out.println("Team ID: " + teamId);
            System.out.println("Training Plan ID: " + trainingPlanId);
            System.out.println("Session Date: " + sessionDate);
            System.out.println("Duration: " + durationField.getText());
            System.out.println("Notes: " + notesArea.getText());
            
            // Update coaching session
            currentSession.setCoachId(coachId);
            currentSession.setTeamId(teamId);
            currentSession.setSessionDate(sessionDate);
            currentSession.setDuration(Integer.parseInt(durationField.getText()));
            currentSession.setNotes(notesArea.getText());
            currentSession.setTrainingPlanId(trainingPlanId);

            System.out.println("Calling updateCoachingSession...");
            coachingSessionService.updateCoachingSession(currentSession);
            System.out.println("Update completed successfully");
            
            showAlert("Success", "Coaching session updated successfully!");
            handleBack(); // Go back to list

        } catch (Exception e) {
            System.err.println("Error in handleSave: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to update coaching session: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        populateFields(); // Reset to original values
    }

    private boolean validateInput() {
        System.out.println("=== VALIDATION START ===");
        StringBuilder errorMessage = new StringBuilder();

        // Validate Coach ID
        System.out.println("Coach ID text: '" + coachIdField.getText() + "'");
        if (coachIdField.getText().trim().isEmpty()) {
            errorMessage.append("Coach ID is required.\n");
        } else {
            try {
                int coachId = Integer.parseInt(coachIdField.getText().trim());
                if (coachId <= 0) {
                    errorMessage.append("Coach ID must be positive.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Coach ID must be a number.\n");
            }
        }

        // Validate Team ID
        System.out.println("Team ID text: '" + teamIdField.getText() + "'");
        if (teamIdField.getText().trim().isEmpty()) {
            errorMessage.append("Team ID is required.\n");
        } else {
            try {
                int teamId = Integer.parseInt(teamIdField.getText().trim());
                if (teamId <= 0) {
                    errorMessage.append("Team ID must be positive.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Team ID must be a number.\n");
            }
        }

        // Validate Training Plan ID
        System.out.println("Training Plan ID text: '" + trainingPlanIdField.getText() + "'");
        if (trainingPlanIdField.getText().trim().isEmpty()) {
            errorMessage.append("Training Plan ID is required.\n");
        } else {
            try {
                int planId = Integer.parseInt(trainingPlanIdField.getText().trim());
                if (planId <= 0) {
                    errorMessage.append("Training Plan ID must be positive.\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Training Plan ID must be a number.\n");
            }
        }

        // Validate Date
        System.out.println("Date selected: " + sessionDatePicker.getValue());
        if (sessionDatePicker.getValue() == null) {
            errorMessage.append("Session date is required.\n");
        }

        // Validate Duration
        System.out.println("Duration text: '" + durationField.getText() + "'");
        if (durationField.getText().trim().isEmpty()) {
            errorMessage.append("Duration is required.\n");
        } else {
            try {
                int duration = Integer.parseInt(durationField.getText().trim());
                System.out.println("Duration parsed: " + duration);
                if (duration <= 0) {
                    errorMessage.append("Duration must be positive.\n");
                } else if (duration > 480) { // 8 hours max
                    errorMessage.append("Duration cannot exceed 8 hours (480 minutes).\n");
                }
            } catch (NumberFormatException e) {
                errorMessage.append("Duration must be a number.\n");
            }
        }

        // Validate Time Selection
        System.out.println("Hour selected: " + sessionHourCombo.getValue());
        System.out.println("Minute selected: " + sessionMinuteCombo.getValue());
        if (sessionHourCombo.getValue() == null || sessionMinuteCombo.getValue() == null) {
            errorMessage.append("Time selection is required.\n");
        }

        if (errorMessage.length() > 0) {
            System.out.println("Validation errors: " + errorMessage.toString());
            showAlert("Validation Error", errorMessage.toString());
            return false;
        }

        System.out.println("=== VALIDATION PASSED ===");
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
