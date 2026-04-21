package Controllers.team;

import entities.team.Team;
import Iservices.team.ITeamService;
import services.team.TeamService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class DashboardTeamCreateController {
    
    private final ITeamService teamService = new TeamService();
    private DashboardTeamsController teamsController;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private ComboBox<String> gameComboBox;
    
    @FXML
    private ComboBox<Integer> coachComboBox;
    
    @FXML
    private Button createButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    public void initialize() {
        setupGameComboBox();
        setupCoachComboBox();
        setupValidation();
    }
    
    private void setupGameComboBox() {
        String[] games = {"Valorant", "CS:GO", "League of Legends", "Dota 2", "Overwatch", "Fortnite"};
        gameComboBox.getItems().addAll(games);
    }
    
    private void setupCoachComboBox() {
        // Load coach IDs from 1 to 100
        for (int i = 1; i <= 100; i++) {
            coachComboBox.getItems().add(i);
        }
    }
    
    private void setupValidation() {
        // Real-time validation
        nameField.textProperty().addListener((obs, oldText, newText) -> validateForm());
        gameComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        coachComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }
    
    private void validateForm() {
        boolean isValid = validateInput();
        createButton.setDisable(!isValid);
    }
    
    private boolean validateInput() {
        String name = nameField.getText().trim();
        String game = gameComboBox.getValue();
        Integer coachId = coachComboBox.getValue();
        
        if (name.isEmpty()) {
            showError("Team name is required.");
            return false;
        }
        
        if (game == null || game.isEmpty()) {
            showError("Game selection is required.");
            return false;
        }
        
        if (coachId == null || coachId <= 0) {
            showError("Coach selection is required.");
            return false;
        }
        
        clearError();
        return true;
    }
    
    @FXML
    private void handleCreateTeam() {
        if (!validateInput()) {
            return;
        }
        
        try {
            Team newTeam = createTeamFromFields();
            teamService.createTeam(newTeam);
            
            showSuccess("Team created successfully!");
            
            // Return to team list
            handleBackToTeams();
            
        } catch (Exception e) {
            showError("Error creating team: " + e.getMessage());
        }
    }
    
    private Team createTeamFromFields() {
        Team team = new Team();
        team.setName(nameField.getText().trim());
        team.setGame(gameComboBox.getValue());
        team.setCoachId(coachComboBox.getValue());
        team.setCreationDate(LocalDateTime.now());
        return team;
    }
    
    @FXML
    private void handleBackToTeams() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/dashboardTeams.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Teams Management - Phantom App");
            stage.show();
            
            // Refresh the team list
            if (teamsController != null) {
                teamsController.refreshTeams();
            }
        } catch (Exception e) {
            showError("Error returning to team list: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setTeamsController(DashboardTeamsController teamsController) {
        this.teamsController = teamsController;
    }
}
