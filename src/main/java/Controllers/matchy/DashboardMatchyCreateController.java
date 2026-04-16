package Controllers.matchy;

import Iservices.matchy.IMatchyService;
import Iservices.team.ITeamService;
import entities.matchy.Matchy;
import entities.team.Team;
import javafx.collections.FXCollections;
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
import java.util.List;

public class DashboardMatchyCreateController {
    
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
    private Button createButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    private IDashboardMatchyController matchyController;
    
    @FXML
    public void initialize() {
        // Initialize status options
        statusComboBox.getItems().addAll("scheduled", "in_progress", "finished", "cancelled");
        statusComboBox.setValue("scheduled");
        
        // Load teams from database using real TeamService
        setupTeamComboBoxes();
        
        // Set default date to today
        matchDatePicker.setValue(LocalDate.now());
        
        // Set up score field logic control - enable only when status is 'finished'
        setupScoreFieldLogic();
    }
    
    private void setupTeamComboBoxes() {
        // Use real TeamService like frontend
        services.team.TeamService teamService = new services.team.TeamService();
        
        List<Team> teams = teamService.getAllTeams();
        team1ComboBox.setItems(FXCollections.observableArrayList(teams));
        team2ComboBox.setItems(FXCollections.observableArrayList(teams));
        
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
        // Initially disable score fields (default status is 'scheduled')
        score1Field.setDisable(true);
        score2Field.setDisable(true);
        score1Field.setEditable(false);
        score2Field.setEditable(false);
        
        // Add listener to status combo box
        statusComboBox.setOnAction(e -> {
            String selectedStatus = statusComboBox.getValue();
            boolean isFinished = "finished".equals(selectedStatus);
            
            System.out.println("DEBUG: Status changed to: " + selectedStatus);
            System.out.println("DEBUG: isFinished: " + isFinished);
            
            // Enable/disable score fields based on status
            score1Field.setDisable(!isFinished);
            score2Field.setDisable(!isFinished);
            score1Field.setEditable(isFinished);
            score2Field.setEditable(isFinished);
            
            System.out.println("DEBUG: score1Field disabled: " + score1Field.isDisabled());
            System.out.println("DEBUG: score1Field editable: " + score1Field.isEditable());
            
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
            team1NameLabel.setText(selectedTeam.getName());
        } else {
            team1NameLabel.setText("");
        }
    }
    
    private void updateTeam2Name() {
        Team selectedTeam = team2ComboBox.getValue();
        if (selectedTeam != null) {
            team2NameLabel.setText(selectedTeam.getName());
        } else {
            team2NameLabel.setText("");
        }
    }
    
    @FXML
    private void handleCreateMatch() {
        if (!validateInput()) {
            return;
        }
        
        try {
            Matchy newMatch = createMatchFromFields();
            
            // Use real MatchyService to actually save to database
            services.matchy.MatchyService matchyService = new services.matchy.MatchyService();
            matchyService.createMatch(newMatch);
            
            showSuccess("Match created successfully!");
            
            // Return to match list
            handleBackToMatches();
            
        } catch (Exception e) {
            showError("Error creating match: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleBackToMatches() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/dashboardMatchy.fxml"));
            Parent root = loader.load();
            
            // Get the dashboard controller
            DashboardMatchyController dashboardController = loader.getController();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Matches Management - Phantom App");
            stage.show();
            
            // Load matches in the dashboard
            dashboardController.loadMatches();
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
        
        // Check if teams are different
        int team1Id = team1ComboBox.getValue().getId();
        int team2Id = team2ComboBox.getValue().getId();
        if (team1Id == team2Id) {
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
        
        if (statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty()) {
            showError("Please select a status.");
            return false;
        }
        
        // Validate coordinates if provided
        String latStr = latitudeField.getText().trim();
        if (!latStr.isEmpty()) {
            try {
                Double.parseDouble(latStr);
            } catch (NumberFormatException e) {
                showError("Please enter a valid latitude.");
                return false;
            }
        }
        
        String lonStr = longitudeField.getText().trim();
        if (!lonStr.isEmpty()) {
            try {
                Double.parseDouble(lonStr);
            } catch (NumberFormatException e) {
                showError("Please enter a valid longitude.");
                return false;
            }
        }
        
        // Validate scores if finished
        if ("finished".equals(statusComboBox.getValue())) {
            String score1Str = score1Field.getText().trim();
            String score2Str = score2Field.getText().trim();
            
            if (score1Str.isEmpty() || score2Str.isEmpty()) {
                showError("Please enter scores for finished matches.");
                return false;
            }
            
            try {
                Integer.parseInt(score1Str);
                Integer.parseInt(score2Str);
            } catch (NumberFormatException e) {
                showError("Please enter valid scores.");
                return false;
            }
        }
        
        return true;
    }
    
    private Matchy createMatchFromFields() {
        Matchy match = new Matchy();
        
        // Set game
        match.setGame(gameField.getText().trim());
        
        // Set teams with proper IDs and names from Team entities
        Team team1 = team1ComboBox.getValue();
        Team team2 = team2ComboBox.getValue();
        match.setTeam1Id(team1.getId());
        match.setTeam2Id(team2.getId());
        match.setTeam1Name(team1.getName());
        match.setTeam2Name(team2.getName());
        
        // Combine date and time
        String dateTimeStr = matchDatePicker.getValue().toString() + " " + matchTimeField.getText().trim();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime matchDateTime = LocalDateTime.parse(dateTimeStr, formatter);
            match.setMatchDate(matchDateTime);
            // Set formatted date for display
            match.setMatchDateFormatted(matchDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date/time format. Use HH:mm format for time.");
        }
        
        // Set status
        match.setStatus(statusComboBox.getValue());
        
        // Set location
        String locationText = locationField.getText().trim();
        match.setLocation(locationText.isEmpty() ? null : locationText);
        
        // Set coordinates
        String latStr = latitudeField.getText().trim();
        match.setLatitude(latStr.isEmpty() ? null : Double.parseDouble(latStr));
        
        String lonStr = longitudeField.getText().trim();
        match.setLongitude(lonStr.isEmpty() ? null : Double.parseDouble(lonStr));
        
        // Set scores if finished
        if ("finished".equals(statusComboBox.getValue())) {
            match.setScoreTeam1(Integer.parseInt(score1Field.getText()));
            match.setScoreTeam2(Integer.parseInt(score2Field.getText()));
            
            // Determine winner based on scores
            int score1 = Integer.parseInt(score1Field.getText());
            int score2 = Integer.parseInt(score2Field.getText());
            if (score1 > score2) {
                match.setWinnerTeamId(team1.getId());
                match.setWinnerTeamName(team1.getName());
            } else if (score2 > score1) {
                match.setWinnerTeamId(team2.getId());
                match.setWinnerTeamName(team2.getName());
            } else {
                // Draw - no winner
                match.setWinnerTeamId(null);
                match.setWinnerTeamName(null);
            }
        } else {
            // Not finished - no scores or winner
            match.setScoreTeam1(null);
            match.setScoreTeam2(null);
            match.setWinnerTeamId(null);
            match.setWinnerTeamName(null);
        }
        
        return match;
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
