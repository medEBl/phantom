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
import java.util.Optional;

public class AdminEditShopItemController {

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

    private int currentId;
    private final ShopService shopService = new ShopService();

    public void setShopItemData(ShopItem shopItem) {
        currentId = shopItem.getId();
        idField.setText(String.valueOf(shopItem.getId()));
        buyerIdField.setText(String.valueOf(shopItem.getBuyerId()));
        itemNameField.setText(shopItem.getItemName());
        itemDescriptionField.setText(shopItem.getItemDescription());
        itemCategoryField.setText(shopItem.getItemCategory());
        priceField.setText(String.valueOf(shopItem.getPrice()));
        quantityField.setText(String.valueOf(shopItem.getQuantity()));
        statusField.setText(shopItem.getStatus());
    }

    @FXML
    private void handleUpdateShopItem() {
        try {
            String buyerId = buyerIdField.getText().trim();
            String itemName = itemNameField.getText().trim();
            String itemDescription = itemDescriptionField.getText().trim();
            String itemCategory = itemCategoryField.getText().trim();
            String priceText = priceField.getText().trim();
            String quantityText = quantityField.getText().trim();
            String status = statusField.getText().trim();

            // Validation des champs
            if (!validateUpdateFields(buyerId, itemName, itemDescription, itemCategory, priceText, quantityText, status)) {
                return;
            }

            // Récupérer l'item existant
            Optional<ShopItem> existingItem = shopService.getShopItemById(currentId);
            if (!existingItem.isPresent()) {
                showAlert("Error", "Shop item not found", Alert.AlertType.ERROR);
                return;
            }

            ShopItem itemToUpdate = existingItem.get();

            // Update shop item fields
            if (!buyerId.isEmpty()) {
                itemToUpdate.setBuyerId(Integer.parseInt(buyerId));
            }
            if (!itemName.isEmpty()) {
                itemToUpdate.setItemName(itemName);
            }
            if (!itemDescription.isEmpty()) {
                itemToUpdate.setItemDescription(itemDescription);
            }
            if (!itemCategory.isEmpty()) {
                itemToUpdate.setItemCategory(itemCategory);
            }
            if (!priceText.isEmpty()) {
                double price = Double.parseDouble(priceText);
                itemToUpdate.setPrice(price);
                itemToUpdate.setTotalPrice(price * itemToUpdate.getQuantity());
            }
            if (!quantityText.isEmpty()) {
                int quantity = Integer.parseInt(quantityText);
                itemToUpdate.setQuantity(quantity);
                itemToUpdate.setTotalPrice(itemToUpdate.getPrice() * quantity);
            }
            if (!status.isEmpty()) {
                itemToUpdate.setStatus(status);
            }

            // Utiliser la méthode updateShopItem existante
            shopService.updateShopItem(itemToUpdate);

            showAlert("Success", "Shop item updated successfully", Alert.AlertType.INFORMATION);
            handleBack();

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please check that buyer ID, price and quantity are valid numbers", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/shop-items.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) cancelButton.getScene().getWindow();
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

    private boolean validateUpdateFields(String buyerId, String itemName, String itemDescription, String itemCategory, String priceText, String quantityText, String status) {
        // Au moins un champ doit être modifié
        if (buyerId.isEmpty() && itemName.isEmpty() && itemDescription.isEmpty() && 
            itemCategory.isEmpty() && priceText.isEmpty() && quantityText.isEmpty() && status.isEmpty()) {
            showAlert("No Changes", "Please modify at least one field", Alert.AlertType.WARNING);
            return false;
        }

        // Valider le statut si fourni
        if (!status.isEmpty() && !isValidShopItemStatus(status)) {
            showAlert("Invalid Status", "Valid statuses: pending, confirmed, cancelled, refunded", Alert.AlertType.ERROR);
            return false;
        }

        // Valider les champs numériques si fournis
        if (!priceText.isEmpty()) {
            try {
                double price = Double.parseDouble(priceText);
                if (price <= 0) {
                    showAlert("Invalid Price", "Price must be a positive number", Alert.AlertType.ERROR);
                    return false;
                }
                if (price > 100000) {
                    showAlert("Invalid Price", "Price cannot exceed 100,000 DT", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Price", "Price must be a valid number", Alert.AlertType.ERROR);
                return false;
            }
        }

        if (!quantityText.isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    showAlert("Invalid Quantity", "Quantity must be a positive number", Alert.AlertType.ERROR);
                    return false;
                }
                if (quantity > 1000) {
                    showAlert("Invalid Quantity", "Quantity cannot exceed 1,000", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Quantity", "Quantity must be a valid number", Alert.AlertType.ERROR);
                    return false;
            }
        }

        if (!buyerId.isEmpty()) {
            try {
                int buyerIdInt = Integer.parseInt(buyerId);
                if (buyerIdInt <= 0) {
                    showAlert("Invalid Buyer ID", "Buyer ID must be a positive number", Alert.AlertType.ERROR);
                    return false;
                }
            } catch (NumberFormatException e) {
                showAlert("Invalid Buyer ID", "Buyer ID must be a valid number", Alert.AlertType.ERROR);
                return false;
            }
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
