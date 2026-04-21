package Controllers.team;

import entities.team.Team;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;

public class TeamViewController {
    
    private Team currentTeam;
    
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label gameId;
    
    @FXML
    private Label creationDateLabel;
    
    @FXML
    private Label coachIdLabel;
    
    @FXML
    private Label coachNameLabel;
    
    @FXML
    private Label teamIdLabel;
    
    @FXML
    private Button closeButton;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public void initialize() {
        // Initialize UI components
    }
    
    public void setTeam(Team team) {
        this.currentTeam = team;
        populateFields();
    }
    
    private void populateFields() {
        if (currentTeam != null) {
            teamIdLabel.setText(String.valueOf(currentTeam.getId()));
            nameLabel.setText(currentTeam.getName());
            gameId.setText(currentTeam.getGame());
            coachIdLabel.setText(String.valueOf(currentTeam.getCoachId()));
            coachNameLabel.setText(currentTeam.getCoachName());
            
            if (currentTeam.getCreationDate() != null) {
                creationDateLabel.setText(currentTeam.getCreationDate().format(dateFormatter));
            } else {
                creationDateLabel.setText("N/A");
            }
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}
