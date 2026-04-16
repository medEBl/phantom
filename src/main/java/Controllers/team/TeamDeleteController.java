package Controllers.team;

import entities.team.Team;
import Iservices.team.ITeamService;
import services.team.TeamService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class TeamDeleteController {
    
    private Team currentTeam;
    private IDashboardTeamController teamController;
    
    @FXML
    private Text teamNameText;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Button confirmButton;
    
    public void initialize() {
        // Initialize UI components
    }
    
    public void setTeam(Team team) {
        this.currentTeam = team;
        if (teamNameText != null) {
            teamNameText.setText(team.getName());
        }
    }
    
    public void setTeamController(TeamController teamController) {
        this.teamController = teamController;
    }
    
    @FXML
    private void handleCancel() {
        navigateBackToTeamList();
    }
    
    @FXML
    private void handleConfirm() {
        if (currentTeam != null && teamController != null) {
            try {
                ITeamService teamService = new TeamService();
                teamService.deleteTeam(currentTeam.getId());
                
                navigateBackToTeamList();
            } catch (Exception e) {
                if (teamController != null) {
                    teamController.showError("Error deleting team: " + e.getMessage());
                }
            }
        }
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
            if (teamController != null) {
                teamController.showError("Error returning to team list: " + e.getMessage());
            }
        }
    }
}
