package Controllers.user;

import entities.user.User;
import services.user.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UsersController {

    private final UserService userService = new UserService();
    private User currentUser;
    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize = 10;

    @FXML
    private Button backButton;
    
    @FXML
    private Label currentUserLabel;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private ComboBox roleFilter;
    
    @FXML
    private ComboBox statusFilter;
    
    @FXML
    private Button searchButton;
    
    @FXML
    private Button clearButton;
    
    @FXML
    private Button addUserButton;
    
    @FXML
    private TableView<User> usersTable;
    
    @FXML
    private TableColumn<User, Integer> idColumn;
    
    @FXML
    private TableColumn<User, String> usernameColumn;
    
    @FXML
    private TableColumn<User, String> fullNameColumn;
    
    @FXML
    private TableColumn<User, String> emailColumn;
    
    @FXML
    private TableColumn<User, String> roleColumn;
    
    @FXML
    private TableColumn<User, String> countryColumn;
    
    @FXML
    private TableColumn<User, String> statusColumn;
    
    @FXML
    private TableColumn<User, String> createdDateColumn;
    
    @FXML
    private TableColumn<User, Void> actionsColumn;
    
    @FXML
    private Button prevPageButton;
    
    @FXML
    private Label pageInfoLabel;
    
    @FXML
    private Button nextPageButton;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadUsers();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        currentUserLabel.setText("USER: " + user.getFullName().toUpperCase());
    }

    private void setupTableColumns() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Set up actions column with edit/delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editButton = new Button("EDIT");
            private final Button deleteButton = new Button("DELETE");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleEditUser(user);
                });
                
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupFilters() {
        // Setup role filter
        roleFilter.getItems().addAll("All", "admin", "player", "organizer", "coach");
        roleFilter.setValue("All");
        
        // Setup status filter
        statusFilter.getItems().addAll("All", "Active", "Inactive");
        statusFilter.setValue("All");
    }

    private void loadUsers() {
        try {
            // TODO: Implement pagination and filtering
            List<User> users = userService.getAllUsers();
            usersTable.getItems().setAll(users);
            
            // Update pagination info
            totalPages = (int) Math.ceil((double) users.size() / pageSize);
            updatePaginationInfo();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void updatePaginationInfo() {
        pageInfoLabel.setText("Page " + currentPage + " of " + totalPages);
        prevPageButton.setDisable(currentPage <= 1);
        nextPageButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
            Parent root = loader.load();
            
            // Pass current user back to home controller
            Controllers.home.HomeController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Home - Phantom App");
            
        } catch (IOException e) {
            showAlert("Error", "Cannot navigate back: " + e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        // TODO: Implement search functionality
        System.out.println("Search users with filters");
        loadUsers();
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        roleFilter.setValue("All");
        statusFilter.setValue("All");
        loadUsers();
    }

    @FXML
    private void handleAddUser() {
        // TODO: Open add user dialog
        System.out.println("Add new user");
    }

    @FXML
    private void handleEditUser(User user) {
        // TODO: Open edit user dialog
        System.out.println("Edit user: " + user.getFullName());
    }

    @FXML
    private void handleDeleteUser(User user) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete User");
        confirmDialog.setContentText("Are you sure you want to delete user " + user.getFullName() + "?");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                userService.deleteUser(user.getId());
                loadUsers();
                showAlert("Success", "User deleted successfully");
            } catch (Exception e) {
                showAlert("Error", "Failed to delete user: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadUsers();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            loadUsers();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
