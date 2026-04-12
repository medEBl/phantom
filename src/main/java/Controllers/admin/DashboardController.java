package Controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController {

    @FXML
    private Button logoutButton;
    
    @FXML
    private Button navUsers;
    
    @FXML
    private Button navTournaments;
    
    @FXML
    private Button navTeams;
    
    @FXML
    private Button navMatches;
    
    @FXML
    private Button navReports;
    
    @FXML
    private Button navSettings;
    
    @FXML
    private Label adminNameLabel;
    
    @FXML
    private Label totalUsersLabel;
    
    @FXML
    private Label activeTournamentsLabel;
    
    @FXML
    private Label totalTeamsLabel;
    
    @FXML
    private Label pendingApprovalsLabel;
    
    @FXML
    private TableView activityTable;
    
    @FXML
    private TableColumn timestampColumn;
    
    @FXML
    private TableColumn activityColumn;
    
    @FXML
    private TableColumn userColumn;
    
    @FXML
    private TableColumn statusColumn;

    @FXML
    public void initialize() {
        // Load dashboard data
        loadDashboardData();
        
        // Set up activity table
        setupActivityTable();
    }

    private void loadDashboardData() {
        // TODO: Load actual data from database
        adminNameLabel.setText("ADMIN: SYSTEM");
        totalUsersLabel.setText("0");
        activeTournamentsLabel.setText("0");
        totalTeamsLabel.setText("0");
        pendingApprovalsLabel.setText("0");
    }

    private void setupActivityTable() {
        // TODO: Set up table columns with data
        timestampColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("timestamp"));
        activityColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("activity"));
        userColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("user"));
        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
    }

    @FXML
    private void handleLogout() {
        try {
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Login - Phantom App");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUsers() {
        try {
            // Load users management screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/user/fxml/users.fxml"));
            Parent root = loader.load();
            
            // Pass current admin user to users controller
            Controllers.user.UsersController controller = loader.getController();
            // TODO: Set current user when admin user object is available
            
            Stage stage = (Stage) navUsers.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Users Management - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot open users management: " + e.getMessage());
        }
    }

    @FXML
    private void handleTournaments() {
        // TODO: Navigate to tournaments management
        System.out.println("Navigate to Tournaments Management");
    }

    @FXML
    private void handleTeams() {
        // TODO: Navigate to teams management
        System.out.println("Navigate to Teams Management");
    }

    @FXML
    private void handleMatches() {
        // TODO: Navigate to matches management
        System.out.println("Navigate to Matches Management");
    }

    @FXML
    private void handleReports() {
        // TODO: Navigate to reports
        System.out.println("Navigate to Reports");
    }

    @FXML
    private void handleSettings() {
        // TODO: Navigate to system settings
        System.out.println("Navigate to System Settings");
    }

    @FXML
    private void handleCreateTournament() {
        // TODO: Open create tournament dialog
        System.out.println("Create New Tournament");
    }

    @FXML
    private void manageUsers() {
        handleUsers();
    }

    @FXML
    private void viewReports() {
        handleReports();
    }

    @FXML
    private void systemSettings() {
        handleSettings();
    }
}
