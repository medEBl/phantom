package Controllers.user;

import entities.user.User;
import services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import javafx.scene.Scene;
public class AddUserController {

    private final UserService userService = new UserService();
    private User currentUser;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private Button backButton;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField countryField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField achievementPointsField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    public void initialize() {
        setupComboBoxes();
    }

    private void setupComboBoxes() {
        // Setup role combo box
        roleComboBox.getItems().addAll("PLAYER", "ADMIN", "ORGANIZER", "COACH");
        roleComboBox.setValue("PLAYER");

        // Setup status combo box
        statusComboBox.getItems().addAll("Active", "Inactive");
        statusComboBox.setValue("Active");

    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/users.fxml"));
            Parent root = loader.load();

            // Pass admin user back to users controller
            Controllers.user.UsersController controller = loader.getController();

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Users Management - Phantom Admin");

        } catch (IOException e) {
            System.err.println("Cannot go back to users: " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        fullNameField.clear();
        usernameField.clear();
        emailField.clear();
        countryField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        achievementPointsField.clear();

        roleComboBox.setValue("PLAYER");
        statusComboBox.setValue("Active");

        hideMessages();
    }

    @FXML
    private void handleAddUser() {
        try {
            hideMessages();

            // Validate input
            if (!validateInput()) {
                return;
            }

            // Create new user
            User newUser = new User();
            newUser.setFullName(fullNameField.getText().trim());
            newUser.setUsername(usernameField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setCountry(countryField.getText().trim());
            newUser.setPassword(passwordField.getText());
            newUser.setRole(roleComboBox.getValue());
            newUser.setActive("Active".equals(statusComboBox.getValue()));
            newUser.setAchievementPoints(Integer.parseInt(achievementPointsField.getText().trim()));

            newUser.setBirthDate(LocalDate.now());

            // Save user
            userService.createUser(newUser);

            showSuccess("User '" + newUser.getFullName() + "' has been successfully added!");

            // Clear form after successful addition
            handleClear();

        } catch (Exception e) {
            showError("Failed to add user: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Full name validation
        if (fullNameField.getText().trim().isEmpty()) {
            showError("Full name is required.");
            return false;
        }

        // Username validation
        if (usernameField.getText().trim().isEmpty()) {
            showError("Username is required.");
            return false;
        }

        if (usernameField.getText().trim().length() < 3) {
            showError("Username must be at least 3 characters long.");
            return false;
        }

        // Email validation
        if (emailField.getText().trim().isEmpty()) {
            showError("Email is required.");
            return false;
        }

        if (!isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email address.");
            return false;
        }

        // Password validation
        if (passwordField.getText().isEmpty()) {
            showError("Password is required.");
            return false;
        }

        if (passwordField.getText().length() < 6) {
            showError("Password must be at least 6 characters long.");
            return false;
        }

        // Password confirmation
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match.");
            return false;
        }

        // Country validation
        if (countryField.getText().trim().isEmpty()) {
            showError("Country is required.");
            return false;
        }

        // Role validation
        if (roleComboBox.getValue() == null) {
            showError("Please select a role.");
            return false;
        }

        // Status validation
        if (statusComboBox.getValue() == null) {
            showError("Please select a status.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        successLabel.setVisible(false);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setVisible(true);
        errorLabel.setVisible(false);
    }

    private void hideMessages() {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);
    }
}
