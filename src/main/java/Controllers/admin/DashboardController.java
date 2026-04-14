package Controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController {

    @FXML
    private Button logoutButton;
    
    @FXML
    private Button navDashboard;
    
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
    private Button navCoaching;

    @FXML
    private Button navShop;

    @FXML
    private Button createTournamentButton;

    @FXML
    private Button manageUsersButton;

    @FXML
    private Button createTeamButton;

    @FXML
    private Button scheduleMatchButton;

    @FXML
    private Button viewReportsButton;

    @FXML
    private Button systemSettingsButton;

    @FXML
    private Button exportDataButton;

    @FXML
    private Button backupDatabaseButton;

    @FXML
    private Button clearCacheButton;
    
    @FXML
    private Label adminNameLabel;
    
    @FXML
    private Label lastLoginLabel;
    
    @FXML
    private Label totalUsersLabel;
    
    @FXML
    private Label activeTournamentsLabel;
    
    @FXML
    private Label totalTeamsLabel;
    
    @FXML
    private Label pendingApprovalsLabel;
    
    @FXML
    private Label totalRevenueLabel;
    
    @FXML
    private Label matchesPlayedLabel;
    
    @FXML
    private Label activePlayersLabel;
    
    @FXML
    private Label conversionRateLabel;
    
    @FXML
    private TableView activityTable;
    
    @FXML
    private LineChart userGrowthChart;
    
    @FXML
    private PieChart tournamentChart;
    
    @FXML
    private Label cpuUsageLabel;
    
    @FXML
    private ProgressBar cpuProgressBar;
    
    @FXML
    private Label memoryUsageLabel;
    
    @FXML
    private ProgressBar memoryProgressBar;
    
    @FXML
    private Label connectionsLabel;
    
    @FXML
    private VBox recentUsersList;
    
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
        lastLoginLabel.setText("Last login: Today");
        totalUsersLabel.setText("0");
        activeTournamentsLabel.setText("0");
        totalTeamsLabel.setText("0");
        pendingApprovalsLabel.setText("0");
        totalRevenueLabel.setText("$0");
        matchesPlayedLabel.setText("0");
        activePlayersLabel.setText("0");
        conversionRateLabel.setText("0%");
        cpuUsageLabel.setText("45%");
        memoryUsageLabel.setText("62%");
        connectionsLabel.setText("1,234");
        
        // Set progress bar values
        cpuProgressBar.setProgress(0.45);
        memoryProgressBar.setProgress(0.62);
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
            
            // Create a simple admin user for now (TODO: get actual logged-in admin)
            entities.user.User adminUser = new entities.user.User();
            adminUser.setFullName("ADMIN");
            adminUser.setEmail("admin@phantom.com");
            adminUser.setRole("ADMIN");
            adminUser.setUsername("admin");
            
            controller.setCurrentUser(adminUser);
            
            Stage stage = (Stage) navUsers.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.show();
            stage.setTitle("Users Management - Phantom App");
            
        } catch (IOException e) {
            System.err.println("Cannot open users management: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error in handleUsers: " + e.getMessage());
        }
    }

    @FXML
    private void handleMedadleUsers() {
        // Handle typos in FXML by calling the correct method
        handleUsers();
    }

    @FXML
    private void handleTournaments() {
        // TODO: Navigate to tournaments management
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/settings.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navSettings.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Settings - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open settings: " + e.getMessage());
        }
    }

    @FXML
    private void handleCoaching() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/coaching/fxml/list.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navCoaching.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Coaching - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open coaching: " + e.getMessage());
        }
    }

    @FXML
    private void handleShop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shop/fxml/admin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navShop.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Shop Admin - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open shop admin: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reports/fxml/generate.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navReports.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Generate Report - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open report generator: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewAllActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/activity/fxml/admin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) navReports.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Activity Log - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open activity log: " + e.getMessage());
        }
    }

    @FXML
    private void handleCreateTeam() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/team/fxml/create.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) createTeamButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Create Team - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open team creator: " + e.getMessage());
        }
    }

    @FXML
    private void handleScheduleMatch() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/match/fxml/schedule.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) scheduleMatchButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Schedule Match - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open match scheduler: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportData() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/export.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) exportDataButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Export Data - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open data export: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackupDatabase() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/backup.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) backupDatabaseButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Backup Database - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open database backup: " + e.getMessage());
        }
    }

    @FXML
    private void handleClearCache() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/admin/fxml/cache.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) clearCacheButton.getScene().getWindow();
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.setFullScreen(true);
            stage.setTitle("Clear Cache - Phantom Admin");
            stage.show();
            
        } catch (IOException e) {
            System.err.println("Cannot open cache manager: " + e.getMessage());
        }
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
