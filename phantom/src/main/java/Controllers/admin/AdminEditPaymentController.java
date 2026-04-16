package Controllers.admin;

import entities.shop.Payment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.shop.PaymentService;

import java.io.IOException;

public class AdminEditPaymentController {

    @FXML private TextField idField;
    @FXML private TextField amountField;
    @FXML private TextField methodField;
    @FXML private TextField statusField;
    @FXML private TextField transactionRefField;
    @FXML private TextField shopItemIdField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;

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
            String amountText = amountField.getText().trim();
            String shopItemIdText = shopItemIdField.getText().trim();

            // Validation des champs
            if (!validateUpdateFields(status, method, amountText, shopItemIdText)) {
                return;
            }

            // Update payment fields
            if (!status.isEmpty()) {
                paymentService.updatePaymentStatus(currentId, status);
            }
            if (!method.isEmpty()) {
                paymentService.updatePaymentMethod(currentId, method);
            }
            if (!amountText.isEmpty()) {
                double amount = Double.parseDouble(amountText);
                paymentService.updatePaymentAmount(currentId, amount);
            }
            if (!shopItemIdText.isEmpty()) {
                int shopItemId = Integer.parseInt(shopItemIdText);
                paymentService.updatePaymentShopItemId(currentId, shopItemId);
            }

            showAlert("Success", "Payment updated successfully", Alert.AlertType.INFORMATION);
            handleBack();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please check that amount and shop item ID are valid numbers", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/payments.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Payment Management - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot return to payment list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/payments.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Payment Management - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot return to payment list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateUpdateFields(String status, String method, String amountText, String shopItemIdText) {
        // Au moins un champ doit être modifié
        if (status.isEmpty() && method.isEmpty() && amountText.isEmpty() && shopItemIdText.isEmpty()) {
            showAlert("No Changes", "Please modify at least one field", Alert.AlertType.WARNING);
            return false;
        }

        // Valider le statut si fourni
        if (!status.isEmpty() && !isValidPaymentStatus(status)) {
            showAlert("Invalid Status", "Valid statuses: pending, success, failed, refunded, cancelled", Alert.AlertType.ERROR);
            return false;
        }

        // Valider la méthode si fournie
        if (!method.isEmpty() && !isValidPaymentMethod(method)) {
            showAlert("Invalid Method", "Valid methods: card, cash, paypal, bank_transfer, crypto, check", Alert.AlertType.ERROR);
            return false;
        }

        // Valider le montant si fourni
        if (!amountText.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    showAlert("Invalid Amount", "Amount must be a positive number", Alert.AlertType.ERROR);
                    return false;
                }
                if (amount > 100000) {
                    showAlert("Invalid Amount", "Amount cannot exceed 100,000 DT", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Amount", "Amount must be a valid number", Alert.AlertType.ERROR);
                return false;
            }
        }

        // Valider ShopItem ID si fourni
        if (!shopItemIdText.isEmpty()) {
            try {
                int shopItemId = Integer.parseInt(shopItemIdText);
                if (shopItemId <= 0) {
                    showAlert("Invalid Shop Item ID", "Shop Item ID must be a positive number", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Shop Item ID", "Shop Item ID must be a valid number", Alert.AlertType.ERROR);
                return false;
            }
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
