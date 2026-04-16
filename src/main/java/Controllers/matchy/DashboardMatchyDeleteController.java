package Controllers.matchy;

import Iservices.matchy.IMatchyService;
import entities.matchy.Matchy;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DashboardMatchyDeleteController {
    
    @FXML
    private Label matchIdLabel;
    
    @FXML
    private Label matchGameLabel;
    
    @FXML
    private Label matchTeam1Label;
    
    @FXML
    private Label matchTeam2Label;
    
    @FXML
    private Label matchDateLabel;
    
    @FXML
    private Button confirmButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    private Matchy currentMatch;
    private IDashboardMatchyController matchyController;
    
    public void setMatch(Matchy match) {
        this.currentMatch = match;
        populateFields();
    }
    
    private void populateFields() {
        if (currentMatch != null) {
            matchIdLabel.setText(String.valueOf(currentMatch.getId()));
            matchGameLabel.setText(currentMatch.getGame());
            matchTeam1Label.setText(currentMatch.getTeam1Name() != null ? currentMatch.getTeam1Name() : "Team " + currentMatch.getTeam1Id());
            matchTeam2Label.setText(currentMatch.getTeam2Name() != null ? currentMatch.getTeam2Name() : "Team " + currentMatch.getTeam2Id());
            matchDateLabel.setText(currentMatch.getMatchDate() != null ? currentMatch.getMatchDate().toString() : "Not set");
        }
    }
    
    @FXML
    private void handleConfirmDelete() {
        if (currentMatch == null) {
            showError("No match selected for deletion.");
            return;
        }
        
        try {
            IMatchyService matchyService = new IMatchyService() {
                @Override
                public void createMatch(Matchy match) {
                    // Implementation would go here
                }
                
                @Override
                public void updateMatch(Matchy match) {
                    // Implementation would go here
                }
                
                @Override
                public void deleteMatch(int id) {
                    // Implementation would go here
                }
                
                @Override
                public java.util.Optional<Matchy> getMatchById(int id) {
                    return java.util.Optional.empty();
                }
                
                @Override
                public java.util.List<Matchy> getAllMatches() {
                    return null;
                }
                
                @Override
                public java.util.List<Matchy> getMatchesByGame(String game) {
                    return null;
                }
                
                @Override
                public java.util.List<Matchy> getMatchesByStatus(String status) {
                    return null;
                }
                
                @Override
                public java.util.List<Matchy> getMatchesByTeam(int teamId) {
                    return null;
                }
                
                @Override
                public java.util.List<Matchy> getMatchesByDateRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
                    return null;
                }
                
                @Override
                public java.util.List<Matchy> getUpcomingMatches() {
                    return null;
                }
                
                @Override
                public java.util.List<Matchy> getFinishedMatches() {
                    return null;
                }
                
                @Override
                public void updateMatchResult(int matchId, int scoreTeam1, int scoreTeam2) {
                    // Implementation would go here
                }
                
                @Override
                public void cancelMatch(int matchId) {
                    // Implementation would go here
                }
            };
            
            matchyService.deleteMatch(currentMatch.getId());
            
            showSuccess("Match deleted successfully!");
            
            // Return to match list
            handleBackToMatches();
            
        } catch (Exception e) {
            showError("Error deleting match: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToMatches() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/dashboardMatchy.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Matches Management - Phantom App");
            stage.show();
            
            // Refresh the match list
            if (matchyController != null) {
                matchyController.loadMatches();
            }
        } catch (Exception e) {
            showError("Error returning to match list: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void setMatchyController(IDashboardMatchyController matchyController) {
        this.matchyController = matchyController;
    }
}
