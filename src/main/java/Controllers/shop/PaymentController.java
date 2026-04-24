package Controllers.shop;

import entities.shop.Payment;
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
import services.shop.PaymentService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Integer> colId;
    @FXML private TableColumn<Payment, Double> colAmount;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, String> colStatus;
    @FXML private TableColumn<Payment, String> colTransactionRef;
    @FXML private TableColumn<Payment, Integer> colShopItemId;
    @FXML private VBox messageContainer;

    private final PaymentService paymentService = new PaymentService();
    private ObservableList<Payment> paymentsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadPayments();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));
        colTransactionRef.setCellValueFactory(new PropertyValueFactory<>("transactionRef"));
        colShopItemId.setCellValueFactory(new PropertyValueFactory<>("shopItemId"));
    }

    private void loadPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        paymentsList = FXCollections.observableArrayList(payments);
        paymentsTable.setItems(paymentsList);
        paymentsTable.refresh();
    }

    @FXML
    private void handleAddPayment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/add-payment.fxml"));
            Parent root = loader.load();
            
            // Get current stage and set new scene
            Stage currentStage = (Stage) paymentsTable.getScene().getWindow();
            currentStage.setTitle("Ajouter un paiement");
            currentStage.setScene(new Scene(root, 1920, 1080));
            currentStage.setMaximized(true);
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showPageMessage("Erreur", "Impossible d'ouvrir la fenêtre d'ajout", "error");
        }
    }

    @FXML
    private void handleEditPayment() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showPageMessage("Aucune sélection", "Veuillez sélectionner un paiement à modifier", "warning");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/edit-payment.fxml"));
            Parent root = loader.load();

            EditPaymentController controller = loader.getController();
            controller.setPaymentData(selected);

            // Get current stage and set new scene
            Stage currentStage = (Stage) paymentsTable.getScene().getWindow();
            currentStage.setTitle("Modifier un paiement");
            currentStage.setScene(new Scene(root, 1920, 1080));
            currentStage.setMaximized(true);
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la fenêtre de modification", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeletePayment() {
        Payment selected = paymentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showPageMessage("Aucune sélection", "Veuillez sélectionner un paiement à supprimer", "warning");
            return;
        }

        int id = selected.getId();
        String transactionRef = selected.getTransactionRef();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le paiement ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer le paiement " + transactionRef + " ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            paymentService.deletePayment(id);
            loadPayments();
            showAlert("Succès", "Paiement supprimé avec succès", Alert.AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleRefresh() {
        loadPayments();
        showAlert("Rafraîchi", "La liste des paiements a été mise à jour", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleFilterByStatus() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Filtrer par statut");
        dialog.setHeaderText("Statut du paiement");
        dialog.setContentText("Entrez le statut (pending / success / failed) :");

        dialog.showAndWait().ifPresent(status -> {
            List<Payment> filtered = paymentService.getPaymentsByStatus(status);
            if (filtered.isEmpty()) {
                showAlert("Aucun résultat", "Aucun paiement avec le statut '" + status + "'", Alert.AlertType.WARNING);
            } else {
                paymentsList = FXCollections.observableArrayList(filtered);
                paymentsTable.setItems(paymentsList);
                showAlert("Filtre appliqué", filtered.size() + " paiement(s) trouvé(s)", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    private void handleSearchByTransactionRef() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rechercher par référence");
        dialog.setHeaderText("Référence transaction");
        dialog.setContentText("Entrez la référence transaction :");

        dialog.showAndWait().ifPresent(ref -> {
            Optional<Payment> paymentOptional = paymentService.getPaymentByTransactionRef(ref);
            if (paymentOptional.isEmpty()) {
                showAlert("Non trouvé", "Aucun paiement avec la référence '" + ref + "'", Alert.AlertType.WARNING);
            } else {
                Payment payment = paymentOptional.get();
                paymentsList = FXCollections.observableArrayList(List.of(payment));
                paymentsTable.setItems(paymentsList);
                showAlert("Résultat", "Paiement trouvé", Alert.AlertType.INFORMATION);
            }
        });
    }

    @FXML
    private void handleShowAll() {
        loadPayments();
        showAlert("Affichage complet", "Tous les paiements sont affichés", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleBackToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) paymentsTable.getScene().getWindow();
            stage.setTitle("PHANTOM FORCE - HOME");

            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à l'accueil", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewItems() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/items.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) paymentsTable.getScene().getWindow();
            stage.setTitle("PHANTOM FORCE - SHOP ITEMS");
            stage.setScene(new Scene(root, 1920, 1080));
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page des articles", Alert.AlertType.ERROR);
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