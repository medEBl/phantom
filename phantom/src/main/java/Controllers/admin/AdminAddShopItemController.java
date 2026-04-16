package Controllers.admin;

import entities.shop.ShopItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import services.shop.ShopService;

import java.io.IOException;
import java.sql.Timestamp;

public class AdminAddShopItemController {

    @FXML private TextField idField;
    @FXML private TextField buyerIdField;
    @FXML private TextField itemNameField;
    @FXML private TextField itemDescriptionField;
    @FXML private TextField itemCategoryField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextField statusField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button backButton;

    private final ShopService shopService = new ShopService();

    @FXML
    private void handleSaveShopItem() {
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
            int buyerId = Integer.parseInt(buyerIdField.getText().trim());
            String itemName = itemNameField.getText().trim();
            String itemDescription = itemDescriptionField.getText().trim();
            String itemCategory = itemCategoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int quantity = Integer.parseInt(quantityField.getText().trim());
            String status = statusField.getText().trim().isEmpty() ? "pending" : statusField.getText().trim();

            // Validation des valeurs
            if (!validateShopItemValues(id, buyerId, itemName, itemCategory, price, quantity, status)) {
                return;
            }

            // Create ShopItem object
            ShopItem newShopItem = new ShopItem(
                id, buyerId, itemName, itemDescription, itemCategory,
                price, quantity, price * quantity, new Timestamp(System.currentTimeMillis()), status
            );
            
            shopService.createShopItem(newShopItem);
            showAlert("Success", "Shop item added successfully", Alert.AlertType.INFORMATION);
            handleBack();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please check that ID, buyer ID, price and quantity are valid numbers", Alert.AlertType.ERROR);
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/shop-items.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Shop Items Management - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot return to shop items list: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateRequiredFields() {
        StringBuilder missingFields = new StringBuilder();
        
        if (idField.getText().trim().isEmpty()) {
            missingFields.append("ID, ");
        }
        if (buyerIdField.getText().trim().isEmpty()) {
            missingFields.append("Buyer ID, ");
        }
        if (itemNameField.getText().trim().isEmpty()) {
            missingFields.append("Item Name, ");
        }
        if (priceField.getText().trim().isEmpty()) {
            missingFields.append("Price, ");
        }
        if (quantityField.getText().trim().isEmpty()) {
            missingFields.append("Quantity, ");
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

            // Valider Buyer ID
            String buyerIdText = buyerIdField.getText().trim();
            int buyerId = Integer.parseInt(buyerIdText);
            if (buyerId <= 0) {
                showAlert("Invalid Buyer ID", "Buyer ID must be a positive number", Alert.AlertType.ERROR);
                return false;
            }

            // Valider Prix
            String priceText = priceField.getText().trim();
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showAlert("Invalid Price", "Price must be a positive number", Alert.AlertType.ERROR);
                return false;
            }
            if (price > 100000) {
                showAlert("Invalid Price", "Price cannot exceed 100,000 DT", Alert.AlertType.ERROR);
                return false;
            }

            // Valider Quantité
            String quantityText = quantityField.getText().trim();
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Invalid Quantity", "Quantity must be a positive number", Alert.AlertType.ERROR);
                return false;
            }
            if (quantity > 1000) {
                showAlert("Invalid Quantity", "Quantity cannot exceed 1,000", Alert.AlertType.ERROR);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            showAlert("Format Error", "Numeric fields must contain valid numbers", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validateShopItemValues(int id, int buyerId, String itemName, String itemCategory, double price, int quantity, String status) {
        // Valider nom de l'article
        if (itemName.length() < 2) {
            showAlert("Invalid Item Name", "Item name must contain at least 2 characters", Alert.AlertType.ERROR);
            return false;
        }
        if (itemName.length() > 100) {
            showAlert("Invalid Item Name", "Item name cannot exceed 100 characters", Alert.AlertType.ERROR);
            return false;
        }

        // Valider catégorie
        if (!itemCategory.isEmpty() && itemCategory.length() > 50) {
            showAlert("Invalid Category", "Category cannot exceed 50 characters", Alert.AlertType.ERROR);
            return false;
        }

        // Valider statut
        if (!status.isEmpty() && !isValidShopItemStatus(status)) {
            showAlert("Invalid Status", "Valid statuses: pending, confirmed, cancelled, refunded", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean isValidShopItemStatus(String status) {
        String[] validStatuses = {"pending", "confirmed", "cancelled", "refunded"};
        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private void clearFields() {
        idField.clear();
        buyerIdField.clear();
        itemNameField.clear();
        itemDescriptionField.clear();
        itemCategoryField.clear();
        priceField.clear();
        quantityField.clear();
        statusField.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
