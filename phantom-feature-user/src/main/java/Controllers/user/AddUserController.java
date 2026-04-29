package Controllers.user;

import entities.user.User;
import Iservices.user.IUserService;
import services.user.UserService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class AddUserController {

    @FXML private ScrollPane mainContainer;
    private User currentUser;

    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField fullNameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField countryField;
    @FXML private TextField birthDateField;
    @FXML private ComboBox<String> roleComboBox;
    
    @FXML private Label emailError;
    @FXML private Label usernameError;
    @FXML private Label fullNameError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label countryError;
    @FXML private Label birthDateError;
    @FXML private Label successLabel;
    
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button clearButton;

    private final IUserService userService = new UserService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        tools.AnimatedBackground.addAnimatedBackground(mainContainer);
        // Initialize role ComboBox
        roleComboBox.getItems().addAll("PLAYER", "COACH", "ORGANIZER", "ADMIN");
        roleComboBox.getSelectionModel().selectFirst();
        
        // Clear error labels on startup
        clearErrorLabels();
        
        // Add real-time validation listeners
        addValidationListeners();
    }

    private void addValidationListeners() {
        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError(emailError);
            if (!newVal.isEmpty()) validateEmail(newVal);
        });
        
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError(usernameError);
            if (!newVal.isEmpty()) validateUsername(newVal);
        });
        
        fullNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError(fullNameError);
            if (!newVal.isEmpty()) validateFullName(newVal);
        });
        
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError(passwordError);
            if (!newVal.isEmpty()) validatePassword(newVal);
        });
        
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError(confirmPasswordError);
            if (!newVal.isEmpty()) validatePasswordMatch();
        });
        
        countryField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError(countryError);
            if (!newVal.isEmpty()) validateCountry(newVal);
        });
        
        birthDateField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearError(birthDateError);
            if (!newVal.isEmpty()) validateBirthDate(newVal);
        });
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (validateForm()) {
            try {
                User user = createUserFromFields();
                userService.createUser(user);
                
                successLabel.setText("✅ User created successfully!");
                successLabel.setStyle("-fx-text-fill: #2dff8b; -fx-font-weight: 600;");
                
                // Navigate back to users list after successful creation
                navigateBackToUsers();
                
            } catch (Exception e) {
                successLabel.setText("❌ Error creating user: " + e.getMessage());
                successLabel.setStyle("-fx-text-fill: #ff2d2d; -fx-font-weight: 600;");
            }
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        navigateBackToUsers();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
        clearErrorLabels();
        successLabel.setText("");
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        isValid &= validateEmail(emailField.getText());
        isValid &= validateUsername(usernameField.getText());
        isValid &= validateFullName(fullNameField.getText());
        isValid &= validatePassword(passwordField.getText());
        isValid &= validatePasswordMatch();
        isValid &= validateCountry(countryField.getText());
        isValid &= validateBirthDate(birthDateField.getText());
        isValid &= validateRole();
        
        return isValid;
    }

    private boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            setError(emailError, "Email is required.");
            return false;
        }
        
        if (email.length() > 180) {
            setError(emailError, "Email must not exceed 180 characters.");
            return false;
        }
        
        if (!Pattern.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$", email)) {
            setError(emailError, "Invalid email format.");
            return false;
        }
        
        if (userService.emailExists(email)) {
            setError(emailError, "Email already exists.");
            return false;
        }
        
        return true;
    }

    private boolean validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            setError(usernameError, "Username is required.");
            return false;
        }
        
        if (username.length() < 3) {
            setError(usernameError, "Username must be at least 3 characters.");
            return false;
        }
        
        if (username.length() > 50) {
            setError(usernameError, "Username must not exceed 50 characters.");
            return false;
        }
        
        if (!Pattern.matches("^[a-zA-Z0-9_]+$", username)) {
            setError(usernameError, "Username can only contain letters, numbers, and underscores.");
            return false;
        }
        
        if (userService.usernameExists(username)) {
            setError(usernameError, "Username already exists.");
            return false;
        }
        
        return true;
    }

    private boolean validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            setError(fullNameError, "Full name is required.");
            return false;
        }
        
        if (fullName.length() < 2) {
            setError(fullNameError, "Full name must be at least 2 characters.");
            return false;
        }
        
        if (fullName.length() > 100) {
            setError(fullNameError, "Full name must not exceed 100 characters.");
            return false;
        }
        
        if (!Pattern.matches("^[a-zA-ZÀ-ÿ\\s'\\-]+$", fullName)) {
            setError(fullNameError, "Full name can only contain letters, spaces, hyphens, and apostrophes.");
            return false;
        }
        
        return true;
    }

    private boolean validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            setError(passwordError, "Password is required.");
            return false;
        }
        
        if (password.length() < 8) {
            setError(passwordError, "Password must be at least 8 characters.");
            return false;
        }
        
        if (!Pattern.matches(".*[A-Z].*", password)) {
            setError(passwordError, "Password must contain at least one uppercase letter.");
            return false;
        }
        
        if (!Pattern.matches(".*[a-z].*", password)) {
            setError(passwordError, "Password must contain at least one lowercase letter.");
            return false;
        }
        
        if (!Pattern.matches(".*[0-9].*", password)) {
            setError(passwordError, "Password must contain at least one digit.");
            return false;
        }
        
        if (!Pattern.matches(".*[^A-Za-z0-9].*", password)) {
            setError(passwordError, "Password must contain at least one special character.");
            return false;
        }
        
        return true;
    }

    private boolean validatePasswordMatch() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            setError(confirmPasswordError, "Please confirm password.");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            setError(confirmPasswordError, "Passwords do not match.");
            return false;
        }
        
        return true;
    }

    private boolean validateCountry(String country) {
        if (country == null || country.trim().isEmpty()) {
            setError(countryError, "Country is required.");
            return false;
        }
        
        if (country.length() > 50) {
            setError(countryError, "Country must not exceed 50 characters.");
            return false;
        }
        
        if (!Pattern.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$", country)) {
            setError(countryError, "Country can only contain letters, spaces, and hyphens.");
            return false;
        }
        
        return true;
    }

    private boolean validateBirthDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            setError(birthDateError, "Birth date is required.");
            return false;
        }
        
        try {
            LocalDate birthDate = LocalDate.parse(dateStr, dateFormatter);
            LocalDate today = LocalDate.now();
            int age = java.time.Period.between(birthDate, today).getYears();
            
            if (age < 13) {
                setError(birthDateError, "User must be at least 13 years old.");
                return false;
            }
            
            if (age > 100) {
                setError(birthDateError, "Invalid birth date (age over 100).");
                return false;
            }
            
        } catch (DateTimeParseException e) {
            setError(birthDateError, "Invalid date format. Use YYYY-MM-DD.");
            return false;
        }
        
        return true;
    }

    private boolean validateRole() {
        String role = roleComboBox.getValue();
        if (role == null || role.trim().isEmpty()) {
            // This shouldn't happen with ComboBox, but just in case
            return false;
        }
        return true;
    }

    private User createUserFromFields() {
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = passwordField.getText();
        String country = countryField.getText().trim();
        String birthDateStr = birthDateField.getText().trim();
        LocalDate birthDate = LocalDate.parse(birthDateStr, dateFormatter);
        String role = roleComboBox.getValue();
        String roles = buildRolesJson(role);
        
        return new User(email, roles, password, username, fullName, country, birthDate, role);
    }

    private String buildRolesJson(String role) {
        return switch (role) {
            case "ADMIN" -> "[\"ROLE_ADMIN\"]";
            case "COACH" -> "[\"ROLE_USER\",\"ROLE_COACH\"]";
            case "ORGANIZER" -> "[\"ROLE_USER\",\"ROLE_ORGANIZER\"]";
            default -> "[\"ROLE_USER\",\"ROLE_PLAYER\"]";
        };
    }

    private void clearForm() {
        emailField.clear();
        usernameField.clear();
        fullNameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        countryField.clear();
        birthDateField.clear();
        roleComboBox.getSelectionModel().selectFirst();
    }

    private void clearErrorLabels() {
        clearError(emailError);
        clearError(usernameError);
        clearError(fullNameError);
        clearError(passwordError);
        clearError(confirmPasswordError);
        clearError(countryError);
        clearError(birthDateError);
    }

    private void clearError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    private void setError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: #ff2d2d; -fx-font-size: 12px; -fx-font-weight: 600;");
        }
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    private void navigateBackToUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/users.fxml"));
            Parent root = loader.load();
            
            // Pass current user back to users controller
            Controllers.user.UsersController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) saveButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Users Management - Phantom Admin");
            
        } catch (IOException e) {
            System.err.println("Cannot navigate back to users: " + e.getMessage());
            // If navigation fails, just close the window
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }
}
