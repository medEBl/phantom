package Controllers.user;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.user.PasswordResetService;

import java.util.regex.Pattern;

public class ForgotPasswordController {

    // ── FXML injections ───────────────────────────────────────────────────────
    @FXML private VBox      stepEmailPane;
    @FXML private VBox      stepResetPane;
    @FXML private VBox      stepCodesPane;
    @FXML private Label     stepLabel;

    // Step 1
    @FXML private TextField emailField;
    @FXML private Label     emailError;
    @FXML private Label     successMsg;
    @FXML private Button    sendBtn;

    // Step 2.5 - Codes from email
    @FXML private TextField selectorField;
    @FXML private TextField tokenField;
    @FXML private Label     codesError;

    // Step 3
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label         resetError;

    // ── State ─────────────────────────────────────────────────────────────────
    private final PasswordResetService resetService = new PasswordResetService();

    // Stored after a manual token entry (for demo / console flow)
    // In a real app these come from the deep-link URL
    private String pendingSelector;
    private String pendingVerifier;

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");

    // ═════════════════════════════════════════════════════════════════════════
    //  STEP 1 — Send reset link
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void onSendLink() {
        clearErrors();
        String email = emailField.getText().trim();

        // Validate email
        if (email.isBlank()) {
            showError(emailError, "L'email est obligatoire.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(emailError, "L'email \"" + email + "\" n'est pas valide.");
            return;
        }

        // Disable button while sending
        sendBtn.setDisable(true);
        sendBtn.setText("Sending…");

        // Run in background thread so UI doesn't freeze
        new Thread(() -> {
            boolean sent = resetService.requestReset(email);
            Platform.runLater(() -> {
                sendBtn.setDisable(false);
                sendBtn.setText("Send Reset Link");
                if (sent) {
                    showSuccess("✅ If this email exists, a reset link has been sent. Check your inbox.");
                    emailField.setDisable(true);
                    sendBtn.setDisable(true);

                    // ── In a real app the link opens the app via deep link.
                    //    For demo: show step 2 after user pastes selector+token.
                    goToStep2();

                }
            });
        }).start();
    }

    /**
     * In production this is triggered by the deep-link handler.
     * For desktop demo we show a small dialog to paste the token from the email link.
     */
    private void showTokenInputDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Enter Reset Token");
        dialog.setHeaderText("Paste the token from your email link.\nYou can use:\n• The full link from email\n• Format: selector|verifier");

        VBox content = new VBox(10);
        
        // Method 1: Full URL input
        TextField urlField = new TextField();
        urlField.setPromptText("Paste full reset link from email");
        urlField.setPrefWidth(400);
        
        // Method 2: Manual token input
        TextField tokenField = new TextField();
        tokenField.setPromptText("Or enter: selector|verifier");
        tokenField.setPrefWidth(400);
        
        Label separator = new Label("──────── OR ─────────");
        separator.setStyle("-fx-text-fill: #666666; -fx-font-size: 10px;");
        
        content.getChildren().addAll(urlField, separator, tokenField);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                String urlText = urlField.getText().trim();
                String tokenText = tokenField.getText().trim();
                
                // Try URL method first
                if (!urlText.isEmpty()) {
                    parseTokenFromUrl(urlText);
                }
                // Try manual method if URL is empty
                else if (!tokenText.isEmpty()) {
                    String[] parts = tokenText.split("\\|");
                    if (parts.length == 2) {
                        pendingSelector = parts[0];
                        pendingVerifier = parts[1];
                        goToStep2();
                    } else {
                        showError(emailError, "Invalid token format. Use: selector|verifier");
                    }
                } else {
                    showError(emailError, "Please enter either the full URL or the token");
                }
            }
        });
    }
    
    private void parseTokenFromUrl(String url) {
        try {
            System.out.println("🔗 DEBUG: Parsing URL: " + url);
            
            // Parse phantom://reset-password?selector=abc&token=xyz format
            if (url.contains("selector=") && url.contains("token=")) {
                String[] params = url.split("\\?");
                if (params.length > 1) {
                    String paramString = params[1];
                    String[] keyValuePairs = paramString.split("&");
                    
                    for (String pair : keyValuePairs) {
                        String[] keyValue = pair.split("=");
                        if (keyValue.length == 2) {
                            if (keyValue[0].equals("selector")) {
                                pendingSelector = keyValue[1];
                                System.out.println("✅ SUCCESS: Found selector: " + keyValue[1]);
                            } else if (keyValue[0].equals("token")) {
                                pendingVerifier = keyValue[1];
                                System.out.println("✅ SUCCESS: Found token: " + keyValue[1]);
                            }
                        }
                    }
                    
                    if (pendingSelector != null && pendingVerifier != null) {
                        System.out.println("🎯 SUCCESS: Both tokens parsed from URL");
                        goToStep2();
                        return;
                    }
                }
            }
            
            System.err.println("❌ Failed to parse URL");
            showError(emailError, "Invalid reset link format");
        } catch (Exception e) {
            System.err.println("❌ Error parsing URL: " + e.getMessage());
            showError(emailError, "Invalid reset link format");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STEP 2.5 — Verify codes from email
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void onVerifyCodes() {
        clearErrors();
        
        String selector = selectorField.getText().trim();
        String token = tokenField.getText().trim();
        
        System.out.println("🔍 DEBUG: Verifying codes...");
        System.out.println("🔍 DEBUG: Selector = " + (selector.isEmpty() ? "EMPTY" : "***FILLED***"));
        System.out.println("🔍 DEBUG: Token = " + (token.isEmpty() ? "EMPTY" : "***FILLED***"));
        
        if (selector.isEmpty()) {
            showError(codesError, "Selector code is required.");
            return;
        }
        
        if (token.isEmpty()) {
            showError(codesError, "Token code is required.");
            return;
        }
        
        // Store the codes for password reset
        pendingSelector = selector;
        pendingVerifier = token;
        
        System.out.println("✅ SUCCESS: Codes stored, proceeding to password reset");
        goToStep3();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STEP 3 — Set new password
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void onResetPassword() {
        clearErrors();

        String newPw      = newPasswordField.getText();
        String confirmPw  = confirmPasswordField.getText();

        // 🔍 DEBUG: Print token values
        System.out.println("🔍 DEBUG: onResetPassword called");
        System.out.println("🔍 DEBUG: pendingSelector = " + (pendingSelector != null ? pendingSelector : "NULL"));
        System.out.println("🔍 DEBUG: pendingVerifier = " + (pendingVerifier != null ? pendingVerifier : "NULL"));
        System.out.println("🔍 DEBUG: newPassword = " + (newPw.isEmpty() ? "EMPTY" : "***FILLED***"));

        // ── Validate new password (same rules as User.php) ────────────────────
        if (newPw.isBlank()) {
            showError(resetError, "Le mot de passe est obligatoire.");
            return;
        }
        if (newPw.length() < 8) {
            showError(resetError, "Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }
        if (!newPw.matches(".*[A-Z].*")) {
            showError(resetError, "Le mot de passe doit contenir au moins une lettre majuscule.");
            return;
        }
        if (!newPw.matches(".*[a-z].*")) {
            showError(resetError, "Le mot de passe doit contenir au moins une lettre minuscule.");
            return;
        }
        if (!newPw.matches(".*[0-9].*")) {
            showError(resetError, "Le mot de passe doit contenir au moins un chiffre.");
            return;
        }
        if (!newPw.matches(".*[^A-Za-z0-9].*")) {
            showError(resetError, "Le mot de passe doit contenir au moins un caractère spécial.");
            return;
        }
        if (!newPw.equals(confirmPw)) {
            showError(resetError, "Les mots de passe ne correspondent pas.");
            return;
        }

        // 🔍 DEBUG: Check if tokens are set before proceeding
        if (pendingSelector == null || pendingVerifier == null) {
            System.err.println("❌ ERROR: No token data available!");
            System.err.println("   This usually means you didn't complete step 1 properly.");
            System.err.println("   Please go back and request a new reset link.");
            showError(resetError, "❌ No reset token found. Please request a new reset link.");
            return;
        }

        // ── Reset in DB ───────────────────────────────────────────────────────
        System.out.println("🔄 DEBUG: Calling resetService.resetPassword...");
        boolean success = resetService.resetPassword(pendingSelector, pendingVerifier, newPw);
        System.out.println("🔍 DEBUG: resetService returned: " + success);
        
        if (success) {
            System.out.println("✅ SUCCESS: Password reset completed");
            showAlert(Alert.AlertType.INFORMATION,
                "Password Reset",
                "✅ Your password has been reset successfully!\nYou can now log in.");
            navigateToLogin();
        } else {
            System.err.println("❌ FAILED: Token validation failed");
            showError(resetError, "❌ Token invalid or expired. Please request a new reset link.");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  Navigation
    // ═════════════════════════════════════════════════════════════════════════
    @FXML
    private void onBackToLogin() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(
                getClass().getResource("/user/fxml/login.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private void goToStep2() {
        stepEmailPane.setVisible(false);
        stepEmailPane.setManaged(false);
        stepCodesPane.setVisible(true);
        stepCodesPane.setManaged(true);
        stepResetPane.setVisible(false);
        stepResetPane.setManaged(false);
        stepLabel.setText("Step 2 of 3 — Enter codes from email");
    }

    private void goToStep3() {
        stepEmailPane.setVisible(false);
        stepEmailPane.setManaged(false);
        stepCodesPane.setVisible(false);
        stepCodesPane.setManaged(false);
        stepResetPane.setVisible(true);
        stepResetPane.setManaged(true);
        stepLabel.setText("Step 3 of 3 — Set your new password");
    }

    private void showError(Label label, String msg) {
        label.setText(msg);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void showSuccess(String msg) {
        successMsg.setText(msg);
        successMsg.setVisible(true);
        successMsg.setManaged(true);
    }

    private void clearErrors() {
        emailError.setText("");
        codesError.setText("");
        resetError.setText("");
        successMsg.setVisible(false);
        successMsg.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
