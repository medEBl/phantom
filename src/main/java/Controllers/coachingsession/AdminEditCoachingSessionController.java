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
import javafx.scene.text.Text;

import java.util.Optional;

public class AdminEditCoachingSessionController {

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
    private Text sessionInfoLabel;

    @FXML
    private TextField sessionIdField;

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
    private Button updateButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button cancelButton;

    private List<User> coaches;
    private List<Team> teams;
    private List<TrainingPlan> trainingPlans;

    @FXML
    public void initialize() {
        System.out.println("=== AdminEditCoachingSessionController initialized ===");
        setupComboBoxes();
        setupTimeComboBox();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (currentUserLabel != null && user != null) {
            currentUserLabel.setText("ADMIN: " + user.getFullName().toUpperCase());
        } else if (currentUserLabel != null) {
            currentUserLabel.setText("ADMIN: NOT LOGGED IN");
        }
        System.out.println("Current user set: " + (user != null ? user.getFullName() : "null"));
    }

    public void setCoachingSession(CoachingSession session) {
        this.currentSession = session;
        loadSessionData();
        System.out.println("Coaching session set: " + (session != null ? session.getId() : "null"));
    }

    // ==================== MÉTHODES AJOUTÉES POUR VIEW/EDIT MODE ====================

    public void setViewMode(boolean viewMode) {
        System.out.println("=== SET VIEW MODE: " + viewMode + " ===");
        if (viewMode) {
            disableAllFields(true);
            if (updateButton != null) updateButton.setVisible(false);
            if (deleteButton != null) deleteButton.setVisible(false);
            if (cancelButton != null) cancelButton.setText("CLOSE");
            if (sessionInfoLabel != null && currentSession != null) {
                sessionInfoLabel.setText("VIEW COACHING SESSION - Session ID: " + currentSession.getId());
            }
        }
    }

    public void setEditMode(boolean editMode) {
        System.out.println("=== SET EDIT MODE: " + editMode + " ===");
        if (editMode) {
            disableAllFields(false);
            if (updateButton != null) {
                updateButton.setText("UPDATE SESSION");
                updateButton.setVisible(true);
            }
            if (deleteButton != null) deleteButton.setVisible(true);
            if (cancelButton != null) cancelButton.setText("CANCEL");
            if (sessionInfoLabel != null && currentSession != null) {
                sessionInfoLabel.setText("EDIT COACHING SESSION - Session ID: " + currentSession.getId());
            }
        }
    }

    private void disableAllFields(boolean disable) {
        if (coachComboBox != null) coachComboBox.setDisable(disable);
        if (teamComboBox != null) teamComboBox.setDisable(disable);
        if (sessionDatePicker != null) sessionDatePicker.setDisable(disable);
        if (timeComboBox != null) timeComboBox.setDisable(disable);
        if (durationField != null) durationField.setDisable(disable);
        if (trainingPlanComboBox != null) trainingPlanComboBox.setDisable(disable);
        if (notesArea != null) notesArea.setDisable(disable);
    }

    // ==================== FIN DES MÉTHODES AJOUTÉES ====================

    private void loadSessionData() {
        if (currentSession == null) {
            System.out.println("No session to load");
            return;
        }

        System.out.println("Loading session data for ID: " + currentSession.getId());

        // Set session ID
        if (sessionIdField != null) {
            sessionIdField.setText(String.valueOf(currentSession.getId()));
        }
        if (sessionInfoLabel != null) {
            sessionInfoLabel.setText("Session ID: " + currentSession.getId() + " | Current Coach: " +
                    (currentSession.getCoachName() != null ? currentSession.getCoachName() : "Unknown"));
        }

        // Set coach
        if (currentSession.getCoachId() > 0 && coaches != null) {
            for (User coach : coaches) {
                if (coach.getId() == currentSession.getCoachId()) {
                    coachComboBox.setValue(coach.getFullName() + " (" + coach.getUsername() + ")");
                    break;
                }
            }
        }

        // Set team
        if (currentSession.getTeamId() > 0 && teams != null) {
            for (Team team : teams) {
                if (team.getId() == currentSession.getTeamId()) {
                    teamComboBox.setValue(team.getName() + " (" + team.getGame() + ")");
                    break;
                }
            }
        }

        // Set date and time
        if (currentSession.getSessionDate() != null) {
            sessionDatePicker.setValue(currentSession.getSessionDate().toLocalDate());
            LocalTime time = currentSession.getSessionDate().toLocalTime();
            String timeStr = String.format("%02d:%02d", time.getHour(), time.getMinute());
            timeComboBox.setValue(timeStr);
        }

        // Set duration
        if (durationField != null) {
            durationField.setText(String.valueOf(currentSession.getDuration()));
        }

        // Set training plan
        if (currentSession.getTrainingPlanId() > 0 && trainingPlans != null) {
            for (TrainingPlan plan : trainingPlans) {
                if (plan.getId() == currentSession.getTrainingPlanId()) {
                    trainingPlanComboBox.setValue(plan.getTitle() + " (" + plan.getDuration() + " min)");
                    break;
                }
            }
        } else {
            trainingPlanComboBox.setValue("No Training Plan");
        }

        // Set notes
        if (currentSession.getNotes() != null && notesArea != null) {
            notesArea.setText(currentSession.getNotes());
        }

        System.out.println("Session data loaded successfully");
    }

    private void setupComboBoxes() {
        // Setup coaches
        try {
            coaches = userService.getUsersByRole("COACH");
            if (coachComboBox != null) {
                coachComboBox.getItems().clear();
                for (User coach : coaches) {
                    coachComboBox.getItems().add(coach.getFullName() + " (" + coach.getUsername() + ")");
                }
                System.out.println("Loaded " + coaches.size() + " coaches");
            }
        } catch (Exception e) {
            System.err.println("Error loading coaches: " + e.getMessage());
            showAlert("Error", "Failed to load coaches: " + e.getMessage());
        }

        // Setup teams
        try {
            teams = teamService.getAllTeams();
            if (teamComboBox != null) {
                teamComboBox.getItems().clear();
                for (Team team : teams) {
                    teamComboBox.getItems().add(team.getName() + " (" + team.getGame() + ")");
                }
                System.out.println("Loaded " + teams.size() + " teams");
            }
        } catch (Exception e) {
            System.err.println("Error loading teams: " + e.getMessage());
            showAlert("Error", "Failed to load teams: " + e.getMessage());
        }

        // Setup training plans
        try {
            trainingPlans = trainingPlanService.getAllTrainingPlans();
            if (trainingPlanComboBox != null) {
                trainingPlanComboBox.getItems().clear();
                trainingPlanComboBox.getItems().add("No Training Plan");
                for (TrainingPlan plan : trainingPlans) {
                    trainingPlanComboBox.getItems().add(plan.getTitle() + " (" + plan.getDuration() + " min)");
                }
                System.out.println("Loaded " + trainingPlans.size() + " training plans");
            }
        } catch (Exception e) {
            System.err.println("Error loading training plans: " + e.getMessage());
            showAlert("Error", "Failed to load training plans: " + e.getMessage());
        }
    }

    private void setupTimeComboBox() {
        if (timeComboBox != null) {
            timeComboBox.getItems().clear();
            for (int hour = 0; hour < 24; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    String time = String.format("%02d:%02d", hour, minute);
                    timeComboBox.getItems().add(time);
                }
            }
            System.out.println("Loaded time slots");
        }
    }

    @FXML
    private void handleUpdate() {
        System.out.println("=== UPDATE SESSION ===");

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

            // Update coaching session
            currentSession.setCoachId(coachId);
            currentSession.setTeamId(teamId);
            currentSession.setSessionDate(sessionDateTime);
            currentSession.setDuration(duration);
            currentSession.setTrainingPlanId(trainingPlanId);
            currentSession.setNotes(notes);

            // Save to database
            coachingSessionService.updateCoachingSession(currentSession);

            System.out.println("Session updated successfully: " + currentSession.getId());
            showAlert("Success", "Coaching session updated successfully!");
            handleBack();

        } catch (Exception e) {
            System.err.println("Error updating session: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to update coaching session: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        System.out.println("=== DELETE SESSION ===");

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Coaching Session");
        confirmAlert.setContentText("Are you sure you want to delete this coaching session?\n\n" +
                "Session ID: " + currentSession.getId() + "\n" +
                "Coach: " + (currentSession.getCoachName() != null ? currentSession.getCoachName() : "Unknown") + "\n" +
                "Team: " + (currentSession.getTeamName() != null ? currentSession.getTeamName() : "Unknown") + "\n\n" +
                "This action cannot be undone!");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                coachingSessionService.deleteCoachingSession(currentSession.getId());
                System.out.println("Session deleted successfully: " + currentSession.getId());
                showAlert("Success", "Coaching session deleted successfully!");
                handleBack();
            } catch (Exception e) {
                System.err.println("Error deleting session: " + e.getMessage());
                e.printStackTrace();
                showAlert("Error", "Failed to delete coaching session: " + e.getMessage());
            }
        }
    }

    private int getCoachIdFromSelection(String selection) {
        if (selection == null) return 0;
        try {
            String username = selection.substring(selection.indexOf("(") + 1, selection.indexOf(")"));
            for (User coach : coaches) {
                if (coach.getUsername().equals(username)) {
                    return coach.getId();
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing coach selection: " + e.getMessage());
        }
        return 0;
    }

    private int getTeamIdFromSelection(String selection) {
        if (selection == null) return 0;
        try {
            String teamName = selection.substring(0, selection.indexOf("(")).trim();
            for (Team team : teams) {
                if (team.getName().equals(teamName)) {
                    return team.getId();
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing team selection: " + e.getMessage());
        }
        return 0;
    }

    private int getTrainingPlanIdFromSelection(String selection) {
        if (selection == null || selection.equals("No Training Plan")) return 0;
        try {
            String planTitle = selection.substring(0, selection.indexOf("(")).trim();
            for (TrainingPlan plan : trainingPlans) {
                if (plan.getTitle().equals(planTitle)) {
                    return plan.getId();
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing training plan selection: " + e.getMessage());
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
    private void handleCancel() {
        System.out.println("=== CANCEL ===");
        handleBack();
    }

    @FXML
    private void handleBack() {
        System.out.println("=== BACK TO LIST ===");
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
            stage.setTitle("Coaching Sessions - Phantom Admin");

        } catch (IOException e) {
            System.err.println("Cannot go back to list: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Cannot go back to list: " + e.getMessage());
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