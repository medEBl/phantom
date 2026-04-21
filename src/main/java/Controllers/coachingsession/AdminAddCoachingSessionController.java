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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class AdminAddCoachingSessionController {

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
    private ComboBox<String> coachComboBox;

    @FXML
    private ComboBox<String> teamComboBox;

    @FXML
    private DatePicker sessionDatePicker;

    @FXML
    private ComboBox<String> timeComboBox;

    @FXML
    private TextField durationField;

    @FXML
    private ComboBox<String> trainingPlanComboBox;

    @FXML
    private TextArea notesArea;

    @FXML
    private Button createButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button cancelButton;

    private List<User> coaches;
    private List<Team> teams;
    private List<TrainingPlan> trainingPlans;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupTimeComboBox();
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
        // Setup coaches
        try {
            coaches = userService.getUsersByRole("COACH");
            coachComboBox.getItems().clear();
            for (User coach : coaches) {
                coachComboBox.getItems().add(coach.getFullName() + " (" + coach.getUsername() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error loading coaches: " + e.getMessage());
            showAlert("Error", "Failed to load coaches: " + e.getMessage());
        }

        // Setup teams
        try {
            teams = teamService.getAllTeams();
            teamComboBox.getItems().clear();
            for (Team team : teams) {
                teamComboBox.getItems().add(team.getName() + " (" + team.getGame() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error loading teams: " + e.getMessage());
            showAlert("Error", "Failed to load teams: " + e.getMessage());
        }

        // Setup training plans
        try {
            trainingPlans = trainingPlanService.getAllTrainingPlans();
            trainingPlanComboBox.getItems().clear();
            trainingPlanComboBox.getItems().add("No Training Plan");
            for (TrainingPlan plan : trainingPlans) {
                trainingPlanComboBox.getItems().add(plan.getTitle() + " (" + plan.getDuration() + " min)");
            }
            trainingPlanComboBox.setValue("No Training Plan");
        } catch (Exception e) {
            System.err.println("Error loading training plans: " + e.getMessage());
            showAlert("Error", "Failed to load training plans: " + e.getMessage());
        }
    }

    private void setupTimeComboBox() {
        timeComboBox.getItems().clear();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                String time = String.format("%02d:%02d", hour, minute);
                timeComboBox.getItems().add(time);
            }
        }
    }

    @FXML
    private void handleCreate() {
        if (!validateForm()) {
            return;
        }

        try {
            // Get selected coach ID
            String selectedCoach = coachComboBox.getValue();
            int coachId = getCoachIdFromSelection(selectedCoach);

            // Get selected team ID
            String selectedTeam = teamComboBox.getValue();
            int teamId = getTeamIdFromSelection(selectedTeam);

            // Get date and time
            LocalDate date = sessionDatePicker.getValue();
            String timeStr = timeComboBox.getValue();
            LocalTime time = LocalTime.parse(timeStr);
            LocalDateTime sessionDateTime = LocalDateTime.of(date, time);

            // Get duration
            int duration = Integer.parseInt(durationField.getText());

            // Get training plan ID
            int trainingPlanId = getTrainingPlanIdFromSelection(trainingPlanComboBox.getValue());

            // Get notes
            String notes = notesArea.getText() != null ? notesArea.getText() : "";

            // Create coaching session
            CoachingSession session = new CoachingSession();
            session.setCoachId(coachId);
            session.setTeamId(teamId);
            session.setSessionDate(sessionDateTime);
            session.setDuration(duration);
            session.setTrainingPlanId(trainingPlanId);
            session.setNotes(notes);

            // Save to database
            coachingSessionService.createCoachingSession(session);

            showAlert("Success", "Coaching session created successfully!");
            handleBack();

        } catch (Exception e) {
            showAlert("Error", "Failed to create coaching session: " + e.getMessage());
        }
    }

    private int getCoachIdFromSelection(String selection) {
        if (selection == null) return 0;
        String username = selection.substring(selection.indexOf("(") + 1, selection.indexOf(")"));
        for (User coach : coaches) {
            if (coach.getUsername().equals(username)) {
                return coach.getId();
            }
        }
        return 0;
    }

    private int getTeamIdFromSelection(String selection) {
        if (selection == null) return 0;
        String teamName = selection.substring(0, selection.indexOf("(")).trim();
        for (Team team : teams) {
            if (team.getName().equals(teamName)) {
                return team.getId();
            }
        }
        return 0;
    }

    private int getTrainingPlanIdFromSelection(String selection) {
        if (selection == null || selection.equals("No Training Plan")) return 0;
        String planTitle = selection.substring(0, selection.indexOf("(")).trim();
        for (TrainingPlan plan : trainingPlans) {
            if (plan.getTitle().equals(planTitle)) {
                return plan.getId();
            }
        }
        return 0;
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (coachComboBox.getValue() == null) {
            errors.append("Please select a coach.\n");
        }

        if (teamComboBox.getValue() == null) {
            errors.append("Please select a team.\n");
        }

        if (sessionDatePicker.getValue() == null) {
            errors.append("Please select a session date.\n");
        }

        if (timeComboBox.getValue() == null) {
            errors.append("Please select a session time.\n");
        }

        if (durationField.getText() == null || durationField.getText().trim().isEmpty()) {
            errors.append("Please enter duration.\n");
        } else {
            try {
                int duration = Integer.parseInt(durationField.getText());
                if (duration <= 0) {
                    errors.append("Duration must be greater than 0.\n");
                }
            } catch (NumberFormatException e) {
                errors.append("Duration must be a valid number.\n");
            }
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString());
            return false;
        }

        return true;
    }

    @FXML
    private void handleClear() {
        coachComboBox.setValue(null);
        teamComboBox.setValue(null);
        sessionDatePicker.setValue(null);
        timeComboBox.setValue(null);
        durationField.clear();
        trainingPlanComboBox.setValue("No Training Plan");
        notesArea.clear();
    }

    @FXML
    private void handleCancel() {
        handleBack();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coach/fxml/adminlist_coaching_sessions.fxml"));
            Parent root = loader.load();
            
            AdminListCoachingSessionController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Coaching - Phantom Admin");
            
        } catch (IOException e) {
            System.err.println("Cannot go back to backoffice: " + e.getMessage());
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
