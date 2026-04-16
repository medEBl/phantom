package Controllers.shop;

import entities.shop.ShopItem;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.shop.ShopService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ItemsController implements Initializable {

    @FXML private TableView<ShopItem> itemsTable;
    @FXML private TableColumn<ShopItem, Integer> colId;
    @FXML private TableColumn<ShopItem, String> colName;
    @FXML private TableColumn<ShopItem, String> colCategory;
    @FXML private TableColumn<ShopItem, Double> colPrice;
    @FXML private TableColumn<ShopItem, Integer> colQuantity;
    @FXML private TableColumn<ShopItem, String> colStatus;
    @FXML private VBox messageContainer;

    private final ShopService shopService = new ShopService();
    private ObservableList<ShopItem> itemsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadItems();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("itemCategory"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadItems() {
        List<ShopItem> items = shopService.getAllShopItems();
        itemsList = FXCollections.observableArrayList(items);
        itemsTable.setItems(itemsList);
    }

    @FXML
    private void handleAddItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/add-item.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("➕ Ajouter un jeu");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadItems(); // Rafraîchir après ajout
        } catch (IOException e) {
            e.printStackTrace();
            showPageMessage("Erreur", "Impossible d'ouvrir la fenêtre d'ajout", "error");
        }
    }

    @FXML
    private void handleEditItem() {
        ShopItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showPageMessage("Aucune sélection", "Veuillez sélectionner un jeu à modifier", "warning");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/edit-item.fxml"));
            Parent root = loader.load();

            EditItemController controller = loader.getController();
            controller.setItemData(selected);

            Stage stage = new Stage();
            stage.setTitle("✏️ Modifier un jeu");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadItems(); // Rafraîchir après modification
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteItem() {
        ShopItem selected = itemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showPageMessage("Aucune sélection", "Veuillez sélectionner un jeu à supprimer", "warning");
            return;
        }

        int id = selected.getId();
        String name = selected.getItemName();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le jeu ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + name + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            shopService.deleteShopItem(id);
            loadItems();
            showAlert("Succès", "Jeu supprimé avec succès", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleRefresh() {
        loadItems();
        showAlert("Rafraîchi", "La liste des jeux a été mise à jour", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleFilterByCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Filtrer par catégorie");
        dialog.setHeaderText("Catégorie du jeu");
        dialog.setContentText("Entrez la catégorie :");

        dialog.showAndWait().ifPresent(category -> {
            List<ShopItem> filtered = shopService.getShopItemsByCategory(category);
            if (filtered.isEmpty()) {
                showAlert("Aucun résultat", "Aucun jeu dans la catégorie '" + category + "'", Alert.AlertType.WARNING);
            } else {
                itemsList = FXCollections.observableArrayList(filtered);
                itemsTable.setItems(itemsList);
                showAlert("Filtre appliqué", filtered.size() + " jeu(x) trouvé(s)", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    private void handleFilterInStock() {
        List<ShopItem> inStock = shopService.getShopItemsInStock();
        if (inStock.isEmpty()) {
            showAlert("Aucun résultat", "Aucun jeu en stock", Alert.AlertType.WARNING);
        } else {
            itemsList = FXCollections.observableArrayList(inStock);
            itemsTable.setItems(itemsList);
            showAlert("Filtre appliqué", inStock.size() + " jeu(x) en stock", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleShowAll() {
        loadItems();
        showAlert("Affichage complet", "Tous les jeux sont affichés", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) itemsTable.getScene().getWindow();
            stage.setTitle("PHANTOM FORCE - HOME");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewPayments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/payments.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) itemsTable.getScene().getWindow();
            stage.setTitle("PHANTOM FORCE - PAYMENTS");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page des paiements", Alert.AlertType.ERROR);
        }
    }

    private void showPageMessage(String title, String message, String type) {
        messageContainer.getChildren().clear();
        
        Label messageLabel = new Label(title + ": " + message);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(600);
        
        // Apply styling based on message type
        messageLabel.getStyleClass().addAll("message", "message-" + type);
        
        messageContainer.getChildren().add(messageLabel);
        
        // Auto-hide success and info messages after 3 seconds
        if ("success".equals(type) || "info".equals(type)) {
            Duration delay = Duration.seconds(3);
            PauseTransition pause = new PauseTransition(delay);
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