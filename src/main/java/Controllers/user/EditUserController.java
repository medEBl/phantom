package Controllers.user;

import entities.user.User;
import services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.application.Platform;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

public class EditUserController {

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    private Button backButton;

    @FXML
    private Label currentUserInfo;

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
    private TextField preferredGameField;

    @FXML
    private ComboBox skillLevelComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField achievementPointsField;

    @FXML
    private Label errorLabel;

    @FXML
    private Label successLabel;

    @FXML
    private Button resetButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        System.out.println("EditUserController initialized");
        setupComboBoxes();
        setupValidationListeners();
        hideMessages();
    }
    private void setupValidationListeners() {
        // Real-time email validation
        if (emailField != null) {
            emailField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.trim().isEmpty() && !isValidEmail(newValue.trim())) {
                    emailField.setStyle("-fx-border-color: #ff2d2d;");
                } else {
                    emailField.setStyle("");
                }
            });
        }

        // Real-time username length validation
        if (usernameField != null) {
            usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.trim().isEmpty() && newValue.trim().length() < 3) {
                    usernameField.setStyle("-fx-border-color: #ff2d2d;");
                } else {
                    usernameField.setStyle("");
                }
            });
        }

        // Real-time password match validation
        if (passwordField != null && confirmPasswordField != null) {
            confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.isEmpty() && !newValue.equals(passwordField.getText())) {
                    confirmPasswordField.setStyle("-fx-border-color: #ff2d2d;");
                } else {
                    confirmPasswordField.setStyle("");
                }
            });
        }

        // Achievement points — digits only
        if (achievementPointsField != null) {
            achievementPointsField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    achievementPointsField.setText(oldValue);
                }
            });
        }
    }
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    private void setupComboBoxes() {
        if (roleComboBox != null) {
            roleComboBox.getItems().clear();
            roleComboBox.getItems().addAll("PLAYER", "ADMIN", "ORGANIZER", "COACH");
        }

        if (statusComboBox != null) {
            statusComboBox.getItems().clear();
            statusComboBox.getItems().addAll("Active", "Inactive");
        }
    }

    private void loadUserData() {
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName());
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            countryField.setText(currentUser.getCountry());
            roleComboBox.setValue(currentUser.getRole());
            statusComboBox.setValue(currentUser.isActive() ? "Active" : "Inactive");
            achievementPointsField.setText(String.valueOf(currentUser.getAchievementPoints()));
            
            // Update current user info display
            currentUserInfo.setText("Editing: " + currentUser.getFullName());
            currentUserInfo.setStyle("-fx-text-fill: #3498db;");
            
            // Clear password fields
            if (passwordField != null) {
                passwordField.clear();
                passwordField.setPromptText("Leave blank to keep current password");
            }
            if (confirmPasswordField != null) {
                confirmPasswordField.clear();
                confirmPasswordField.setPromptText("Leave blank to keep current password");
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/users.fxml"));
            Parent root = loader.load();

            // Pass admin user back to users controller
            Controllers.user.UsersController controller = loader.getController();

            // Create admin user context
            User adminUser = new User();
            adminUser.setFullName("ADMIN");
            adminUser.setEmail("admin@phantom.com");
            adminUser.setRole("ADMIN");
            adminUser.setUsername("admin");

            controller.setCurrentUser(adminUser);

            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Users Management - Phantom App");

        } catch (IOException e) {
            System.err.println("Cannot go back to users: " + e.getMessage());
            e.printStackTrace();
            showError("Cannot go back: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        loadUserData();
        hideMessages();
        showSuccess("Form has been reset to original values.");
    }

    @FXML
    private void handleDeleteUser() {
        if (currentUser == null) {
            showError("No user selected for deletion.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete User");
        confirmDialog.setContentText("Are you sure you want to delete user '" + currentUser.getFullName() + "'?\n\nThis action cannot be undone!");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteUser(currentUser.getId());
                showSuccess("User '" + currentUser.getFullName() + "' has been successfully deleted!");

                // Wait a moment then go back to users list
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        Platform.runLater(() -> {
                            try {
                                handleBack();
                            } catch (Exception e) {
                                System.err.println("Error navigating back: " + e.getMessage());
                            }
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();

            } catch (Exception e) {
                System.err.println("Failed to delete user: " + e.getMessage());
                e.printStackTrace();
                showError("Failed to delete user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleSaveUser() {
        if (currentUser == null) {
            showError("No user selected for editing.");
            return;
        }

        try {
            hideMessages();

            // Validate input
            if (!validateInput()) {
                return;
            }

            // Update user data
            currentUser.setFullName(fullNameField.getText().trim());
            currentUser.setUsername(usernameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            currentUser.setCountry(countryField.getText().trim());
            currentUser.setRole(roleComboBox.getValue());
            currentUser.setActive("Active".equals(statusComboBox.getValue()));

            // Update password only if provided
            if (passwordField != null && !passwordField.getText().trim().isEmpty()) {
                if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                    showError("Passwords do not match.");
                    return;
                }
                if (passwordField.getText().length() < 6) {
                    showError("Password must be at least 6 characters long.");
                    return;
                }
                currentUser.setPassword(passwordField.getText());
            }

            // Update achievement points
            if (achievementPointsField != null && !achievementPointsField.getText().trim().isEmpty()) {
                try {
                    int points = Integer.parseInt(achievementPointsField.getText().trim());
                    if (points < 0) {
                        showError("Achievement points cannot be negative.");
                        return;
                    }
                    currentUser.setAchievementPoints(points);
                } catch (NumberFormatException e) {
                    showError("Invalid achievement points. Please enter a valid number.");
                    return;
                }
            }

            // Save updated user
            userService.updateUser(currentUser);

            showSuccess("User '" + currentUser.getFullName() + "' has been successfully updated!");

            // Auto hide success message after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        if (successLabel != null) {
                            successLabel.setVisible(false);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Failed to update user: " + e.getMessage());
            e.printStackTrace();
            showError("Failed to update user: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Full name validation
        if (fullNameField.getText().trim().isEmpty()) {
            showError("Full name is required.");
            return false;
        }

        // Email validation
        if (!isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email address (e.g., user@example.com).");
            return false;
        }

        // Email uniqueness validation
        String newEmail = emailField.getText().trim();
        if (!newEmail.equals(currentUser.getEmail()) && userService.emailExists(newEmail)) {
            showError("Email '" + newEmail + "' is already taken by another user.");
            return false;
        }

        // Username validation
        if (usernameField.getText().trim().length() < 3) {
            showError("Username must be at least 3 characters long.");
            return false;
        }

        // Check username uniqueness (exclude current user)
        String newUsername = usernameField.getText().trim();
        if (!newUsername.equals(currentUser.getUsername()) && userService.usernameExists(newUsername)) {
            showError("Username '" + newUsername + "' is already taken by another user.");
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

        // Achievement points validation
        if (achievementPointsField.getText().trim().isEmpty()) {
            achievementPointsField.setText("0");
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        // Basic email validation
        if (email == null || email.isEmpty()) return false;

        boolean hasAt = email.contains("@");
        boolean hasDot = email.substring(email.indexOf("@")).contains(".");
        boolean atBeforeDot = email.indexOf("@") < email.lastIndexOf(".");
        boolean noSpaces = !email.contains(" ");

        return hasAt && hasDot && atBeforeDot && noSpaces;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setStyle("-fx-text-fill: #ff2d2d; -fx-font-size: 13px;");
        }
        if (successLabel != null) {
            successLabel.setVisible(false);
        }

        // Auto hide error after 5 seconds
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> {
                    if (errorLabel != null) {
                        errorLabel.setVisible(false);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showSuccess(String message) {
        if (successLabel != null) {
            successLabel.setText(message);
            successLabel.setVisible(true);
            successLabel.setStyle("-fx-text-fill: #2dff8b; -fx-font-size: 13px;");
        }
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    private void hideMessages() {
        if (errorLabel != null) errorLabel.setVisible(false);
        if (successLabel != null) successLabel.setVisible(false);
    }
}