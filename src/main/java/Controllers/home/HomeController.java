package Controllers.home;

import entities.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HomeController {

    private User currentUser;

    @FXML
    private Label userLabel;

    @FXML
    private Text welcomeText;

    @FXML
    private Text roleText;

    @FXML
    private Text pointsText;

    @FXML
    private Text tournamentsText;

    @FXML
    private Text teamsText;

    @FXML
    private Button logoutButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button tournamentButton;

    @FXML
    private Button teamButton;

    @FXML
    private Button trainingButton;

    @FXML
    private Button shopButton;

    @FXML
    private Button matchyButton;

    @FXML
    private Button editProfileButton;

    @FXML
    private Button navHome;

    @FXML
    private Button navTournaments;

    @FXML
    private Button navTeams;

    @FXML
    private Button navTraining;

    @FXML
    private Button navShop;

    @FXML
    private Button navMatchy;

    @FXML
    private Button navAgent;

    @FXML
    private Button agentButton;

    @FXML
    private Button navLeaderboard;

    @FXML
    private Text winRateText;

    @FXML
    private Label userRoleLabel;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInfo();
    }

    private void updateUserInfo() {
        if (currentUser != null) {
            userLabel.setText("Welcome, " + currentUser.getFullName());
            userRoleLabel.setText(currentUser.getRole().toString());
            welcomeText.setText("Welcome, " + currentUser.getFullName().toUpperCase() + "!");
            roleText.setText("Your journey to esports excellence starts here");
            pointsText.setText("2,847");
            
            // TODO: Load actual statistics from services
            tournamentsText.setText("42");
            teamsText.setText("3");
            winRateText.setText("68%");
        }
    }

    @FXML
    private void handleEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/profile.fxml"));
            Parent root = loader.load();
            
            // Pass current user to profile controller
            Controllers.user.ProfileController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) editProfileButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("My Profile - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading profile screen: " + e.getMessage());
            showError("Cannot open profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleNavHome() {
        // Already on home page - just refresh
        updateUserInfo();
    }

    @FXML
    private void handleNavTournaments() {
        handleTournaments();
    }

    @FXML
    private void handleNavTeams() {
        handleTeams();
    }

    @FXML
    private void handleNavTraining() {
        handleTraining();
    }

    @FXML
    private void handleNavShop() {
        handleShop();
    }

    @FXML
    private void handleNavMatchy() {
        handleMatchy();
    }

    @FXML
    private void handleNavAgent() {
        handleAgent();
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Login - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading login screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/profile.fxml"));
            Parent root = loader.load();
            
            // Pass current user to profile controller
            Controllers.user.ProfileController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) profileButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("My Profile - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading profile screen: " + e.getMessage());
            showError("Cannot open profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleTournaments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) tournamentButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Tournaments - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading tournaments screen: " + e.getMessage());
            showError("Cannot open tournaments: " + e.getMessage());
        }
    }

    @FXML
    private void handleTeams() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) teamButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Teams - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading teams screen: " + e.getMessage());
            showError("Cannot open teams: " + e.getMessage());
        }
    }

    @FXML
    private void handleTraining() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/trainingplan/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) trainingButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Training Plans - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading training plans screen: " + e.getMessage());
            showError("Cannot open training plans: " + e.getMessage());
        }
    }

    @FXML
    private void handleShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) shopButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Shop - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading shop screen: " + e.getMessage());
            showError("Cannot open shop: " + e.getMessage());
        }
    }

    @FXML
    private void handleMatchy() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) matchyButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Matchy - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading matchy screen: " + e.getMessage());
            showError("Cannot open matchy: " + e.getMessage());
        }
    }

    @FXML
    private void handleAgent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/agent/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) agentButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Agent - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading agent screen: " + e.getMessage());
            showError("Cannot open agent: " + e.getMessage());
        }
    }

    @FXML
    private void handleLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/leaderboard/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navLeaderboard.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Leaderboard - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading leaderboard screen: " + e.getMessage());
            showError("Cannot open leaderboard: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAllActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navLeaderboard.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Activity - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading activity screen: " + e.getMessage());
            showError("Cannot open activity: " + e.getMessage());
        }
    }

    @FXML
    private void handleRewards() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/rewards/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navLeaderboard.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Rewards - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading rewards screen: " + e.getMessage());
            showError("Cannot open rewards: " + e.getMessage());
        }
    }

    @FXML
    private void handleFindMatch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/matchy/fxml/find.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navLeaderboard.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Find Match - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading find match screen: " + e.getMessage());
            showError("Cannot open find match: " + e.getMessage());
        }
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
