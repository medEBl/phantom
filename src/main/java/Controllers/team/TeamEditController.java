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

public class TeamEditController {
    
    private final ITeamService teamService = new TeamService();
    private IDashboardTeamController teamController;
    private Team currentTeam;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private ComboBox<String> gameComboBox;
    
    @FXML
    private ComboBox<Integer> coachComboBox;
    
    @FXML
    private TextField creationDateField;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    public void initialize() {
        setupGameComboBox();
        setupCoachComboBox();
        clearError();
        
        // Add listeners for real-time validation
        nameField.textProperty().addListener((obs, oldText, newText) -> validateForm());
        gameComboBox.valueProperty().addListener((obs, oldValue, newValue) -> validateForm());
        coachComboBox.valueProperty().addListener((obs, oldValue, newValue) -> validateForm());
    }
    
    private void setupGameComboBox() {
        gameComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
            "League of Legends",
            "Valorant", 
            "CS:GO",
            "Dota 2",
            "Overwatch",
            "Rocket League",
            "Fortnite",
            "Apex Legends"
        ));
    }
    
    private void setupCoachComboBox() {
        // Load valid coach IDs from service
        try {
            coachComboBox.setItems(javafx.collections.FXCollections.observableArrayList(
                teamService.getValidCoachIds()
            ));
        } catch (Exception e) {
            showError("Error loading coaches: " + e.getMessage());
        }
    }
    
    public void setTeam(Team team) {
        this.currentTeam = team;
        populateFields();
    }
    
    private void populateFields() {
        if (currentTeam != null) {
            nameField.setText(currentTeam.getName());
            gameComboBox.setValue(currentTeam.getGame());
            coachComboBox.setValue(currentTeam.getCoachId());
            
            if (currentTeam.getCreationDate() != null) {
                creationDateField.setText(currentTeam.getCreationDate().toString());
            }
            
            validateForm();
        }
    }
    
    private void validateForm() {
        boolean isValid = nameField.getText() != null && !nameField.getText().trim().isEmpty() &&
                         gameComboBox.getValue() != null &&
                         coachComboBox.getValue() != null;
        
        saveButton.setDisable(!isValid);
    }
    
    @FXML
    private void handleSave() {
        try {
            clearError();
            
            String name = nameField.getText().trim();
            String game = gameComboBox.getValue();
            Integer coachId = coachComboBox.getValue();
            
            // Validate input
            if (name.isEmpty()) {
                showError("Team name is required");
                return;
            }
            
            if (game == null) {
                showError("Please select a game");
                return;
            }
            
            if (coachId == null) {
                showError("Please select a coach");
                return;
            }
            
            // Check if team name already exists (excluding current team)
            if (!name.equals(currentTeam.getName()) && teamService.teamNameExists(name)) {
                showError("Team name already exists");
                return;
            }
            
            // Update team
            currentTeam.setName(name);
            currentTeam.setGame(game);
            currentTeam.setCoachId(coachId);
            
            teamService.updateTeam(currentTeam);
            
            showSuccess("Team updated successfully!");
            navigateBackToTeamList();
            
        } catch (Exception e) {
            showError("Error updating team: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateBackToTeamList();
    }
    
    private void handleClose() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void navigateBackToTeamList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Teams - Phantom App");
            stage.show();
            
            // Refresh the team list
            if (teamController != null) {
                teamController.refreshTeams();
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
    
    public void setTeamController(TeamController teamController) {
        this.teamController = teamController;
    }
}
