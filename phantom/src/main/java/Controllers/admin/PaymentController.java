package Controllers.admin;

import entities.shop.Payment;
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
import services.shop.PaymentService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class PaymentController {

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Integer> colId;
    @FXML private TableColumn<Payment, Double> colAmount;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, String> colStatus;
    @FXML private TableColumn<Payment, String> colTransactionRef;
    @FXML private TableColumn<Payment, Integer> colShopItemId;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatusCombo;
    @FXML private Text totalPaymentsLabel;
    @FXML private Text totalRevenueLabel;
    @FXML private Button addPaymentButton;
    @FXML private Button editPaymentButton;
    @FXML private Button deletePaymentButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private final PaymentService paymentService = new PaymentService();
    private ObservableList<Payment> paymentsList;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadPayments();
        setupStatistics();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colTransactionRef.setCellValueFactory(new PropertyValueFactory<>("transactionRef"));
        colShopItemId.setCellValueFactory(new PropertyValueFactory<>("shopItemId"));
        
        // Style the table
        paymentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        paymentsTable.setStyle("-fx-font-size: 14px; -fx-cell-size: 40px;");
    }

    private void setupFilters() {
        filterStatusCombo.getItems().addAll(
            "Tous", "pending", "success", "failed", "refunded", "cancelled"
        );
        filterStatusCombo.setValue("Tous");
        
        // Add search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterPayments());
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterPayments());
    }

    private void loadPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        paymentsList = FXCollections.observableArrayList(payments);
        paymentsTable.setItems(paymentsList);
        updateStatistics();
    }

    private void filterPayments() {
        String searchText = searchField.getText().toLowerCase();
        String statusFilter = filterStatusCombo.getValue();
        
        ObservableList<Payment> filteredList = FXCollections.observableArrayList();
        
        for (Payment payment : paymentsList) {
            boolean matchesSearch = searchText.isEmpty() || 
                payment.getTransactionRef().toLowerCase().contains(searchText) ||
                String.valueOf(payment.getId()).contains(searchText) ||
                payment.getPaymentMethod().toLowerCase().contains(searchText);
            
            boolean matchesStatus = statusFilter.equals("Tous") || 
                payment.getPaymentStatus().equalsIgnoreCase(statusFilter);
            
            if (matchesSearch && matchesStatus) {
                filteredList.add(payment);
            }
        }
        
        paymentsTable.setItems(filteredList);
        updateStatistics();
    }

    private void setupStatistics() {
        // Initialize statistics labels
        totalPaymentsLabel.setText("0");
        totalRevenueLabel.setText("0.00 DT");
    }

    private void updateStatistics() {
        List<Payment> currentPayments = paymentsTable.getItems();
        
        int totalCount = currentPayments.size();
        double totalRevenue = currentPayments.stream()
            .filter(p -> "success".equalsIgnoreCase(p.getPaymentStatus()))
            .mapToDouble(Payment::getAmount)
            .sum();
        
        totalPaymentsLabel.setText(String.valueOf(totalCount));
        totalRevenueLabel.setText(String.format("%.2f DT", totalRevenue));
    }

    @FXML
    private void handleAddPayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/payment-add.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) addPaymentButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Add Payment - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot open add payment form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditPayment() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a payment to edit", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/payment-edit.fxml"));
            Parent root = loader.load();
            
            AdminEditPaymentController controller = loader.getController();
            controller.setPaymentData(selected);
            
            Stage stage = (Stage) editPaymentButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Edit Payment - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            showAlert("Error", "Cannot open edit payment form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeletePayment() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a payment to delete", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Payment?");
        confirm.setContentText("Are you sure you want to delete payment " + selected.getTransactionRef() + "?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                paymentService.deletePayment(selected.getId());
                loadPayments();
                showAlert("Success", "Payment deleted successfully", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to delete payment: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadPayments();
        showAlert("Refreshed", "Payment list has been updated", Alert.AlertType.INFORMATION);
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
