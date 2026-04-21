package Controllers.tournament;

import entities.tournament.Tournament;
import entities.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.tournament.RegistrationService;
import services.tournament.TournamentService;

import java.io.IOException;
import java.util.List;

public class TournamentCardController {

    @FXML private Label gameLabel;
    @FXML private Label phaseLabel;
    @FXML private Text nameText;
    @FXML private Text dateText;
    @FXML private Text capacityText;
    @FXML private Button registerButton;
    @FXML private HBox adminActions;
    @FXML private VBox rewardsSection;
    @FXML private VBox rewardsList;

    private Tournament tournament;
    private User currentUser;
    private final TournamentService tournamentService = new TournamentService();
    private final services.tournament.TournamentRewardService rewardService = new services.tournament.TournamentRewardService();
    private final RegistrationService registrationService = new RegistrationService();

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setData(Tournament tournament) {
        this.tournament = tournament;
        
        gameLabel.setText(tournament.getGame().toUpperCase());
        nameText.setText(tournament.getName());
        dateText.setText(tournament.getStartDate() + " - " + tournament.getEndDate());
        
        String phase = tournament.getPhase();
        phaseLabel.setText(phase.replace("_", " ").toUpperCase());
        
        // Dynamic styling for phase
        if ("registrations_open".equals(phase)) {
            phaseLabel.getStyleClass().add("status-success");
        } else if ("ongoing".equals(phase)) {
            phaseLabel.getStyleClass().add("status-warning");
        } else {
            phaseLabel.getStyleClass().add("status-danger");
        }

        // Capacity check
        int currentCount = registrationService.getRegistrationsByTournament(tournament.getId()).size();
        capacityText.setText(currentCount + " / " + tournament.getMaxTeams() + " Teams");
        
        if (currentCount >= tournament.getMaxTeams() || !"registrations_open".equals(phase)) {
            registerButton.setDisable(true);
            registerButton.setText(currentCount >= tournament.getMaxTeams() ? "FULL" : "CLOSED");
        }

        // Register button visibility: Hide for Admins/Organizers
        if (currentUser != null) {
            boolean isPlayer = !isAuthorized("ADMIN") && !isAuthorized("ORGANIZER");
            registerButton.setVisible(isPlayer);
            registerButton.setManaged(isPlayer);
        }

        // Show management actions for Organizers/Admins
        if (currentUser != null) {
            boolean isAdmin = isAuthorized("ADMIN");
            boolean isOrganizer = isAuthorized("ORGANIZER");
            boolean isOwner = tournament.getOrganizerId() == currentUser.getId();

            if (isAdmin || (isOrganizer && isOwner)) {
                adminActions.setVisible(true);
                adminActions.setManaged(true);
            }
        }

        // Display rewards if they exist
        loadRewards();
    }

    private void loadRewards() {
        List<entities.tournament.TournamentReward> rewards = rewardService.getRewardsByTournament(tournament.getId());
        
        if (rewards != null && !rewards.isEmpty()) {
            rewardsSection.setVisible(true);
            rewardsSection.setManaged(true);
            rewardsList.getChildren().clear();
            
            for (entities.tournament.TournamentReward r : rewards) {
                HBox rewardRow = new HBox(10);
                rewardRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                
                String icon = switch (r.getRank()) {
                    case 1 -> "🥇";
                    case 2 -> "🥈";
                    case 3 -> "🥉";
                    default -> "🎖️";
                };
                
                Text rewardText = new Text(icon + " Rank " + r.getRank() + ": " + r.getRewardType() + " - " + r.getRewardValue());
                rewardText.setFill(javafx.scene.paint.Color.web("#DDDDDD"));
                rewardText.setStyle("-fx-font-size: 13;");
                
                rewardRow.getChildren().add(rewardText);
                rewardsList.getChildren().add(rewardRow);
            }
        } else {
            rewardsSection.setVisible(false);
            rewardsSection.setManaged(false);
        }
    }

    private boolean isAuthorized(String targetRole) {
        if (currentUser == null) return false;
        
        String role = currentUser.getRole();
        String jsonRoles = currentUser.getRoles();
        
        // Check singular role
        if (targetRole.equalsIgnoreCase(role) || ("ROLE_" + targetRole).equalsIgnoreCase(role)) return true;
        
        // Use ADMIN role as any-role for some checks, but here targetRole is specific
        if ("ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role)) return true;
        
        // Fallback: check plural roles JSON string (Symfony compatibility)
        if (jsonRoles != null) {
            if (jsonRoles.contains("ROLE_ADMIN")) return true;
            if ("ORGANIZER".equals(targetRole) && jsonRoles.contains("ROLE_ORGANIZER")) return true;
        }
        
        return false;
    }

    @FXML
    private void handleViewRegistrations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/registration_management.fxml"));
            Parent root = loader.load();
            
            RegistrationManagementController controller = loader.getController();
            controller.setTournament(tournament);
            controller.initUserMode(currentUser);
            
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/form.fxml"));
            Parent root = loader.load();
            
            TournamentFormController controller = loader.getController();
            controller.initUserMode(currentUser);
            controller.setTournament(tournament);
            
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Tournament");
        alert.setHeaderText("Are you sure you want to delete this tournament?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            tournamentService.deleteTournament(tournament.getId());
            // Refresh parent view
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/list.fxml"));
                Parent root = loader.load();
                
                TournamentListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                
                Stage stage = (Stage) registerButton.getScene().getWindow();
                stage.setScene(new Scene(root, 1920, 1080));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/registration_form.fxml"));
            Parent root = loader.load();
            
            RegistrationFormController controller = loader.getController();
            controller.initData(tournament, true);
            if (currentUser != null) {
                controller.initUserMode(currentUser);
            }
            
            Stage stage = (Stage) registerButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
