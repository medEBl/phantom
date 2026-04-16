package Controllers.shop;

import entities.shop.Payment;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.shop.PaymentService;

import java.io.IOException;

public class EditPaymentController {
    @FXML private TextField idField;
    @FXML private TextField amountField;
    @FXML private TextField methodField;
    @FXML private TextField statusField;
    @FXML private TextField transactionRefField;
    @FXML private TextField shopItemIdField;
    @FXML private VBox messageContainer;

    private int currentId;
    private final PaymentService paymentService = new PaymentService();

    public void setPaymentData(Payment payment) {
        currentId = payment.getId();
        idField.setText(String.valueOf(payment.getId()));
        amountField.setText(String.valueOf(payment.getAmount()));
        methodField.setText(payment.getPaymentMethod());
        statusField.setText(payment.getPaymentStatus());
        transactionRefField.setText(payment.getTransactionRef());
        shopItemIdField.setText(String.valueOf(payment.getShopItemId()));
    }

    @FXML
    private void handleUpdatePayment() {
        try {
            String status = statusField.getText().trim();
            String method = methodField.getText().trim();

            // Validation des champs
            if (!validateUpdateFields(status, method)) {
                return;
            }

            // Update both status and method
            paymentService.updatePaymentStatus(currentId, status);
            if (!method.isEmpty()) {
                paymentService.updatePaymentMethod(currentId, method);
            }

            showPageMessage("Succès", "Paiement modifié avec succès", "success");
            closeWindow();

        } catch (Exception e) {
            showPageMessage("Erreur", e.getMessage(), "error");
        }
    }

    private boolean validateUpdateFields(String status, String method) {
        // Au moins un champ doit être modifié
        if (status.isEmpty() && method.isEmpty()) {
            showPageMessage("Aucune modification", "Veuillez modifier au moins le statut ou la méthode", "warning");
            return false;
        }

        // Valider le statut si fourni
        if (!status.isEmpty() && !isValidPaymentStatus(status)) {
            showPageMessage("Statut invalide", "Statuts valides: pending, success, failed, refunded, cancelled", "error");
            return false;
        }

        // Valider la méthode si fournie
        if (!method.isEmpty() && !isValidPaymentMethod(method)) {
            showPageMessage("Méthode invalide", "Méthodes valides: card, cash, paypal, bank_transfer, crypto, check", "error");
            return false;
        }

        return true;
    }

    private boolean isValidPaymentMethod(String method) {
        String[] validMethods = {"card", "cash", "paypal", "bank_transfer", "crypto", "check"};
        for (String validMethod : validMethods) {
            if (validMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidPaymentStatus(String status) {
        String[] validStatuses = {"pending", "success", "failed", "refunded", "cancelled"};
        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/payments.fxml"));
            Parent root = loader.load();
            
            Stage currentStage = (Stage) idField.getScene().getWindow();
            currentStage.setTitle("PHANTOM FORCE - PAYMENTS");
            currentStage.setScene(new Scene(root, 1920, 1080));
            currentStage.setMaximized(true);
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste des paiements", Alert.AlertType.ERROR);
        }
    }

    private void showPageMessage(String title, String message, String type) {
        messageContainer.getChildren().clear();
        
        Label messageLabel = new Label(title + ": " + message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);
        
        // Apply styling based on message type
        messageLabel.getStyleClass().addAll("message", "message-" + type);
        
        messageContainer.getChildren().add(messageLabel);
        
        // Auto-hide success messages after 3 seconds
        if ("success".equals(type)) {
            javafx.util.Duration delay = javafx.util.Duration.seconds(3);
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(delay);
            pause.setOnFinished(event -> messageContainer.getChildren().clear());
            pause.play();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}