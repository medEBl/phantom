package Controllers.shop;

import entities.shop.ShopItem;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.shop.ShopService;

import java.io.IOException;

public class EditItemController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private VBox messageContainer;

    private int currentId;
    private final ShopService shopService = new ShopService();

    public void setItemData(ShopItem item) {
        currentId = item.getId();
        idField.setText(String.valueOf(item.getId()));
        nameField.setText(item.getItemName());
        descField.setText(item.getItemDescription());
        categoryField.setText(item.getItemCategory());
        priceField.setText(String.valueOf(item.getPrice()));
        quantityField.setText(String.valueOf(item.getQuantity()));
    }

    @FXML
    private void handleUpdateItem() {
        try {
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String category = categoryField.getText().trim();
            String priceText = priceField.getText().trim();
            String quantityText = quantityField.getText().trim();

            // Validation des champs obligatoires
            if (!validateUpdateItemFields(name, priceText, quantityText)) {
                return;
            }

            // Validation et conversion des champs numériques
            if (!validateUpdateItemNumericFields(priceText, quantityText)) {
                return;
            }

            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            // Validation des valeurs
            if (!validateUpdateItemValues(name, desc, category, price, quantity)) {
                return;
            }

            // Create updated ShopItem object
            ShopItem updatedItem = new ShopItem(
                currentId, 0, name, desc, category, price, quantity,
                price * quantity, null, "pending"
            );
            
            // Update the item
            shopService.updateShopItem(updatedItem);

            showAlert("Succès", "Jeu modifié avec succès", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "Vérifiez que le prix et la quantité sont des nombres valides", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateUpdateItemFields(String name, String priceText, String quantityText) {
        // Valider nom
        if (name.isEmpty()) {
            showAlert("Champ manquant", "Le nom est obligatoire", Alert.AlertType.WARNING);
            return false;
        }

        // Valider prix
        if (priceText.isEmpty()) {
            showAlert("Champ manquant", "Le prix est obligatoire", Alert.AlertType.WARNING);
            return false;
        }

        // Valider quantité
        if (quantityText.isEmpty()) {
            showAlert("Champ manquant", "La quantité est obligatoire", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private boolean validateUpdateItemNumericFields(String priceText, String quantityText) {
        try {
            // Valider Prix
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showAlert("Prix invalide", "Le prix doit être un nombre positif", Alert.AlertType.ERROR);
                return false;
            }
            if (price > 10000) {
                showAlert("Prix invalide", "Le prix ne peut pas dépasser 10,000 DT", Alert.AlertType.ERROR);
                return false;
            }

            // Valider Quantité
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Quantité invalide", "La quantité doit être un nombre positif", Alert.AlertType.ERROR);
                return false;
            }
            if (quantity > 1000) {
                showAlert("Quantité invalide", "La quantité ne peut pas dépasser 1000", Alert.AlertType.ERROR);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Les champs numériques doivent contenir des nombres valides", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validateUpdateItemValues(String name, String desc, String category, double price, int quantity) {
        // Valider nom (longueur)
        if (name.length() < 2) {
            showAlert("Nom invalide", "Le nom doit contenir au moins 2 caractères", Alert.AlertType.ERROR);
            return false;
        }
        if (name.length() > 100) {
            showAlert("Nom invalide", "Le nom ne peut pas dépasser 100 caractères", Alert.AlertType.ERROR);
            return false;
        }

        // Valider description si fournie
        if (!desc.isEmpty() && desc.length() > 500) {
            showAlert("Description invalide", "La description ne peut pas dépasser 500 caractères", Alert.AlertType.ERROR);
            return false;
        }

        // Valider catégorie si fournie (longueur seulement)
        if (!category.isEmpty() && category.length() > 100) {
            showAlert("Catégorie invalide", "La catégorie ne peut pas dépasser 100 caractères", Alert.AlertType.ERROR);
            return false;
        }

        // Valider le prix total
        double totalPrice = price * quantity;
        if (totalPrice > 50000) {
            showAlert("Prix total invalide", "Le prix total ne peut pas dépasser 50,000 DT", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/items.fxml"));
            Parent root = loader.load();
            
            Stage currentStage = (Stage) idField.getScene().getWindow();
            currentStage.setTitle("PHANTOM FORCE - SHOP ITEMS");
            currentStage.setScene(new Scene(root, 1920, 1080));
            currentStage.setMaximized(true);
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste des articles", Alert.AlertType.ERROR);
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