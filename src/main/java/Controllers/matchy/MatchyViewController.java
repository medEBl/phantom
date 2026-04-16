package Controllers.matchy;

import entities.matchy.Matchy;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class MatchyViewController {
    
    private Matchy currentMatch;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    @FXML
    private Label matchIdLabel;
    
    @FXML
    private Label gameLabel;
    
    @FXML
    private Label matchDateLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Label team1IdLabel;
    
    @FXML
    private Label team1NameLabel;
    
    @FXML
    private Label team2IdLabel;
    
    @FXML
    private Label team2NameLabel;
    
    @FXML
    private Label scoreLabel;
    
    @FXML
    private Label winnerLabel;
    
    @FXML
    private Label locationLabel;
    
    @FXML
    private Label coordinatesLabel;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private Label statusBadge;
    
    public void initialize() {
        // Initialize UI components
    }
    
    public void setMatch(Matchy match) {
        this.currentMatch = match;
        populateFields();
    }
    
    private void populateFields() {
        if (currentMatch != null) {
            matchIdLabel.setText("Match ID: " + currentMatch.getId());
            gameLabel.setText("Game: " + currentMatch.getGame());
            
            if (currentMatch.getMatchDate() != null) {
                matchDateLabel.setText("Date & Time: " + currentMatch.getMatchDate().format(dateFormatter));
            } else {
                matchDateLabel.setText("Date & Time: N/A");
            }
            
            // Set status with color coding
            String status = currentMatch.getStatus().toUpperCase();
            statusLabel.setText("Status: " + status);
            statusBadge.setText(status);
            
            switch (currentMatch.getStatus().toLowerCase()) {
                case "planned":
                    statusBadge.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                    break;
                case "ongoing":
                    statusBadge.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                    break;
                case "finished":
                    statusBadge.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                    break;
                case "cancelled":
                    statusBadge.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
                    break;
                default:
                    statusBadge.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 5;");
            }
            
            team1IdLabel.setText("Team 1 ID: " + currentMatch.getTeam1Id());
            team1NameLabel.setText("Team 1 Name: " + (currentMatch.getTeam1Name() != null ? currentMatch.getTeam1Name() : "Unknown"));
            
            team2IdLabel.setText("Team 2 ID: " + currentMatch.getTeam2Id());
            team2NameLabel.setText("Team 2 Name: " + (currentMatch.getTeam2Name() != null ? currentMatch.getTeam2Name() : "Unknown"));
            
            // Set scores
            if (currentMatch.getScoreTeam1() != null && currentMatch.getScoreTeam2() != null) {
                scoreLabel.setText("Score: " + currentMatch.getScoreTeam1() + " - " + currentMatch.getScoreTeam2());
                
                // Determine winner
                if (currentMatch.getWinnerTeamId() != null) {
                    if (currentMatch.getWinnerTeamId() == currentMatch.getTeam1Id()) {
                        winnerLabel.setText("Winner: " + (currentMatch.getTeam1Name() != null ? currentMatch.getTeam1Name() : "Team 1"));
                    } else if (currentMatch.getWinnerTeamId() == currentMatch.getTeam2Id()) {
                        winnerLabel.setText("Winner: " + (currentMatch.getTeam2Name() != null ? currentMatch.getTeam2Name() : "Team 2"));
                    } else {
                        winnerLabel.setText("Winner: Unknown");
                    }
                } else {
                    // Check if it's a draw
                    if (currentMatch.getScoreTeam1().equals(currentMatch.getScoreTeam2())) {
                        winnerLabel.setText("Result: Draw");
                    } else {
                        winnerLabel.setText("Winner: Not determined");
                    }
                }
            } else {
                scoreLabel.setText("Score: Not played yet");
                winnerLabel.setText("Winner: Not determined");
            }
            
            // Set location
            if (currentMatch.getLocation() != null && !currentMatch.getLocation().trim().isEmpty()) {
                locationLabel.setText("Location: " + currentMatch.getLocation());
            } else {
                locationLabel.setText("Location: Not specified");
            }
            
            // Set coordinates
            if (currentMatch.getLatitude() != null && currentMatch.getLongitude() != null) {
                coordinatesLabel.setText("Coordinates: " + currentMatch.getLatitude() + ", " + currentMatch.getLongitude());
            } else {
                coordinatesLabel.setText("Coordinates: Not specified");
            }
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
