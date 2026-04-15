package Controllers.tournament;

import entities.tournament.Tournament;
import entities.tournament.TournamentReward;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.tournament.TournamentRewardService;

import java.io.IOException;

public class TournamentRewardFormController {

    @FXML private Text sectionTitle;
    @FXML private Label tournamentLabel;
    @FXML private TextField rankField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField valueField;
    @FXML private Label errorLabel;
    @FXML private Button backButton;

    private final TournamentRewardService rewardService = new TournamentRewardService();
    private Tournament tournament;
    private TournamentReward rewardToEdit;
    private entities.user.User currentUser;
    private boolean isUserMode = false;

    public void initUserMode(entities.user.User user) {
        this.currentUser = user;
        this.isUserMode = true;
    }

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Cash", "Skin", "Trophy", "Points", "Other"));
    }

    public void initData(Tournament tournament, TournamentReward reward) {
        this.tournament = tournament;
        this.rewardToEdit = reward;
        tournamentLabel.setText("Tournament: " + tournament.getName());

        if (reward != null) {
            sectionTitle.setText("EDIT REWARD");
            rankField.setText(String.valueOf(reward.getRank()));
            typeCombo.setValue(reward.getRewardType());
            valueField.setText(reward.getRewardValue());
        }
    }

    @FXML
    private void handleSave() {
        if (!validateInput()) return;

        try {
            TournamentReward r = (rewardToEdit == null) ? new TournamentReward() : rewardToEdit;
            r.setTournamentId(tournament.getId());
            r.setRank(Integer.parseInt(rankField.getText()));
            r.setRewardType(typeCombo.getValue());
            r.setRewardValue(valueField.getText());

            if (rewardToEdit == null) {
                rewardService.createReward(r);
            } else {
                rewardService.updateReward(r);
            }
            navigateToManagement();
        } catch (Exception e) {
            showError("Error saving reward: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        navigateToManagement();
    }

    private boolean validateInput() {
        if (rankField.getText() == null || rankField.getText().isBlank() || 
            typeCombo.getValue() == null || 
            valueField.getText() == null || valueField.getText().isBlank()) {
            showError("All fields are required!");
            return false;
        }

        try {
            int rank = Integer.parseInt(rankField.getText());
            if (rank <= 0) {
                showError("Rank must be a positive number!");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Rank must be a valid integer!");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setVisible(true);
    }

    private void navigateToManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/reward_management.fxml"));
            Parent root = loader.load();
            TournamentRewardManagementController controller = loader.getController();
            controller.setTournament(tournament);
            
            if (isUserMode) {
                controller.initUserMode(currentUser);
            }
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
