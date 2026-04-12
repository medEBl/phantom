package Controllers.user;

import entities.user.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateProfileInfo();
    }

    private void updateProfileInfo() {
        if (currentUser != null) {
            fullNameLabel.setText(currentUser.getFullName());
            emailLabel.setText(currentUser.getEmail());
            usernameLabel.setText(currentUser.getUsername());
            roleLabel.setText(currentUser.getRole());
            countryLabel.setText(currentUser.getCountry());
            birthDateLabel.setText(currentUser.getBirthDate().toString());
            pointsLabel.setText(String.valueOf(currentUser.getAchievementPoints()));
            activeStatusLabel.setText(currentUser.isActive() ? "Active" : "Inactive");
            activeStatusLabel.setStyle(currentUser.isActive() ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
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
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Home - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading home screen: " + e.getMessage());
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
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Edit Profile - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading edit profile screen: " + e.getMessage());
            showError("Cannot open edit profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleTournaments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navTournaments.getScene().getWindow();
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
            
            Stage stage = (Stage) navTeams.getScene().getWindow();
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
            
            Stage stage = (Stage) navTraining.getScene().getWindow();
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
            
            Stage stage = (Stage) navShop.getScene().getWindow();
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
            
            Stage stage = (Stage) navMatchy.getScene().getWindow();
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
            
            Stage stage = (Stage) navAgent.getScene().getWindow();
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
