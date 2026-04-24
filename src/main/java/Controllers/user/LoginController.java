package Controllers.user;

import entities.user.User;
import services.user.UserService;
import services.auth.GoogleOAuth2Service;
import services.auth.OAuth2CallbackServer;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

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
    private Hyperlink forgotPasswordLink;

    @FXML
    private Label errorLabel;

    @FXML
    private Button googleLoginButton;

    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void handleGoogleLogin() {
        try {
            GoogleOAuth2Service oauth2Service = new GoogleOAuth2Service();
            OAuth2CallbackServer callbackServer = new OAuth2CallbackServer(
                    oauth2Service, oauth2Service.getStateToken());
            
            // Start callback server
            callbackServer.start();
            
            // Get authorization URL and open browser
            String authUrl = oauth2Service.getAuthorizationUrl();
            Desktop.getDesktop().browse(URI.create(authUrl));
            
            // Handle the callback asynchronously
            callbackServer.getTokenFuture().thenAccept(idToken -> {
                try {
                    Platform.runLater(() -> processGoogleLogin(idToken));
                } catch (Exception e) {
                    Platform.runLater(() -> showError("Google login processing failed: " + e.getMessage()));
                }
            }).exceptionally(throwable -> {
                Platform.runLater(() -> showError("Google authentication failed: " + throwable.getMessage()));
                return null;
            });
            
        } catch (Exception e) {
            if (e.getMessage().contains("Port 8888 is already in use")) {
                showError("Port 8888 is busy. Please try again.");
            } else {
                showError("Failed to start Google authentication: " + e.getMessage());
            }
        }
    }

    private void processGoogleLogin(GoogleIdToken idToken) {
        try {
            GoogleIdToken.Payload payload = idToken.getPayload();
            
            String googleId = payload.getSubject();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");
            
            // Login or create user with Google data
            Optional<User> userOpt = userService.loginWithGoogle(
                    googleId, email, fullName, null, null);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                showSuccess("Login successful! Welcome, " + user.getFullName() + "!");
                clearFields();
                navigateToHome(user);
            } else {
                showError("Failed to authenticate with Google.");
            }
            
        } catch (Exception e) {
            showError("Google login processing failed: " + e.getMessage());
        }
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

    @FXML
    private void handleForgotPassword() {
        try {
            // Load forgot password screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/ForgotPassword.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) forgotPasswordLink.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Forgot Password");
            stage.show();
            
        } catch (IOException e) {
            showError("Cannot open forgot password screen: " + e.getMessage());
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
