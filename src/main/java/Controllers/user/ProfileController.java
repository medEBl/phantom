package Controllers.user;

import entities.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {

    private User currentUser;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label emailLabel;

    @FXML
    private Label usernameLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private Label countryLabel;

    @FXML
    private Label birthDateLabel;

    @FXML
    private Label pointsLabel;

    @FXML
    private Label activeStatusLabel;

    @FXML
    private Button backButton;

    @FXML
    private Button editButton;

    // Navigation buttons
    @FXML
    private Button navHome;

    @FXML
    private Button navProfile;

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
    public void initialize() {
        System.out.println("ProfileController initialized");
        // Les doublons ont été supprimés
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateProfileInfo();
    }

    private void updateProfileInfo() {
        if (currentUser != null) {
            fullNameLabel.setText(currentUser.getFullName() != null ? currentUser.getFullName() : "N/A");
            emailLabel.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "N/A");
            usernameLabel.setText(currentUser.getUsername() != null ? currentUser.getUsername() : "N/A");
            roleLabel.setText(currentUser.getRole() != null ? currentUser.getRole() : "N/A");
            countryLabel.setText(currentUser.getCountry() != null ? currentUser.getCountry() : "N/A");

            // Handle birth date safely
            if (currentUser.getBirthDate() != null) {
                birthDateLabel.setText(currentUser.getBirthDate().toString());
            } else {
                birthDateLabel.setText("Not specified");
            }

            pointsLabel.setText(String.valueOf(currentUser.getAchievementPoints()));

            boolean isActive = currentUser.isActive();
            activeStatusLabel.setText(isActive ? "Active" : "Inactive");
            activeStatusLabel.setStyle(isActive ? "-fx-text-fill: #2dff8b;" : "-fx-text-fill: #ff2d2d;");
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
            Parent root = loader.load();

            // Pass current user back to home controller
            Controllers.home.HomeController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Home - Phantom App");
            stage.show();

        } catch (Exception e) {
            System.err.println("Error loading home screen: " + e.getMessage());
            e.printStackTrace();
            showError("Cannot return to home: " + e.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/edit-profile.fxml"));
            Parent root = loader.load();

            // Pass current user to edit profile controller
            Controllers.user.EditProfileController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            Stage stage = (Stage) editButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Edit Profile - Phantom App");
            stage.show();

        } catch (Exception e) {
            System.err.println("Error loading edit profile screen: " + e.getMessage());
            e.printStackTrace();
            showError("Cannot open edit profile: " + e.getMessage());
        }
    }

    // Navigation handlers
    @FXML
    private void handleNavHome() {
        navigateTo("/home/home.fxml", "Home - Phantom App");
    }

    @FXML
    private void handleNavProfile() {
        // Already on profile page, do nothing or refresh
        System.out.println("Already on Profile page");
    }

    @FXML
    private void handleNavTournaments() {
        navigateTo("/tournament/fxml/list.fxml", "Tournaments - Phantom App");
    }

    @FXML
    private void handleNavTeams() {
        navigateTo("/team & matches/fxml/list.fxml", "Teams - Phantom App");
    }

    @FXML
    private void handleNavTraining() {
        navigateTo("/training/fxml/list.fxml", "Training - Phantom App");
    }

    @FXML
    private void handleNavShop() {
        navigateTo("/shop/fxml/shop.fxml", "Shop - Phantom App");
    }

    @FXML
    private void handleNavMatchy() {
        navigateTo("/match/fxml/list.fxml", "Matches - Phantom App");
    }

    @FXML
    private void handleNavAgent() {
        navigateTo("/agent/fxml/agent.fxml", "AI Agent - Phantom App");
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pass current user to the target controller if it accepts a user
            Object controller = loader.getController();
            if (controller instanceof Controllers.home.HomeController) {
                ((Controllers.home.HomeController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof Controllers.user.ProfileController) {
                ((Controllers.user.ProfileController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof Controllers.tournament.TournamentController) {
                // If TournamentController has setCurrentUser method
                // ((Controllers.tournament.TournamentController) controller).setCurrentUser(currentUser);
            }
            // Add more controllers as needed

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println("Cannot navigate to " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
            showError("Cannot open page: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}