package Controllers.matchy;

import Iservices.matchy.IMatchyService;
import Iservices.team.ITeamService;
import entities.matchy.Matchy;
import entities.team.Team;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DashboardMatchyEditController {
    
    @FXML
    private TextField idField;
    
    @FXML
    private TextField gameField;
    
    @FXML
    private ComboBox<Team> team1ComboBox;
    
    @FXML
    private ComboBox<Team> team2ComboBox;
    
    @FXML
    private DatePicker matchDatePicker;
    
    @FXML
    private TextField matchTimeField;
    
    @FXML
    private ComboBox<String> statusComboBox;
    
    @FXML
    private TextField locationField;
    
    @FXML
    private TextField latitudeField;
    
    @FXML
    private TextField longitudeField;
    
    @FXML
    private TextField score1Field;
    
    @FXML
    private TextField score2Field;
    
    @FXML
    private Label team1NameLabel;
    
    @FXML
    private Label team2NameLabel;
    
    @FXML
    private Button updateButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    private Matchy currentMatch;
    private IDashboardMatchyController matchyController;
    
    @FXML
    public void initialize() {
        // Initialize status options
        statusComboBox.getItems().addAll("scheduled", "in_progress", "finished", "cancelled");
        
        // Load teams from database using real TeamService
        setupTeamComboBoxes();
        
        // Set up score field logic control - enable only when status is 'finished'
        setupScoreFieldLogic();
    }
    
    private void setupTeamComboBoxes() {
        // Use real TeamService like frontend
        services.team.TeamService teamService = new services.team.TeamService();
        
        java.util.List<Team> teams = teamService.getAllTeams();
        team1ComboBox.setItems(javafx.collections.FXCollections.observableArrayList(teams));
        team2ComboBox.setItems(javafx.collections.FXCollections.observableArrayList(teams));
        
        // Set up custom display to show only team names
        javafx.util.StringConverter<Team> teamConverter = new javafx.util.StringConverter<Team>() {
            @Override
            public String toString(Team team) {
                return team != null ? team.getName() : "";
            }
            
            @Override
            public Team fromString(String string) {
                return null; // Not needed for display
            }
        };
        
        team1ComboBox.setConverter(teamConverter);
        team2ComboBox.setConverter(teamConverter);
        
        // Set up team selection listeners to update name labels
        team1ComboBox.setOnAction(e -> updateTeam1Name());
        team2ComboBox.setOnAction(e -> updateTeam2Name());
    }
    
    private void setupScoreFieldLogic() {
        // Initially disable score fields (status will be set when loading match data)
        score1Field.setDisable(true);
        score2Field.setDisable(true);
        score1Field.setEditable(false);
        score2Field.setEditable(false);
        
        // Add listener to status combo box
        statusComboBox.setOnAction(e -> {
            String selectedStatus = statusComboBox.getValue();
            boolean isFinished = "finished".equals(selectedStatus);
            
            System.out.println("DEBUG EDIT: Status changed to: " + selectedStatus);
            System.out.println("DEBUG EDIT: isFinished: " + isFinished);
            
            // Enable/disable score fields based on status
            score1Field.setDisable(!isFinished);
            score2Field.setDisable(!isFinished);
            score1Field.setEditable(isFinished);
            score2Field.setEditable(isFinished);
            
            System.out.println("DEBUG EDIT: score1Field disabled: " + score1Field.isDisabled());
            System.out.println("DEBUG EDIT: score1Field editable: " + score1Field.isEditable());
            
            // Clear scores if status is not finished
            if (!isFinished) {
                score1Field.clear();
                score2Field.clear();
            }
        });
    }
    
    private void updateTeam1Name() {
        Team selectedTeam = team1ComboBox.getValue();
        if (selectedTeam != null) {
            team1NameLabel.setText("Game: " + selectedTeam.getGame() + " | Coach ID: " + selectedTeam.getCoachId());
        } else {
            team1NameLabel.setText("");
        }
    }
    
    private void updateTeam2Name() {
        Team selectedTeam = team2ComboBox.getValue();
        if (selectedTeam != null) {
            team2NameLabel.setText("Game: " + selectedTeam.getGame() + " | Coach ID: " + selectedTeam.getCoachId());
        } else {
            team2NameLabel.setText("");
        }
    }
    
    public void setMatch(Matchy match) {
        this.currentMatch = match;
        populateFields();
    }
    
    private void populateFields() {
        if (currentMatch != null) {
            idField.setText(String.valueOf(currentMatch.getId()));
            gameField.setText(currentMatch.getGame());
            statusComboBox.setValue(currentMatch.getStatus());
            
            // Find teams by ID and set them in ComboBox
            setupTeamSelection();
            
            // Parse match date and time
            if (currentMatch.getMatchDate() != null) {
                try {
                    LocalDateTime dateTime = currentMatch.getMatchDate();
                    matchDatePicker.setValue(dateTime.toLocalDate());
                    matchTimeField.setText(dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                } catch (Exception e) {
                    // If parsing fails, set default values
                    matchDatePicker.setValue(LocalDate.now());
                    matchTimeField.setText("12:00");
                }
            }
            
            locationField.setText(currentMatch.getLocation() != null ? currentMatch.getLocation() : "");
            
            // Set coordinates
            if (currentMatch.getLatitude() != null) {
                latitudeField.setText(currentMatch.getLatitude().toString());
            }
            if (currentMatch.getLongitude() != null) {
                longitudeField.setText(currentMatch.getLongitude().toString());
            }
            
            // Set scores if available
            if (currentMatch.getScoreTeam1() != null) {
                score1Field.setText(currentMatch.getScoreTeam1().toString());
            }
            if (currentMatch.getScoreTeam2() != null) {
                score2Field.setText(currentMatch.getScoreTeam2().toString());
            }
        }
    }
    
    private void setupTeamSelection() {
        // Find teams by ID from the ComboBox items
        Team team1 = findTeamById(currentMatch.getTeam1Id());
        Team team2 = findTeamById(currentMatch.getTeam2Id());
        
        if (team1 != null) {
            team1ComboBox.setValue(team1);
        }
        if (team2 != null) {
            team2ComboBox.setValue(team2);
        }
    }
    
    private Team findTeamById(int teamId) {
        for (Team team : team1ComboBox.getItems()) {
            if (team.getId() == teamId) {
                return team;
            }
        }
        return null;
    }
    
    @FXML
    private void handleUpdateMatch() {
        if (!validateInput()) {
            return;
        }
        
        try {
            updateMatchFromFields();
            
            // Use real MatchyService to actually save to database
            services.matchy.MatchyService matchyService = new services.matchy.MatchyService();
            matchyService.updateMatch(currentMatch);
            
            showSuccess("Match updated successfully!");
            
            // Return to match list
            handleBackToMatches();
            
        } catch (Exception e) {
            showError("Error updating match: " + e.getMessage());
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
    
    private boolean validateInput() {
        if (gameField.getText() == null || gameField.getText().trim().isEmpty()) {
            showError("Please enter a game.");
            return false;
        }
        
        if (team1ComboBox.getValue() == null) {
            showError("Please select team 1.");
            return false;
        }
        
        if (team2ComboBox.getValue() == null) {
            showError("Please select team 2.");
            return false;
        }
        
        if (team1ComboBox.getValue().equals(team2ComboBox.getValue())) {
            showError("Team 1 and Team 2 cannot be the same.");
            return false;
        }
        
        if (matchDatePicker.getValue() == null) {
            showError("Please select a match date.");
            return false;
        }
        
        if (matchTimeField.getText() == null || matchTimeField.getText().trim().isEmpty()) {
            showError("Please enter match time.");
            return false;
        }
        
        // Validate time format (HH:MM)
        if (!matchTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showError("Please enter time in HH:MM format.");
            return false;
        }
        
        return true;
    }
    
    private void updateMatchFromFields() {
        currentMatch.setGame(gameField.getText().trim());
        
        // Set teams with proper IDs and names from Team entities
        Team team1 = team1ComboBox.getValue();
        Team team2 = team2ComboBox.getValue();
        currentMatch.setTeam1Id(team1.getId());
        currentMatch.setTeam2Id(team2.getId());
        currentMatch.setTeam1Name(team1.getName());
        currentMatch.setTeam2Name(team2.getName());
        
        // Combine date and time
        String dateTimeStr = matchDatePicker.getValue().toString() + " " + matchTimeField.getText().trim();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime matchDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            currentMatch.setMatchDate(matchDateTime);
            // Set formatted date for display
            currentMatch.setMatchDateFormatted(matchDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date/time format. Use HH:mm format for time.");
        }
        
        // Set status
        currentMatch.setStatus(statusComboBox.getValue());
        
        // Set location
        String locationText = locationField.getText().trim();
        currentMatch.setLocation(locationText.isEmpty() ? null : locationText);
        
        // Set coordinates
        String latStr = latitudeField.getText().trim();
        currentMatch.setLatitude(latStr.isEmpty() ? null : Double.parseDouble(latStr));
        
        String lonStr = longitudeField.getText().trim();
        currentMatch.setLongitude(lonStr.isEmpty() ? null : Double.parseDouble(lonStr));
        
        // Set scores if finished
        if ("finished".equals(statusComboBox.getValue())) {
            currentMatch.setScoreTeam1(Integer.parseInt(score1Field.getText()));
            currentMatch.setScoreTeam2(Integer.parseInt(score2Field.getText()));
            
            // Determine winner based on scores
            int score1 = Integer.parseInt(score1Field.getText());
            int score2 = Integer.parseInt(score2Field.getText());
            if (score1 > score2) {
                currentMatch.setWinnerTeamId(team1.getId());
                currentMatch.setWinnerTeamName(team1.getName());
            } else if (score2 > score1) {
                currentMatch.setWinnerTeamId(team2.getId());
                currentMatch.setWinnerTeamName(team2.getName());
            } else {
                // Draw - no winner
                currentMatch.setWinnerTeamId(null);
                currentMatch.setWinnerTeamName(null);
            }
        } else {
            // Not finished - no scores or winner
            currentMatch.setScoreTeam1(null);
            currentMatch.setScoreTeam2(null);
            currentMatch.setWinnerTeamId(null);
            currentMatch.setWinnerTeamName(null);
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
