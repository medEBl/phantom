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
import java.sql.Timestamp;

public class AddPaymentController {

    @FXML private TextField idField;
    @FXML private TextField amountField;
    @FXML private TextField methodField;
    @FXML private TextField statusField;
    @FXML private TextField transactionRefField;
    @FXML private TextField shopItemIdField;
    @FXML private VBox messageContainer;

    private final PaymentService paymentService = new PaymentService();

    @FXML
    private void handleAddPayment() {
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

            showAlert("Succès", "Paiement ajouté avec succès", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "Vérifiez que l'ID, le montant et l'ID du jeu sont des nombres valides", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateRequiredFields() {
        StringBuilder missingFields = new StringBuilder();
        
        if (idField.getText().trim().isEmpty()) {
            missingFields.append("ID, ");
        }
        if (amountField.getText().trim().isEmpty()) {
            missingFields.append("Montant, ");
        }
        if (transactionRefField.getText().trim().isEmpty()) {
            missingFields.append("Référence transaction, ");
        }
        if (shopItemIdField.getText().trim().isEmpty()) {
            missingFields.append("ID Article, ");
        }

        if (missingFields.length() > 0) {
            String message = "Champs obligatoires manquants: " + 
                           missingFields.substring(0, missingFields.length() - 2);
            showAlert("Champs manquants", message, Alert.AlertType.WARNING);
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
                showAlert("ID invalide", "L'ID doit être un nombre positif", Alert.AlertType.ERROR);
                return false;
            }

            // Valider Montant
            String amountText = amountField.getText().trim();
            double amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Montant invalide", "Le montant doit être un nombre positif", Alert.AlertType.ERROR);
                return false;
            }
            if (amount > 100000) {
                showAlert("Montant invalide", "Le montant ne peut pas dépasser 100,000 DT", Alert.AlertType.ERROR);
                return false;
            }

            // Valider ShopItem ID
            String shopItemIdText = shopItemIdField.getText().trim();
            int shopItemId = Integer.parseInt(shopItemIdText);
            if (shopItemId <= 0) {
                showAlert("ID Article invalide", "L'ID de l'article doit être un nombre positif", Alert.AlertType.ERROR);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Les champs numériques doivent contenir des nombres valides", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validatePaymentValues(int id, double amount, String method, String status, String transactionRef, int shopItemId) {
        // Valider méthode de paiement
        if (!method.isEmpty() && !isValidPaymentMethod(method)) {
            showAlert("Méthode invalide", "Méthodes valides: card, cash, paypal, bank_transfer, crypto, check", Alert.AlertType.ERROR);
            return false;
        }

        // Valider statut
        if (!status.isEmpty() && !isValidPaymentStatus(status)) {
            showAlert("Statut invalide", "Statuts valides: pending, success, failed, refunded, cancelled", Alert.AlertType.ERROR);
            return false;
        }

        // Valider référence transaction (longueur minimale)
        if (transactionRef.length() < 3) {
            showAlert("Référence invalide", "La référence transaction doit contenir au moins 3 caractères", Alert.AlertType.ERROR);
            return false;
        }

        // Valider caractères spéciaux dans la référence
        if (!transactionRef.matches("^[a-zA-Z0-9_-]+$")) {
            showAlert("Référence invalide", "La référence ne peut contenir que des lettres, chiffres, '_' et '-'", Alert.AlertType.ERROR);
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}