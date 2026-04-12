package Controllers.user;

import entities.user.User;
import services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class LoginController {

    private final UserService userService = new UserService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        try {
            Optional<User> userOpt = userService.login(email, password);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.isActive()) {
                    showError("Your account is inactive. Please contact support.");
                    return;
                }
                
                showSuccess("Login successful! Welcome, " + user.getFullName() + "!");
                clearFields();
                
                // Navigate to home screen
                navigateToHome(user);
                
            } else {
                showError("Invalid email or password.");
            }
        } catch (Exception e) {
            showError("Login failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        try {
            // Load registration screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/register.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) registerLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();
            
        } catch (IOException e) {
            showError("Cannot open registration screen: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$";
        boolean isValid = email.matches(emailRegex);
        
        // Debug output to check what's happening
        System.out.println("Email: '" + email + "'");
        System.out.println("Regex: " + emailRegex);
        System.out.println("Valid: " + isValid);
        
        return isValid;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
    }

    private void clearFields() {
        emailField.clear();
        passwordField.clear();
    }

    private void navigateToHome(User user) {
        try {
            Parent root;
            String title;
            
            // Check user role and redirect accordingly
            if ("admin".equalsIgnoreCase(user.getRole())) {
                // Admin goes to dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
                root = loader.load();
                title = "Admin Dashboard - Phantom App";
            } else {
                // Player, Organizer, Coach go to home
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
                root = loader.load();
                
                // Pass current user to home controller
                Controllers.home.HomeController controller = loader.getController();
                controller.setCurrentUser(user);
                
                title = "Home - Phantom App";
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle(title);
            
        } catch (Exception e) {
            showError("Cannot open home screen: " + e.getMessage());
        }
    }
}
