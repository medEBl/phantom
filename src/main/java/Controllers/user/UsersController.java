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
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

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
    private Button exportPDFButton;
    
    @FXML
    private Button exportExcelButton;
    
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
    private TableColumn<User, Boolean> statusColumn;
    
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
        // Test database connection first
        try {
            List<User> testUsers = userService.getAllUsers();
            System.out.println("Database connection test: Found " + testUsers.size() + " users total");
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            showAlert("Database Error", "Cannot connect to database: " + e.getMessage());
            return;
        }
        
        setupTableColumns();
        setupFilters();
        setupSearchListeners();
        loadUsers();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            currentUserLabel.setText("USER: " + user.getFullName().toUpperCase());
        } else {
            currentUserLabel.setText("USER: NOT LOGGED IN");
        }
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
        statusColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean isActive, boolean empty) {
                super.updateItem(isActive, empty);
                if (empty || isActive == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(isActive ? "Active" : "Inactive");
                    if (isActive) {
                        setStyle("-fx-text-fill: #2dff8b; -fx-font-weight: 600;");
                    } else {
                        setStyle("-fx-text-fill: #ff2d2d; -fx-font-weight: 600;");
                    }
                }
            }
        });
        createdDateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        // Set up actions column with edit/delete buttons
        actionsColumn.setCellFactory(param -> new TableCell<User, Void>() {
            private final Button editButton = new Button("EDIT");
            private final Button deleteButton = new Button("DELETE");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5px 10px;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 5px 10px;");
                
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
        roleFilter.getItems().addAll("All", "ADMIN", "PLAYER", "ORGANIZER", "COACH");
        roleFilter.setValue("All");
        
        // Setup status filter
        statusFilter.getItems().addAll("All", "Active", "Inactive");
        statusFilter.setValue("All");
    }

    private void setupSearchListeners() {
        // Add listener for search field (with debounce)
        Timeline searchDelay = new Timeline(
            new KeyFrame(Duration.millis(300), event -> {
                currentPage = 1;
                loadUsers();
            })
        );
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDelay.stop(); // Stop previous timer
            searchDelay.playFromStart(); // Restart timer
        });
        
        // Add listeners for filters
        roleFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            loadUsers();
        });
        
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 1;
            loadUsers();
        });
    }

    private void loadUsers() {
        try {
            // Get search and filter values
            String searchTerm = searchField.getText().trim();
            String role = roleFilter.getValue() != null ? roleFilter.getValue().toString() : "All";
            String status = statusFilter.getValue() != null ? statusFilter.getValue().toString() : "All";
            
            System.out.println("Loading users with params:");
            System.out.println("  Search term: '" + searchTerm + "'");
            System.out.println("  Role: '" + role + "'");
            System.out.println("  Status: '" + status + "'");
            System.out.println("  Page: " + currentPage + "/" + totalPages);
            
            // Get users with search and filters
            List<User> users = userService.searchUsers(searchTerm, role, status, currentPage, pageSize);
            
            System.out.println("Found " + users.size() + " users");
            usersTable.getItems().setAll(users);
            
            // Update pagination info
            int totalCount = userService.countUsers(searchTerm, role, status);
            totalPages = (int) Math.ceil((double) totalCount / pageSize);
            updatePaginationInfo();
            
            System.out.println("Total count: " + totalCount + ", Total pages: " + totalPages);
            
        } catch (Exception e) {
            System.err.println("Error in loadUsers: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("Back button clicked!");
            System.out.println("Current user: " + (currentUser != null ? currentUser.getFullName() : "NULL"));
            System.out.println("User role: " + (currentUser != null ? currentUser.getRole() : "NULL"));
            
            String fxmlPath;
            String title;
            
            // Default to home page for safety
            fxmlPath = "/home/home.fxml";
            title = "Home - Phantom App";
            
            // Check if current user is admin and navigate accordingly
            if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                // Admin user - go back to dashboard
                fxmlPath = "/dashboard/dashboard.fxml";
                title = "Admin Dashboard - Phantom App";
                System.out.println("Navigating to dashboard...");
            } else {
                // Regular user - go back to home
                System.out.println("Navigating to home...");
            }
            
            System.out.println("Loading FXML: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");
            
            // Pass current user back to appropriate controller
            if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                Controllers.admin.DashboardController controller = loader.getController();
                System.out.println("Dashboard controller loaded");
            } else {
                Controllers.home.HomeController controller = loader.getController();
                if (currentUser != null) {
                    controller.setCurrentUser(currentUser);
                    System.out.println("Home controller loaded with user");
                }
            }
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle(title);
            System.out.println("Scene changed successfully");
            
        } catch (IOException e) {
            System.err.println("Cannot go back: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in handleBack: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        // Reset to first page when searching
        currentPage = 1;
        loadUsers();
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        roleFilter.setValue("All");
        statusFilter.setValue("All");
        currentPage = 1; // Reset to first page
        loadUsers();
    }

    @FXML
    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/add-user.fxml"));
            Parent root = loader.load();
            
            // Pass admin user context to add user controller
            Controllers.user.AddUserController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) addUserButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Add User - Phantom Admin");
            
        } catch (IOException e) {
            System.err.println("Cannot open add user: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditUser(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/edit-user.fxml"));
            Parent root = loader.load();
            
            // Pass user to edit controller
            Controllers.user.EditUserController controller = loader.getController();
            controller.setCurrentUser(user);
            
            Stage stage = (Stage) usersTable.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Edit User - Phantom Admin");
            
        } catch (IOException e) {
            System.err.println("Cannot open edit user: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteUser(User user) {
        try {
            // Delete user directly without confirmation
            userService.deleteUser(user.getId());
            System.out.println("User deleted: " + user.getFullName() + " (ID: " + user.getId() + ")");
            
            // Adjust current page if necessary
            List<User> remainingUsers = userService.searchUsers(
                searchField.getText().trim(),
                roleFilter.getValue() != null ? roleFilter.getValue().toString() : "All",
                statusFilter.getValue() != null ? statusFilter.getValue().toString() : "All",
                currentPage, pageSize);
            
            if (remainingUsers.isEmpty() && currentPage > 1) {
                currentPage--;
            }
            
            loadUsers();
            
        } catch (Exception e) {
            showAlert("Error", "Failed to delete user: " + e.getMessage());
            System.err.println("Cannot delete user: " + e.getMessage());
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

    @FXML
    private void handleExportPDF() {
        try {
            // Get all users for export (not just current page)
            String searchTerm = searchField.getText().trim();
            String role = roleFilter.getValue() != null ? roleFilter.getValue().toString() : "All";
            String status = statusFilter.getValue() != null ? statusFilter.getValue().toString() : "All";
            
            List<User> allUsers = userService.searchUsers(searchTerm, role, status, 1, Integer.MAX_VALUE);
            
            if (allUsers.isEmpty()) {
                showAlert("Export", "No users found to export.");
                return;
            }
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Users to PDF");
            
            // Set initial file name
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileChooser.setInitialFileName("users_report_" + timestamp + ".pdf");
            
            // Set extension filter
            fileChooser.getExtensionFilters().add(new ExtensionFilter("PDF Files", "*.pdf"));
            
            // Show save dialog
            File selectedFile = fileChooser.showSaveDialog(exportPDFButton.getScene().getWindow());
            
            if (selectedFile != null) {
                userService.exportUsersToPDF(allUsers, selectedFile.getAbsolutePath());
                showAlert("Export Successful", "PDF exported successfully to:\n" + selectedFile.getAbsolutePath());
                System.out.println("PDF exported to: " + selectedFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            showAlert("Export Error", "Failed to export PDF: " + e.getMessage());
            System.err.println("PDF export error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportExcel() {
        try {
            // Get all users for export (not just current page)
            String searchTerm = searchField.getText().trim();
            String role = roleFilter.getValue() != null ? roleFilter.getValue().toString() : "All";
            String status = statusFilter.getValue() != null ? statusFilter.getValue().toString() : "All";
            
            List<User> allUsers = userService.searchUsers(searchTerm, role, status, 1, Integer.MAX_VALUE);
            
            if (allUsers.isEmpty()) {
                showAlert("Export", "No users found to export.");
                return;
            }
            
            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export Users to Excel");
            
            // Set initial file name
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileChooser.setInitialFileName("users_report_" + timestamp + ".xlsx");
            
            // Set extension filter
            fileChooser.getExtensionFilters().add(new ExtensionFilter("Excel Files", "*.xlsx"));
            
            // Show save dialog
            File selectedFile = fileChooser.showSaveDialog(exportExcelButton.getScene().getWindow());
            
            if (selectedFile != null) {
                userService.exportUsersToExcel(allUsers, selectedFile.getAbsolutePath());
                showAlert("Export Successful", "Excel exported successfully to:\n" + selectedFile.getAbsolutePath());
                System.out.println("Excel exported to: " + selectedFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            showAlert("Export Error", "Failed to export Excel: " + e.getMessage());
            System.err.println("Excel export error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
