package Controllers.tournament;

import entities.tournament.Registration;
import entities.tournament.Tournament;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.tournament.RegistrationService;

import java.io.IOException;
import java.time.LocalDateTime;

public class RegistrationManagementController {

    @FXML private TableView<Registration> registrationTable;
    @FXML private TableColumn<Registration, Integer> idColumn;
    @FXML private TableColumn<Registration, String> teamNameColumn;
    @FXML private TableColumn<Registration, String> emailColumn;
    @FXML private TableColumn<Registration, LocalDateTime> dateColumn;
    @FXML private TableColumn<Registration, Void> actionsColumn;

    @FXML private Label tournamentNameLabel;
    @FXML private Button backButton;
    @FXML private Button registerButton;

    private final RegistrationService registrationService = new RegistrationService();
    private Tournament tournament;
    private entities.user.User currentUser;
    private boolean isUserMode = false;
    private ObservableList<Registration> registrationList = FXCollections.observableArrayList();

    public void initUserMode(entities.user.User user) {
        this.currentUser = user;
        this.isUserMode = true;
    }

    @FXML
    public void initialize() {
        setupTable();
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        tournamentNameLabel.setText("Tournament: " + tournament.getName());
        loadData();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        teamNameColumn.setCellValueFactory(new PropertyValueFactory<>("teamName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        setupActionButtons();
    }

    private void setupActionButtons() {
        Callback<TableColumn<Registration, Void>, TableCell<Registration, Void>> cellFactory = param -> new TableCell<>() {
            private final Button deleteBtn = new Button();
            {
                // Delete Button - Trash SVG
                javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                deleteIcon.setFill(javafx.scene.paint.Color.WHITE);
                deleteIcon.setScaleX(0.8);
                deleteIcon.setScaleY(0.8);
                deleteBtn.setGraphic(deleteIcon);
                deleteBtn.setTooltip(new Tooltip("Remove Team"));
                deleteBtn.getStyleClass().add("logout-button");
                deleteBtn.setStyle("-fx-padding: 8;");

                deleteBtn.setOnAction(event -> {
                    Registration r = getTableView().getItems().get(getIndex());
                    handleDeleteRegistration(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(deleteBtn);
            }
        };
        actionsColumn.setCellFactory(cellFactory);
    }

    private void loadData() {
        if (tournament != null) {
            java.util.List<Registration> regs = registrationService.getRegistrationsByTournament(tournament.getId());
            registrationList.setAll(regs);
            registrationTable.setItems(registrationList);
            
            // Disable register button if tournament is full
            if (regs.size() >= tournament.getMaxTeams()) {
                registerButton.setDisable(true);
                registerButton.setText("🚫 TOURNAMENT FULL");
            } else {
                registerButton.setDisable(false);
                registerButton.setText("➕ REGISTER TEAM");
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            String fxmlPath = isUserMode ? "/tournament/fxml/list.fxml" : "/tournament/fxml/management.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            if (isUserMode) {
                TournamentListController controller = loader.getController();
                controller.setCurrentUser(currentUser);
            }
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/registration_form.fxml"));
            Parent root = loader.load();
            
            RegistrationFormController controller = loader.getController();
            controller.initData(tournament, isUserMode);
            if (currentUser != null) {
                controller.initUserMode(currentUser);
            }
            
            Stage stage = (Stage) registrationTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteRegistration(Registration r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete registration for " + r.getTeamName() + "?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            registrationService.deleteRegistration(r.getId());
            loadData();
        }
    }
}
