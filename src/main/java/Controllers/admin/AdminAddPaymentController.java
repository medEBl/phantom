package Controllers.admin;

import entities.shop.Payment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import services.shop.PaymentService;
import services.shop.ShopService;

import java.io.IOException;
import java.sql.Timestamp;

public class AdminAddPaymentController {

    @FXML private TextField idField;
    @FXML private TextField amountField;
    @FXML private TextField methodField;
    @FXML private TextField statusField;
    @FXML private TextField transactionRefField;
    @FXML private TextField shopItemIdField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;

    private final PaymentService paymentService = new PaymentService();

    @FXML
    private void handleSavePayment() {
        try {
            // Validation des champs obligatoires
            if (!validateRequiredFields()) {
                return;
            }

            // Validation et conversion des champs numériques
            if (!validateNumericFields()) {
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());
            double amount = Double.parseDouble(amountField.getText().trim());
            String method = methodField.getText().trim().isEmpty() ? "card" : methodField.getText().trim();
            String status = statusField.getText().trim().isEmpty() ? "pending" : statusField.getText().trim();
            String transactionRef = transactionRefField.getText().trim();
            int shopItemId = Integer.parseInt(shopItemIdField.getText().trim());

            // Validation des valeurs
            if (!validatePaymentValues(id, amount, method, status, transactionRef, shopItemId)) {
                return;
            }

            // Create Payment object
            Payment newPayment = new Payment(
                id, amount, method, status, transactionRef,
                new Timestamp(System.currentTimeMillis()), shopItemId
            );
            
            paymentService.createPayment(newPayment);

            showAlert("Success", "Payment added successfully", Alert.AlertType.INFORMATION);
            handleBack();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please check that ID, amount and shop item ID are valid numbers", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        clearFields();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/payments.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            
            // Préserver l'état actuel de la fenêtre
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Payment Management - Phantom Admin");
            
            // Restaurer l'état précédent
            if (wasFullScreen) {
                stage.setFullScreen(true);
            } else if (wasMaximized) {
                stage.setMaximized(true);
            }
            
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot return to payment list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateRequiredFields() {
        StringBuilder missingFields = new StringBuilder();
        
        if (idField.getText().trim().isEmpty()) {
            missingFields.append("ID, ");
        }
        if (amountField.getText().trim().isEmpty()) {
            missingFields.append("Amount, ");
        }
        if (transactionRefField.getText().trim().isEmpty()) {
            missingFields.append("Transaction Ref, ");
        }
        if (shopItemIdField.getText().trim().isEmpty()) {
            missingFields.append("Shop Item ID, ");
        }

        if (missingFields.length() > 0) {
            String message = "Missing required fields: " + 
                           missingFields.substring(0, missingFields.length() - 2);
            showAlert("Missing Fields", message, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateNumericFields() {
        try {
            // Valider ID
            String idText = idField.getText().trim();
            int id = Integer.parseInt(idText);
            if (id <= 0) {
                showAlert("Invalid ID", "ID must be a positive number", Alert.AlertType.ERROR);
                return false;
            }

            // Valider Montant
            String amountText = amountField.getText().trim();
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Invalid Amount", "Amount must be a positive number", Alert.AlertType.ERROR);
                return false;
            }
            if (amount > 100000) {
                showAlert("Invalid Amount", "Amount cannot exceed 100,000 DT", Alert.AlertType.ERROR);
                return false;
            }

            // Valider ShopItem ID
            String shopItemIdText = shopItemIdField.getText().trim();
            int shopItemId = Integer.parseInt(shopItemIdText);
            if (shopItemId <= 0) {
                showAlert("Invalid Shop Item ID", "Shop Item ID must be a positive number", Alert.AlertType.ERROR);
                return false;
            }
            
            // Vérifier si le ShopItem existe dans la base de données
            ShopService shopService = new ShopService();
            try {
                if (!shopService.getShopItemById(shopItemId).isPresent()) {
                    showAlert("Shop Item Not Found", "Shop item with ID " + shopItemId + " does not exist in the database", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (Exception e) {
                showAlert("Verification Error", "Unable to verify shop item existence: " + e.getMessage(), Alert.AlertType.ERROR);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            showAlert("Format Error", "Numeric fields must contain valid numbers", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validatePaymentValues(int id, double amount, String method, String status, String transactionRef, int shopItemId) {
        // Valider méthode de paiement
        if (!method.isEmpty() && !isValidPaymentMethod(method)) {
            showAlert("Invalid Method", "Valid methods: card, cash, paypal, bank_transfer, crypto, check", Alert.AlertType.ERROR);
            return false;
        }

        // Valider statut
        if (!status.isEmpty() && !isValidPaymentStatus(status)) {
            showAlert("Invalid Status", "Valid statuses: pending, success, failed, refunded, cancelled", Alert.AlertType.ERROR);
            return false;
        }

        // Valider référence transaction (longueur minimale)
        if (transactionRef.length() < 3) {
            showAlert("Invalid Reference", "Transaction reference must contain at least 3 characters", Alert.AlertType.ERROR);
            return false;
        }

        // Valider caractères spéciaux dans la référence
        if (!transactionRef.matches("^[a-zA-Z0-9_-]+$")) {
            showAlert("Invalid Reference", "Reference can only contain letters, numbers, '_' and '-'", Alert.AlertType.ERROR);
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

    private void clearFields() {
        idField.clear();
        amountField.clear();
        methodField.clear();
        statusField.clear();
        transactionRefField.clear();
        shopItemIdField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
