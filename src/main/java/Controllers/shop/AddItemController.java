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

import java.sql.Timestamp;

public class AddItemController {
    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private TextField categoryField;
    @FXML private TextField priceField;
    @FXML private TextField quantityField;
    @FXML private TextField buyerIdField;
    @FXML private VBox messageContainer;

    private final ShopService shopService = new ShopService();

    @FXML
    private void handleAddItem() {
        try {

            if (!validateRequiredItemFields()) {
                return;
            }


            if (!validateItemNumericFields()) {
                return;
            }

            int id = Integer.parseInt(idField.getText().trim());
            int buyerId = buyerIdField.getText().trim().isEmpty() ? 0 : Integer.parseInt(buyerIdField.getText().trim());
            String name = nameField.getText().trim();
            String desc = descField.getText().trim();
            String category = categoryField.getText().trim();
            double price = Double.parseDouble(priceField.getText().trim());
            int quantity = quantityField.getText().trim().isEmpty() ? 1 : Integer.parseInt(quantityField.getText().trim());


            if (!validateItemValues(id, buyerId, name, desc, category, price, quantity)) {
                return;
            }


            ShopItem newItem = new ShopItem(
                id, buyerId, name, desc, category, price, quantity,
                price * quantity, new Timestamp(System.currentTimeMillis()), "pending"
            );
            
            shopService.createShopItem(newItem);

            showAlert("Succès", "Jeu ajouté avec succès", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "Vérifiez que l'ID, le prix et la quantité sont des nombres valides", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Erreur", e.getMessage(), Alert.AlertType.ERROR);
        }
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

    private boolean validateRequiredItemFields() {
        StringBuilder missingFields = new StringBuilder();
        
        if (idField.getText().trim().isEmpty()) {
            missingFields.append("ID, ");
        }
        if (nameField.getText().trim().isEmpty()) {
            missingFields.append("Nom, ");
        }
        if (priceField.getText().trim().isEmpty()) {
            missingFields.append("Prix, ");
        }

        if (missingFields.length() > 0) {
            String message = "Champs obligatoires manquants: " + 
                           missingFields.substring(0, missingFields.length() - 2);
            showAlert("Champs manquants", message, Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateItemNumericFields() {
        try {

            String idText = idField.getText().trim();
            int id = Integer.parseInt(idText);
            if (id <= 0) {
                showAlert("ID invalide", "L'ID doit être un nombre positif", Alert.AlertType.ERROR);
                return false;
            }


            String priceText = priceField.getText().trim();
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showAlert("Prix invalide", "Le prix doit être un nombre positif", Alert.AlertType.ERROR);
                return false;
            }
            if (price > 10000) {
                showAlert("Prix invalide", "Le prix ne peut pas dépasser 10,000 DT", Alert.AlertType.ERROR);
                return false;
            }


            String quantityText = quantityField.getText().trim();
            if (!quantityText.isEmpty()) {
                int quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) {
                    showAlert("Quantité invalide", "La quantité doit être un nombre positif", Alert.AlertType.ERROR);
                    return false;
                }
                if (quantity > 1000) {
                    showAlert("Quantité invalide", "La quantité ne peut pas dépasser 1000", Alert.AlertType.ERROR);
                    return false;
                }
            }


            String buyerIdText = buyerIdField.getText().trim();
            if (!buyerIdText.isEmpty()) {
                int buyerId = Integer.parseInt(buyerIdText);
                if (buyerId < 0) {
                    showAlert("ID Acheteur invalide", "L'ID de l'acheteur doit être un nombre positif ou 0", Alert.AlertType.ERROR);
                    return false;
                }
            }

            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur de format", "Les champs numériques doivent contenir des nombres valides", Alert.AlertType.ERROR);
            return false;
        }
    }

    private boolean validateItemValues(int id, int buyerId, String name, String desc, String category, double price, int quantity) {

        if (name.length() < 2) {
            showAlert("Nom invalide", "Le nom doit contenir au moins 2 caractères", Alert.AlertType.ERROR);
            return false;
        }
        if (name.length() > 100) {
            showAlert("Nom invalide", "Le nom ne peut pas dépasser 100 caractères", Alert.AlertType.ERROR);
            return false;
        }


        if (!desc.isEmpty() && desc.length() > 500) {
            showAlert("Description invalide", "La description ne peut pas dépasser 500 caractères", Alert.AlertType.ERROR);
            return false;
        }


        if (!category.isEmpty() && category.length() > 100) {
            showAlert("Catégorie invalide", "La catégorie ne peut pas dépasser 100 caractères", Alert.AlertType.ERROR);
            return false;
        }


        double totalPrice = price * quantity;
        if (totalPrice > 50000) {
            showAlert("Prix total invalide", "Le prix total ne peut pas dépasser 50,000 DT", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}