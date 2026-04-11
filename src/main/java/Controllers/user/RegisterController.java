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
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class RegisterController {

    private final UserService userService = new UserService();

    @FXML
    private TextField emailField;
    @FXML
    private Label emailError;

    @FXML
    private TextField usernameField;
    @FXML
    private Label usernameError;

    @FXML
    private TextField fullNameField;
    @FXML
    private Label fullNameError;

    @FXML
    private PasswordField passwordField;
    @FXML
    private Label passwordError;

    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label confirmPasswordError;

    @FXML
    private TextField countryField;
    @FXML
    private Label countryError;

    @FXML
    private TextField birthDateField;
    @FXML
    private Label birthDateError;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button registerButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label successLabel;

    @FXML
    public void initialize() {
        clearAllErrors();
        clearSuccessLabel();
        
        // Initialize role combo box
        roleComboBox.getItems().addAll("PLAYER", "COACH", "ORGANIZER", "ADMIN");
        roleComboBox.getSelectionModel().selectFirst(); // Default to PLAYER
    }

    @FXML
    private void handleRegister() {
        clearAllErrors();
        clearSuccessLabel();

        // Get input values
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String country = countryField.getText().trim();
        String birthDateStr = birthDateField.getText().trim();
        String role = roleComboBox.getValue();

        // Validate all fields
        boolean hasErrors = false;

        // Email validation
        List<String> emailErrors = validateEmail(email);
        if (!emailErrors.isEmpty()) {
            emailError.setText(String.join(", ", emailErrors));
            hasErrors = true;
        }

        // Username validation
        List<String> usernameErrors = validateUsername(username, null);
        if (!usernameErrors.isEmpty()) {
            usernameError.setText(String.join(", ", usernameErrors));
            hasErrors = true;
        }

        // Full Name validation
        List<String> fullNameErrors = validateFullName(fullName);
        if (!fullNameErrors.isEmpty()) {
            fullNameError.setText(String.join(", ", fullNameErrors));
            hasErrors = true;
        }

        // Password validation
        List<String> passwordErrors = validatePassword(password);
        if (!passwordErrors.isEmpty()) {
            passwordError.setText(String.join(", ", passwordErrors));
            hasErrors = true;
        }

        // Confirm password validation
        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Passwords do not match");
            hasErrors = true;
        }

        // Country validation
        List<String> countryErrors = validateCountry(country);
        if (!countryErrors.isEmpty()) {
            countryError.setText(String.join(", ", countryErrors));
            hasErrors = true;
        }

        // Birth Date validation
        List<String> birthDateErrors = validateBirthDate(birthDateStr);
        if (!birthDateErrors.isEmpty()) {
            birthDateError.setText(String.join(", ", birthDateErrors));
            hasErrors = true;
        }

        // Role validation
        List<String> roleErrors = validateRole(role);
        if (!roleErrors.isEmpty()) {
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        try {
            // Check if email already exists
            if (userService.emailExists(email)) {
                emailError.setText("This email is already registered");
                return;
            }

            // Check if username already exists
            if (userService.usernameExists(username)) {
                usernameError.setText("This username is already taken");
                return;
            }

            // Parse birth date
            LocalDate birthDate = LocalDate.parse(birthDateStr);

            // Create user
            String roles = buildRolesJson(role);
            User user = new User(email, roles, password, username, fullName, country, birthDate, role);
            
            userService.createUser(user);
            
            showSuccess("Registration successful! Please login with your new account.");
            clearFields();

            // Auto-redirect to login after 2 seconds
            new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        javafx.application.Platform.runLater(() -> handleLogin());
                    }
                },
                2000
            );

        } catch (Exception e) {
            showError("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();
            
        } catch (Exception e) {
            showError("Cannot open login screen: " + e.getMessage());
        }
    }

    // Validation methods (copied from UserController)
    private List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        if (email == null || email.isBlank()) {
            errors.add("Email is required");
            return errors;
        }
        if (email.length() > 180)
            errors.add("Email must not exceed 180 characters");
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            errors.add("Invalid email format");
        return errors;
    }

    private List<String> validateUsername(String username, Integer excludeId) {
        List<String> errors = new ArrayList<>();
        if (username == null || username.isBlank()) {
            errors.add("Username is required");
            return errors;
        }
        if (username.length() < 3)
            errors.add("Username must be at least 3 characters");
        if (username.length() > 50)
            errors.add("Username must not exceed 50 characters");
        if (!username.matches("^[a-zA-Z0-9_]+$"))
            errors.add("Username can only contain letters, numbers and underscores");
        return errors;
    }

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
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 13)
            errors.add("User must be at least 13 years old");
        if (age > 100)
            errors.add("Invalid birth date (over 100 years)");
        return errors;
    }

    private List<String> validateRole(String role) {
        List<String> errors = new ArrayList<>();
        if (role == null || role.isBlank()) {
            errors.add("Role is required");
            return errors;
        }
        if (!List.of("PLAYER", "COACH", "ORGANIZER", "ADMIN").contains(role.toUpperCase()))
            errors.add("Invalid role. Choose: PLAYER, COACH, ORGANIZER, ADMIN");
        return errors;
    }

    private String buildRolesJson(String role) {
        return switch (role) {
            case "ADMIN" -> "[\"ROLE_ADMIN\"]";
            case "COACH" -> "[\"ROLE_USER\",\"ROLE_COACH\"]";
            case "ORGANIZER" -> "[\"ROLE_USER\",\"ROLE_ORGANIZER\"]";
            default -> "[\"ROLE_USER\",\"ROLE_PLAYER\"]";
        };
    }

    private void clearAllErrors() {
        emailError.setText("");
        usernameError.setText("");
        fullNameError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        countryError.setText("");
        birthDateError.setText("");
    }

    private void clearSuccessLabel() {
        successLabel.setText("");
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Registration Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        emailField.clear();
        usernameField.clear();
        fullNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        countryField.clear();
        birthDateField.clear();
        roleComboBox.getSelectionModel().selectFirst();
    }
}
