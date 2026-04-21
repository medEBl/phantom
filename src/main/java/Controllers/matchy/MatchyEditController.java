package Controllers.matchy;

import Iservices.team.ITeamService;
import entities.matchy.Matchy;
import Iservices.matchy.IMatchyService;
import entities.team.Team;
import javafx.collections.FXCollections;
import services.matchy.MatchyService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.team.TeamService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MatchyEditController {
    
    private final IMatchyService matchyService = new MatchyService();
    private IDashboardMatchyController matchyController;
    private Matchy currentMatch;
    
    @FXML
    private TextField gameField;
    
    @FXML
    private DatePicker matchDatePicker;
    
    @FXML
    private TextField matchTimeField;
    
    @FXML
    private ComboBox<String> statusComboBox;
    
    @FXML
    private ComboBox<Team> team1ComboBox;
    
    @FXML
    private ComboBox<Team> team2ComboBox;
    
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
    private Label matchIdLabel;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    @FXML
    private Label errorLabel;
    
    public void initialize() {
        setupStatusComboBox();
        setupTeamComboBoxes();
        setupValidation();
    }
    
    private void setupTeamComboBoxes() {
        ITeamService teamService = new TeamService();
        List<Team> teams = teamService.getAllTeams();
        
        team1ComboBox.setItems(FXCollections.observableArrayList(teams));
        team2ComboBox.setItems(FXCollections.observableArrayList(teams));
        
        // Set up team selection listeners to update name labels
        team1ComboBox.setOnAction(e -> updateTeam1Name());
        team2ComboBox.setOnAction(e -> updateTeam2Name());
    }
    
    private void updateTeam1Name() {
        Team selectedTeam = team1ComboBox.getValue();
        if (selectedTeam != null) {
            team1NameLabel.setText(selectedTeam.getName());
        } else {
            team1NameLabel.setText("");
        }
        validateForm();
    }
    
    private void updateTeam2Name() {
        Team selectedTeam = team2ComboBox.getValue();
        if (selectedTeam != null) {
            team2NameLabel.setText(selectedTeam.getName());
        } else {
            team2NameLabel.setText("");
        }
        validateForm();
    }
    
    private void setupStatusComboBox() {
        statusComboBox.getItems().addAll("planned", "ongoing", "finished", "cancelled");
        statusComboBox.setOnAction(e -> updateScoreFields());
    }
    
    private void setupValidation() {
        updateScoreFields();
        
        statusComboBox.setOnAction(e -> validateForm());
    }
    
    private void updateScoreFields() {
        String status = statusComboBox.getValue();
        boolean isFinished = "finished".equals(status);
        
        System.out.println("FRONTEND EDIT DEBUG: Status changed to: " + status);
        System.out.println("FRONTEND EDIT DEBUG: isFinished: " + isFinished);
        
        // AGGRESSIVE APPROACH: Try multiple methods to enable fields
        if (isFinished) {
            // Enable fields using multiple methods
            score1Field.setDisable(false);
            score2Field.setDisable(false);
            score1Field.setEditable(true);
            score2Field.setEditable(true);
            
            // Force focus and clear any CSS restrictions
            score1Field.setFocusTraversable(true);
            score2Field.setFocusTraversable(true);
            score1Field.setStyle(""); // Clear any disabling styles
            score2Field.setStyle(""); // Clear any disabling styles
            
            // Try to request focus to make them active
            javafx.application.Platform.runLater(() -> {
                score1Field.requestFocus();
            });
        } else {
            // Disable fields
            score1Field.setDisable(true);
            score2Field.setDisable(true);
            score1Field.setEditable(false);
            score2Field.setEditable(false);
            score1Field.setFocusTraversable(false);
            score2Field.setFocusTraversable(false);
            
            // Clear scores if status is not finished
            score1Field.clear();
            score2Field.clear();
        }
        
        System.out.println("FRONTEND EDIT DEBUG: score1Field disabled: " + score1Field.isDisabled());
        System.out.println("FRONTEND EDIT DEBUG: score1Field editable: " + score1Field.isEditable());
        System.out.println("FRONTEND EDIT DEBUG: score1Field focusTraversable: " + score1Field.isFocusTraversable());
    }
    
    private void loadTeamName(int teamNumber, int teamId) {
        try {
            if (teamNumber == 1) {
                team1NameLabel.setText("Team ID: " + teamId);
            } else {
                team2NameLabel.setText("Team ID: " + teamId);
            }
        } catch (Exception e) {
            if (teamNumber == 1) {
                team1NameLabel.setText("Invalid Team ID");
            } else {
                team2NameLabel.setText("Invalid Team ID");
            }
        }
    }
    
    private boolean validateTeam1() {
        Team selectedTeam = team1ComboBox.getValue();
        return selectedTeam != null && selectedTeam.getId() > 0;
    }
    
    private boolean validateTeam2() {
        Team selectedTeam = team2ComboBox.getValue();
        return selectedTeam != null && selectedTeam.getId() > 0;
    }
    
    private void validateForm() {
        boolean isValid = validateTeam1() && validateTeam2() && 
                         !gameField.getText().trim().isEmpty() &&
                         matchDatePicker.getValue() != null &&
                         !matchTimeField.getText().trim().isEmpty();
        
        if (validateTeam1() && validateTeam2()) {
            int team1Id = team1ComboBox.getValue().getId();
            int team2Id = team2ComboBox.getValue().getId();
            isValid = isValid && team1Id != team2Id;
        }
        
        if ("finished".equals(statusComboBox.getValue())) {
            isValid = isValid && !score1Field.getText().trim().isEmpty() && 
                             !score2Field.getText().trim().isEmpty() &&
                             isValidInteger(score1Field.getText()) &&
                             isValidInteger(score2Field.getText());
        }
        
        saveButton.setDisable(!isValid);
    }
    
    private boolean isValidInteger(String text) {
        try {
            Integer.parseInt(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public void setMatch(Matchy match) {
        this.currentMatch = match;
        populateFields();
    }
    
    private void populateFields() {
        if (currentMatch != null) {
            matchIdLabel.setText("Match ID: " + currentMatch.getId());
            gameField.setText(currentMatch.getGame());
            
            if (currentMatch.getMatchDate() != null) {
                matchDatePicker.setValue(currentMatch.getMatchDate().toLocalDate());
                matchTimeField.setText(currentMatch.getMatchDate().format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            
            statusComboBox.setValue(currentMatch.getStatus());
            // Set team selections
            ITeamService teamService = new TeamService();
            List<Team> teams = teamService.getAllTeams();
            team1ComboBox.setItems(FXCollections.observableArrayList(teams));
            team2ComboBox.setItems(FXCollections.observableArrayList(teams));
            
            // Select current teams
            for (Team team : teams) {
                if (team.getId() == currentMatch.getTeam1Id()) {
                    team1ComboBox.setValue(team);
                }
                if (team.getId() == currentMatch.getTeam2Id()) {
                    team2ComboBox.setValue(team);
                }
            }
            
            if (currentMatch.getLocation() != null) {
                locationField.setText(currentMatch.getLocation());
            }
            
            if (currentMatch.getLatitude() != null) {
                latitudeField.setText(currentMatch.getLatitude().toString());
            }
            
            if (currentMatch.getLongitude() != null) {
                longitudeField.setText(currentMatch.getLongitude().toString());
            }
            
            if (currentMatch.getScoreTeam1() != null) {
                score1Field.setText(currentMatch.getScoreTeam1().toString());
            }
            
            if (currentMatch.getScoreTeam2() != null) {
                score2Field.setText(currentMatch.getScoreTeam2().toString());
            }
        }
    }
    
    @FXML
    private void handleSave() {
        try {
            if (!validateInput()) {
                return;
            }
            
            updateMatchFromFields();
            matchyService.updateMatch(currentMatch);
            
            matchyController.showSuccess("Match updated successfully!");
            navigateBackToMatchyList();
            
        } catch (Exception e) {
            errorLabel.setText("Error updating match: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
    
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (gameField.getText().trim().isEmpty()) {
            errors.append("Game is required.\n");
        }
        
        if (matchDatePicker.getValue() == null) {
            errors.append("Match date is required.\n");
        }
        
        if (matchTimeField.getText().trim().isEmpty()) {
            errors.append("Match time is required.\n");
        }
        
        if (!validateTeam1()) {
            errors.append("Team 1 selection is required.\n");
        }
        
        if (!validateTeam2()) {
            errors.append("Team 2 selection is required.\n");
        }
        
        if (validateTeam1() && validateTeam2()) {
            int team1Id = team1ComboBox.getValue().getId();
            int team2Id = team2ComboBox.getValue().getId();
            if (team1Id == team2Id) {
                errors.append("Team 1 and Team 2 must be different.\n");
            }
        }
        
        if ("finished".equals(statusComboBox.getValue())) {
            if (score1Field.getText().trim().isEmpty() || score2Field.getText().trim().isEmpty()) {
                errors.append("Scores are required for finished matches.\n");
            } else if (!isValidInteger(score1Field.getText()) || !isValidInteger(score2Field.getText())) {
                errors.append("Scores must be valid numbers.\n");
            }
        }
        
        if (errors.length() > 0) {
            errorLabel.setText(errors.toString());
            errorLabel.setVisible(true);
            return false;
        }
        
        errorLabel.setVisible(false);
        return true;
    }
    
    private void updateMatchFromFields() {
        currentMatch.setGame(gameField.getText().trim());
        
        String dateTimeStr = matchDatePicker.getValue().toString() + " " + matchTimeField.getText().trim();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            currentMatch.setMatchDate(LocalDateTime.parse(dateTimeStr, formatter));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date/time format. Use HH:mm format for time.");
        }
        
        currentMatch.setStatus(statusComboBox.getValue());
        currentMatch.setTeam1Id(team1ComboBox.getValue().getId());
        currentMatch.setTeam2Id(team2ComboBox.getValue().getId());
        
        String location = locationField.getText().trim();
        currentMatch.setLocation(location.isEmpty() ? null : location);
        
        String latStr = latitudeField.getText().trim();
        currentMatch.setLatitude(latStr.isEmpty() ? null : Double.parseDouble(latStr));
        
        String lonStr = longitudeField.getText().trim();
        currentMatch.setLongitude(lonStr.isEmpty() ? null : Double.parseDouble(lonStr));
        
        if ("finished".equals(statusComboBox.getValue())) {
            currentMatch.setScoreTeam1(Integer.parseInt(score1Field.getText()));
            currentMatch.setScoreTeam2(Integer.parseInt(score2Field.getText()));
        } else {
            currentMatch.setScoreTeam1(null);
            currentMatch.setScoreTeam2(null);
            currentMatch.setWinnerTeamId(null);
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
            errorLabel.setText("Error returning to matchy list: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
    
    public void setMatchyController(IDashboardMatchyController matchyController) {
        this.matchyController = matchyController;
    }
}
