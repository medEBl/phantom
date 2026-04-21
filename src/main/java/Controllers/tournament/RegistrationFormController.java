package Controllers.tournament;

import entities.tournament.Registration;
import entities.tournament.Tournament;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.tournament.RegistrationService;

import java.io.IOException;
import java.time.LocalDateTime;

public class RegistrationFormController {

    @FXML private Label tournamentLabel;
    @FXML private TextField teamNameField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private Button backButton;

    private final RegistrationService registrationService = new RegistrationService();
    private Tournament tournament;
    private entities.user.User currentUser;
    private boolean isUserMode = false;

    public void initData(Tournament tournament) {
        initData(tournament, false);
    }

    public void initData(Tournament tournament, boolean isUserMode) {
        this.tournament = tournament;
        this.isUserMode = isUserMode;
        tournamentLabel.setText("Tournament: " + tournament.getName());
    }

    public void initUserMode(entities.user.User user) {
        this.currentUser = user;
        this.isUserMode = true;
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        try {
            Registration r = new Registration();
            r.setTournamentId(tournament.getId());
            r.setTeamName(teamNameField.getText().trim());
            r.setContactEmail(emailField.getText().trim());
            r.setCreatedAt(LocalDateTime.now());

            registrationService.createRegistration(r);
            navigateToManagement();
        } catch (Exception e) {
            showError("Error registering team: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        navigateToManagement();
    }

    private boolean validateInput() {
        if (teamNameField.getText() == null || teamNameField.getText().isBlank() ||
            emailField.getText() == null || emailField.getText().isBlank()) {
            showError("All fields are required!");
            return false;
        }

        String email = emailField.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format!");
            return false;
        }

        // Check Max Teams Limit
        int currentRegistrations = registrationService.getRegistrationsByTournament(tournament.getId()).size();
        if (currentRegistrations >= tournament.getMaxTeams()) {
            showError("Tournament is full! Max " + tournament.getMaxTeams() + " teams allowed.");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setVisible(true);
    }

    private void navigateToManagement() {
        try {
            String fxmlPath = isUserMode ? "/tournament/fxml/list.fxml" : "/tournament/fxml/registration_management.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (isUserMode) {
                TournamentListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
            } else {
                RegistrationManagementController controller = loader.getController();
                controller.setTournament(tournament);
                // Also pass user to management if we have it (for consistency)
                if (currentUser != null) {
                    controller.initUserMode(currentUser);
                }
            }
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
