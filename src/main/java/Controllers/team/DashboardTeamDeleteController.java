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

public class DashboardTeamDeleteController {
    
    private Team currentTeam;
    private DashboardTeamsController teamsController;
    
    @FXML
    private Label teamIdLabel;
    
    @FXML
    private Label teamNameLabel;
    
    @FXML
    private Label teamGameLabel;
    
    @FXML
    private Label teamCoachLabel;
    
    @FXML
    private Button confirmButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    public void setTeam(Team team) {
        this.currentTeam = team;
        populateTeamDetails();
    }
    
    private void populateTeamDetails() {
        if (currentTeam != null) {
            teamIdLabel.setText(String.valueOf(currentTeam.getId()));
            teamNameLabel.setText(currentTeam.getName());
            teamGameLabel.setText(currentTeam.getGame());
            teamCoachLabel.setText(String.valueOf(currentTeam.getCoachId()));
        }
    }
    
    @FXML
    private void handleConfirmDelete() {
        if (currentTeam == null) {
            showError("No team selected for deletion.");
            return;
        }
        
        try {
            ITeamService teamService = new TeamService();
            teamService.deleteTeam(currentTeam.getId());
            
            showSuccess("Team deleted successfully!");
            
            // Return to team list
            handleBackToTeams();
            
        } catch (Exception e) {
            showError("Error deleting team: " + e.getMessage());
        }
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
