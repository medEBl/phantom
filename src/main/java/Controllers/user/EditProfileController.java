package Controllers.user;

import entities.user.User;
import services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EditProfileController {

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    private TextField fullNameField;
    @FXML
    private Label fullNameError;

    @FXML
    private TextField countryField;
    @FXML
    private Label countryError;

    @FXML
    private TextField birthDateField;
    @FXML
    private Label birthDateError;

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private Label currentPasswordError;

    @FXML
    private PasswordField newPasswordField;
    @FXML
    private Label newPasswordError;

    @FXML
    private PasswordField confirmNewPasswordField;
    @FXML
    private Label confirmNewPasswordError;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label successLabel;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadUserData();
    }

    private void loadUserData() {
        if (currentUser != null) {
            fullNameField.setText(currentUser.getFullName());
            countryField.setText(currentUser.getCountry());
            birthDateField.setText(currentUser.getBirthDate().toString());
        }
    }

    @FXML
    private void handleSave() {
        clearAllErrors();
        clearSuccessLabel();

        String fullName = fullNameField.getText().trim();
        String country = countryField.getText().trim();
        String birthDateStr = birthDateField.getText().trim();
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = newPasswordField.getText().trim();
        String confirmNewPassword = confirmNewPasswordField.getText().trim();

        boolean hasErrors = false;

        // Validate full name
        List<String> fullNameErrors = validateFullName(fullName);
        if (!fullNameErrors.isEmpty()) {
            fullNameError.setText(String.join(", ", fullNameErrors));
            hasErrors = true;
        }

        // Validate country
        List<String> countryErrors = validateCountry(country);
        if (!countryErrors.isEmpty()) {
            countryError.setText(String.join(", ", countryErrors));
            hasErrors = true;
        }

        // Validate birth date
        List<String> birthDateErrors = validateBirthDate(birthDateStr);
        if (!birthDateErrors.isEmpty()) {
            birthDateError.setText(String.join(", ", birthDateErrors));
            hasErrors = true;
        }

        // Validate password if user wants to change it
        if (!newPassword.isEmpty()) {
            if (currentPassword.isEmpty()) {
                currentPasswordError.setText("Current password is required to change password");
                hasErrors = true;
            }

            List<String> newPasswordErrors = validatePassword(newPassword);
            if (!newPasswordErrors.isEmpty()) {
                newPasswordError.setText(String.join(", ", newPasswordErrors));
                hasErrors = true;
            }

            if (!newPassword.equals(confirmNewPassword)) {
                confirmNewPasswordError.setText("Passwords do not match");
                hasErrors = true;
            }
        }

        if (hasErrors) {
            return;
        }

        try {
            // Verify current password if user wants to change password
            if (!newPassword.isEmpty()) {
                if (!userService.login(currentUser.getEmail(), currentPassword).isPresent()) {
                    currentPasswordError.setText("Current password is incorrect");
                    return;
                }
            }

            // Update user information
            currentUser.setFullName(fullName);
            currentUser.setCountry(country);
            currentUser.setBirthDate(LocalDate.parse(birthDateStr));

            // Update password if provided
            if (!newPassword.isEmpty()) {
                userService.updatePassword(currentUser.getId(), newPassword);
            }

            // Save user changes
            userService.updateUser(currentUser);

            showSuccess("Profile updated successfully!");

            // Auto-redirect back to profile after 2 seconds
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> handleCancel());
                    }
                },
                2000
            );

        } catch (Exception e) {
            showError("Update failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/profile.fxml"));
            Parent root = loader.load();
            
            Controllers.user.ProfileController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("My Profile - Phantom App");
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error loading profile screen: " + e.getMessage());
            showError("Cannot return to profile: " + e.getMessage());
        }
    }

    // Validation methods (copied from UserController)
    private List<String> validateFullName(String fullName) {
        List<String> errors = new ArrayList<>();
        if (fullName == null || fullName.isBlank()) {
            errors.add("Full name is required");
            return errors;
        }
        if (fullName.length() < 2)
            errors.add("Full name must be at least 2 characters");
        if (fullName.length() > 100)
            errors.add("Full name must not exceed 100 characters");
        if (!fullName.matches("^[a-zA-ZÀ-ÿ\\s'\\-]+$"))
            errors.add("Full name can only contain letters, spaces, hyphens and apostrophes");
        return errors;
    }

    private List<String> validateCountry(String country) {
        List<String> errors = new ArrayList<>();
        if (country == null || country.isBlank()) {
            errors.add("Country is required");
            return errors;
        }
        if (country.length() > 50)
            errors.add("Country must not exceed 50 characters");
        if (!country.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$"))
            errors.add("Country can only contain letters, spaces and hyphens");
        return errors;
    }

    private List<String> validateBirthDate(String dateStr) {
        List<String> errors = new ArrayList<>();
        if (dateStr == null || dateStr.isBlank()) {
            errors.add("Birth date is required");
            return errors;
        }
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            errors.add("Invalid date format (use YYYY-MM-DD)");
            return errors;
        }
        int age = java.time.Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 13)
            errors.add("User must be at least 13 years old");
        if (age > 100)
            errors.add("Invalid birth date (over 100 years)");
        return errors;
    }

    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.isBlank()) {
            errors.add("Password is required");
            return errors;
        }
        if (password.length() < 8)
            errors.add("Password must be at least 8 characters");
        if (!password.matches(".*[A-Z].*"))
            errors.add("Password must contain at least one uppercase letter");
        if (!password.matches(".*[a-z].*"))
            errors.add("Password must contain at least one lowercase letter");
        if (!password.matches(".*[0-9].*"))
            errors.add("Password must contain at least one digit");
        if (!password.matches(".*[^A-Za-z0-9].*"))
            errors.add("Password must contain at least one special character");
        return errors;
    }

    private void clearAllErrors() {
        fullNameError.setText("");
        countryError.setText("");
        birthDateError.setText("");
        currentPasswordError.setText("");
        newPasswordError.setText("");
        confirmNewPasswordError.setText("");
    }

    private void clearSuccessLabel() {
        successLabel.setText("");
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        successLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
