package Controllers.user;

import entities.user.User;
import services.user.UserService;
import services.auth.FaceRecognitionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public class FaceAuthSetupController {
    
    private final UserService userService = new UserService();
    private FaceRecognitionService faceRecognitionService;
    private User currentUser;

    @FXML
    private Label titleLabel;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private Button captureButton;
    
    @FXML
    private Button enableButton;
    
    @FXML
    private Button disableButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private ImageView faceImageView;
    
    @FXML
    private ProgressIndicator progressIndicator;

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeFaceAuth();
        updateUI();
    }

    @FXML
    public void initialize() {
        try {
            faceRecognitionService = new FaceRecognitionService();
            if (!faceRecognitionService.isFaceRecognitionAvailable()) {
                showError("Face recognition is not available on this system.");
                disableAllButtons();
            }
        } catch (Exception e) {
            showError("Error initializing face recognition: " + e.getMessage());
            disableAllButtons();
        }
    }

    private void initializeFaceAuth() {
        try {
            faceRecognitionService = new FaceRecognitionService();
            if (!faceRecognitionService.isFaceRecognitionAvailable()) {
                showError("Face recognition is not available on this system.");
                disableAllButtons();
            }
        } catch (Exception e) {
            showError("Error initializing face recognition: " + e.getMessage());
            disableAllButtons();
        }
    }

    private void updateUI() {
        if (currentUser != null) {
            boolean hasFaceData = currentUser.getFaceEncoding() != null && !currentUser.getFaceEncoding().isEmpty();
            boolean faceAuthEnabled = currentUser.isFaceAuthenticationEnabled();
            
            if (hasFaceData) {
                statusLabel.setText("Face data registered. Face authentication " + 
                                 (faceAuthEnabled ? "enabled." : "disabled."));
            } else {
                statusLabel.setText("No face data registered.");
            }
            
            // Update button text and visibility based on face data status
            if (!hasFaceData) {
                captureButton.setText("Register Face");
                enableButton.setVisible(false);
                disableButton.setVisible(false);
            } else {
                captureButton.setText("Update Face");
                enableButton.setVisible(!faceAuthEnabled);
                disableButton.setVisible(faceAuthEnabled);
            }
            
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
            }
        }
    }

    @FXML
    private void handleCaptureFace() {
        if (faceRecognitionService == null || !faceRecognitionService.isFaceRecognitionAvailable()) {
            showError("Face recognition is not available.");
            return;
        }

        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        statusLabel.setText("Please position your face in front of the camera...");
        captureButton.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                boolean success;
                if (currentUser.isFaceAuthenticationEnabled()) {
                    success = faceRecognitionService.updateFaceForUser(currentUser);
                } else {
                    success = faceRecognitionService.registerFaceForUser(currentUser);
                }

                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    captureButton.setDisable(false);
                    
                    if (success) {
                        showSuccess("Face registered successfully!");
                        enableButton.setVisible(true);
                    } else {
                        showError("Failed to register face. Please try again.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    captureButton.setDisable(false);
                    showError("Error capturing face: " + e.getMessage());
                });
            }
        });
    }

    @FXML
    private void handleEnableFaceAuth() {
        if (currentUser.getFaceEncoding() == null || currentUser.getFaceEncoding().isEmpty()) {
            showError("Please register your face first.");
            return;
        }

        try {
            currentUser.setFaceAuthenticationEnabled(true);
            boolean success = userService.updateUserWithFaceData(currentUser);
            
            if (success) {
                showSuccess("Face authentication enabled successfully!");
                updateUI();
            } else {
                showError("Failed to enable face authentication.");
            }
        } catch (Exception e) {
            showError("Error enabling face authentication: " + e.getMessage());
        }
    }

    @FXML
    private void handleDisableFaceAuth() {
        try {
            boolean success = faceRecognitionService.disableFaceAuthForUser(currentUser);
            
            if (success) {
                showSuccess("Face authentication disabled successfully!");
                updateUI();
            } else {
                showError("Failed to disable face authentication.");
            }
        } catch (Exception e) {
            showError("Error disabling face authentication: " + e.getMessage());
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/profile.fxml"));
            Parent root = loader.load();
            
            Controllers.user.ProfileController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("My Profile - Phantom App");
            stage.show();
            
        } catch (IOException e) {
            showError("Cannot return to profile: " + e.getMessage());
        }
    }

    private void disableAllButtons() {
        captureButton.setDisable(true);
        enableButton.setDisable(true);
        disableButton.setDisable(true);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #2dff8b;");
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #ff2d2d;");
    }
}
