package Controllers.admin;

import entities.shop.ShopItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import services.shop.ShopService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ShopItemController {

    @FXML private TableView<ShopItem> shopItemsTable;
    @FXML private TableColumn<ShopItem, Integer> colId;
    @FXML private TableColumn<ShopItem, Integer> colBuyerId;
    @FXML private TableColumn<ShopItem, String> colItemName;
    @FXML private TableColumn<ShopItem, String> colItemDescription;
    @FXML private TableColumn<ShopItem, String> colItemCategory;
    @FXML private TableColumn<ShopItem, Double> colPrice;
    @FXML private TableColumn<ShopItem, Integer> colQuantity;
    @FXML private TableColumn<ShopItem, Double> colTotalPrice;
    @FXML private TableColumn<ShopItem, String> colStatus;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategoryCombo;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private Text totalItemsLabel;
    @FXML private Text totalValueLabel;
    @FXML private Button addShopItemButton;
    @FXML private Button editShopItemButton;
    @FXML private Button deleteShopItemButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button navPayments;

    private final ShopService shopService = new ShopService();
    private ObservableList<ShopItem> shopItemsList;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadShopItems();
        setupStatistics();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBuyerId.setCellValueFactory(new PropertyValueFactory<>("buyerId"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        colItemDescription.setCellValueFactory(new PropertyValueFactory<>("itemDescription"));
        colItemCategory.setCellValueFactory(new PropertyValueFactory<>("itemCategory"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colTotalPrice.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Style the table
        shopItemsTable.setStyle("-fx-font-size: 14px; -fx-cell-size: 40px;");
    }

    private void setupFilters() {
        filterCategoryCombo.getItems().addAll(
            "Tous", "electronics", "clothing", "books", "games", "sports", "other"
        );
        filterCategoryCombo.setValue("Tous");
        
        filterStatusCombo.getItems().addAll(
            "Tous", "pending", "confirmed", "cancelled", "refunded"
        );
        filterStatusCombo.setValue("Tous");
        
        // Add search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterShopItems());
        filterCategoryCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterShopItems());
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterShopItems());
    }

    private void loadShopItems() {
        List<ShopItem> shopItems = shopService.getAllShopItems();
        shopItemsList = FXCollections.observableArrayList(shopItems);
        shopItemsTable.setItems(shopItemsList);
        updateStatistics();
    }

    private void filterShopItems() {
        String searchText = searchField.getText().toLowerCase();
        String categoryFilter = filterCategoryCombo.getValue();
        String statusFilter = filterStatusCombo.getValue();
        
        ObservableList<ShopItem> filteredList = FXCollections.observableArrayList();
        
        for (ShopItem shopItem : shopItemsList) {
            boolean matchesSearch = searchText.isEmpty() || 
                shopItem.getItemName().toLowerCase().contains(searchText) ||
                shopItem.getItemDescription().toLowerCase().contains(searchText) ||
                String.valueOf(shopItem.getId()).contains(searchText);
            
            boolean matchesCategory = categoryFilter.equals("Tous") || 
                shopItem.getItemCategory().equalsIgnoreCase(categoryFilter);
            
            boolean matchesStatus = statusFilter.equals("Tous") || 
                shopItem.getStatus().equalsIgnoreCase(statusFilter);
            
            if (matchesSearch && matchesCategory && matchesStatus) {
                filteredList.add(shopItem);
            }
        }
        
        shopItemsTable.setItems(filteredList);
        updateStatistics();
    }

    private void setupStatistics() {
        // Initialize statistics labels
        totalItemsLabel.setText("0");
        totalValueLabel.setText("0.00 DT");
    }

    private void updateStatistics() {
        List<ShopItem> currentItems = shopItemsTable.getItems();
        
        int totalCount = currentItems.size();
        double totalValue = currentItems.stream()
            .filter(item -> "confirmed".equalsIgnoreCase(item.getStatus()))
            .mapToDouble(ShopItem::getTotalPrice)
            .sum();
        
        totalItemsLabel.setText(String.valueOf(totalCount));
        totalValueLabel.setText(String.format("%.2f DT", totalValue));
    }

    @FXML
    private void handleAddShopItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/shop-item-add.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) addShopItemButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Add Shop Item - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot open add shop item form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditShopItem() {
        ShopItem selected = shopItemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a shop item to edit", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/shop-item-edit.fxml"));
            Parent root = loader.load();
            
            AdminEditShopItemController controller = loader.getController();
            controller.setShopItemData(selected);
            
            Stage stage = (Stage) editShopItemButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Edit Shop Item - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot open edit shop item form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteShopItem() {
        ShopItem selected = shopItemsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a shop item to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Shop Item?");
        confirm.setContentText("Are you sure you want to delete item " + selected.getItemName() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                shopService.deleteShopItem(selected.getId());
                loadShopItems();
                showAlert("Success", "Shop item deleted successfully", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to delete shop item: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadShopItems();
        showAlert("Refreshed", "Shop items list has been updated", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handlePayments() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/payments.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navPayments.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Payment Management - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot open payment management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Phantom Admin Dashboard");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot return to dashboard: " + e.getMessage(), Alert.AlertType.ERROR);
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
