package Controllers.matchy;

import entities.matchy.Matchy;
import Iservices.matchy.IMatchyService;
import services.matchy.MatchyService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MatchyDeleteController {
    
    private final IMatchyService matchyService = new MatchyService();
    private IDashboardMatchyController matchyController;
    private Matchy currentMatch;
    
    @FXML
    private Label matchIdLabel;
    
    @FXML
    private Label gameLabel;
    
    @FXML
    private Label team1Label;
    
    @FXML
    private Label team2Label;
    
    @FXML
    private Label dateLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Button confirmButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label warningLabel;
    
    public void initialize() {
        warningLabel.setVisible(false);
    }
    
    public void setMatch(Matchy match) {
        this.currentMatch = match;
        populateFields();
    }
    
    private void populateFields() {
        if (currentMatch != null) {
            matchIdLabel.setText("Match ID: " + currentMatch.getId());
            gameLabel.setText("Game: " + currentMatch.getGame());
            team1Label.setText("Team 1: " + (currentMatch.getTeam1Name() != null ? currentMatch.getTeam1Name() : "Team ID " + currentMatch.getTeam1Id()));
            team2Label.setText("Team 2: " + (currentMatch.getTeam2Name() != null ? currentMatch.getTeam2Name() : "Team ID " + currentMatch.getTeam2Id()));
            
            if (currentMatch.getMatchDate() != null) {
                dateLabel.setText("Date: " + currentMatch.getMatchDate().toString());
            } else {
                dateLabel.setText("Date: N/A");
            }
            
            statusLabel.setText("Status: " + currentMatch.getStatus().toUpperCase());
            
            // Show warning if match is finished
            if ("finished".equals(currentMatch.getStatus())) {
                warningLabel.setText("WARNING: This match is finished. Deleting it will remove the match result permanently!");
                warningLabel.setVisible(true);
            }
        }
    }
    
    @FXML
    private void handleConfirm() {
        try {
            matchyService.deleteMatch(currentMatch.getId());
            
            navigateBackToMatchyList();
        } catch (Exception e) {
            warningLabel.setText("Error deleting match: " + e.getMessage());
            warningLabel.setVisible(true);
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateBackToMatchyList();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    private void navigateBackToMatchyList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Matchy - Phantom App");
            stage.show();
            
            // Refresh the matchy list
            if (matchyController != null) {
                matchyController.loadMatches();
            }
        } catch (Exception e) {
            warningLabel.setText("Error returning to matchy list: " + e.getMessage());
            warningLabel.setVisible(true);
        }
    }
    
    public void setMatchyController(IDashboardMatchyController matchyController) {
        this.matchyController = matchyController;
    }
}
