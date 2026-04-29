package Controllers.tournament;

import entities.tournament.Tournament;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import services.tournament.TournamentService;

import java.io.IOException;
import java.time.LocalDate;

public class TournamentFormController {

    @FXML private BorderPane mainContainer;
    @FXML private Text formTitle;
    @FXML private Text sectionTitle;
    @FXML private TextField nameField;
    @FXML private TextField gameField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> phaseCombo;
    @FXML private TextField maxTeamsField;
    @FXML private TextField organizerIdField;
    @FXML private CheckBox activeCheck;
    @FXML private Label errorLabel;
    @FXML private Button backButton;
    @FXML private Button manageRewardsBtn;

    private final TournamentService tournamentService = new TournamentService();
    private Tournament tournamentToEdit;
    private entities.user.User currentUser;
    private boolean isUserMode = false;

    public void initUserMode(entities.user.User user) {
        this.currentUser = user;
        this.isUserMode = true;
        
        // Hide organizer ID field in user mode
        organizerIdField.setVisible(false);
        organizerIdField.setManaged(false);
    }

    @FXML
    public void initialize() {
        tools.AnimatedBackground.addAnimatedBackground(mainContainer);
        phaseCombo.setItems(FXCollections.observableArrayList("registrations_open", "ongoing", "finished"));
        phaseCombo.setValue("registrations_open");
    }

    public void setTournament(Tournament tournament) {
        this.tournamentToEdit = tournament;
        formTitle.setText("EDIT");
        sectionTitle.setText("EDIT TOURNAMENT: " + tournament.getName());
        
        nameField.setText(tournament.getName());
        gameField.setText(tournament.getGame());
        startDatePicker.setValue(tournament.getStartDate());
        endDatePicker.setValue(tournament.getEndDate());
        phaseCombo.setValue(tournament.getPhase());
        maxTeamsField.setText(String.valueOf(tournament.getMaxTeams()));
        organizerIdField.setText(String.valueOf(tournament.getOrganizerId()));
        activeCheck.setSelected(tournament.isActive());
        
        manageRewardsBtn.setVisible(true);
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        try {
            Tournament t = (tournamentToEdit == null) ? new Tournament() : tournamentToEdit;
            
            t.setName(nameField.getText());
            t.setGame(gameField.getText());
            t.setStartDate(startDatePicker.getValue());
            t.setEndDate(endDatePicker.getValue());
            t.setPhase(phaseCombo.getValue());
            t.setMaxTeams(Integer.parseInt(maxTeamsField.getText()));
            
            if (isUserMode && tournamentToEdit == null) {
                t.setOrganizerId(currentUser.getId());
            } else {
                t.setOrganizerId(Integer.parseInt(organizerIdField.getText()));
            }
            
            t.setActive(activeCheck.isSelected());

            if (tournamentToEdit == null) {
                tournamentService.createTournament(t);
            } else {
                tournamentService.updateTournament(t);
            }

            navigateToManagement();
        } catch (Exception e) {
            showError("Error saving tournament: " + e.getMessage());
        }
    }

    @FXML
    private void handleManageRewards() {
        if (tournamentToEdit == null) {
            showError("Please save the tournament first!");
            return;
        }
        
        try {
            java.net.URL fxmlLocation = getClass().getResource("/tournament/fxml/reward_management.fxml");
            if (fxmlLocation == null) {
                showError("Resource not found: /tournament/fxml/reward_management.fxml");
                return;
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();
            
            TournamentRewardManagementController controller = loader.getController();
            if (controller == null) {
                showError("Failed to initialize Reward Management controller.");
                return;
            }
            
            controller.setTournament(tournamentToEdit);
            if (isUserMode) {
                controller.initUserMode(currentUser);
            }
            
            Stage stage = (Stage) manageRewardsBtn.getScene().getWindow();
            if (stage == null) {
                showError("Could not determine window stage.");
                return;
            }
            
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (Exception e) {
            System.err.println("Navigation error: " + e.getMessage());
            e.printStackTrace();
            showError("Navigation error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        navigateToManagement();
    }

    private boolean validateInput() {
        if (nameField.getText() == null || nameField.getText().isBlank() || 
            gameField.getText() == null || gameField.getText().isBlank() || 
            startDatePicker.getValue() == null || endDatePicker.getValue() == null ||
            maxTeamsField.getText() == null || maxTeamsField.getText().isBlank() || 
            (!isUserMode && (organizerIdField.getText() == null || organizerIdField.getText().isBlank())) ||
            phaseCombo.getValue() == null) {
            showError("All required fields must be filled!");
            return false;
        }

        LocalDate now = LocalDate.now();
        if (startDatePicker.getValue().isBefore(now)) {
            showError("Start date cannot be in the past (must be today or later)!");
            return false;
        }

        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            showError("End date cannot be before start date!");
            return false;
        }

        try {
            int maxTeams = Integer.parseInt(maxTeamsField.getText());
            if (maxTeams <= 0) {
                showError("Max Teams must be a positive number!");
                return false;
            }

            if (!isUserMode) {
                int organizerId = Integer.parseInt(organizerIdField.getText());
            }
        } catch (NumberFormatException e) {
            showError("Input must be a valid number!");
            return false;
        }

        errorLabel.setVisible(false);
        return true;
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setVisible(true);
    }

    private void navigateToManagement() {
        try {
            String fxmlPath = isUserMode ? "/tournament/fxml/list.fxml" : "/tournament/fxml/management.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (isUserMode) {
                TournamentListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
            }
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
