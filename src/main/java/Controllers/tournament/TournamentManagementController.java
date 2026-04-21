package Controllers.tournament;

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
import services.tournament.TournamentService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentManagementController {

    @FXML private TableView<Tournament> tournamentTable;
    @FXML private TableColumn<Tournament, Integer> idColumn;
    @FXML private TableColumn<Tournament, String> nameColumn;
    @FXML private TableColumn<Tournament, String> gameColumn;
    @FXML private TableColumn<Tournament, LocalDate> startDateColumn;
    @FXML private TableColumn<Tournament, LocalDate> endDateColumn;
    @FXML private TableColumn<Tournament, String> phaseColumn;
    @FXML private TableColumn<Tournament, Integer> maxTeamsColumn;
    @FXML private TableColumn<Tournament, String> statusColumn;
    @FXML private TableColumn<Tournament, Void> actionsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> gameFilter;
    @FXML private ComboBox<String> phaseFilter;
    @FXML private Button backButton;

    private final TournamentService tournamentService = new TournamentService();
    private ObservableList<Tournament> tournamentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupFilters();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        gameColumn.setCellValueFactory(new PropertyValueFactory<>("game"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        phaseColumn.setCellValueFactory(new PropertyValueFactory<>("phase"));
        maxTeamsColumn.setCellValueFactory(new PropertyValueFactory<>("maxTeams"));
        
        // Status Column with custom active/inactive text
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Tournament t = getTableRow().getItem();
                    setText(t.isActive() ? "ACTIVE" : "INACTIVE");
                    getStyleClass().removeAll("status-success", "status-danger");
                    getStyleClass().add(t.isActive() ? "status-success" : "status-danger");
                }
            }
        });

        setupActionButtons();
    }

    private void setupActionButtons() {
        Callback<TableColumn<Tournament, Void>, TableCell<Tournament, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Tournament, Void> call(final TableColumn<Tournament, Void> param) {
                return new TableCell<>() {
                        private final Button editBtn = new Button();
                        private final Button deleteBtn = new Button();
                        private final Button regBtn = new Button();
                        private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(editBtn, regBtn, deleteBtn);

                        {
                            pane.setSpacing(8);
                            pane.setAlignment(javafx.geometry.Pos.CENTER);
                            
                            // Edit Button - Pencil SVG
                            javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
                            editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                            editIcon.setFill(javafx.scene.paint.Color.WHITE);
                            editIcon.setScaleX(0.8);
                            editIcon.setScaleY(0.8);
                            editBtn.setGraphic(editIcon);
                            editBtn.setTooltip(new Tooltip("Edit Tournament"));
                            editBtn.getStyleClass().add("secondary-button");
                            editBtn.setStyle("-fx-padding: 8;");

                            // Registrations Button - Teams SVG
                            javafx.scene.shape.SVGPath regIcon = new javafx.scene.shape.SVGPath();
                            regIcon.setContent("M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5s-3 1.34-3 3 1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z");
                            regIcon.setFill(javafx.scene.paint.Color.WHITE);
                            regIcon.setScaleX(0.8);
                            regIcon.setScaleY(0.8);
                            regBtn.setGraphic(regIcon);
                            regBtn.setTooltip(new Tooltip("Manage Registrations"));
                            regBtn.getStyleClass().add("secondary-button");
                            regBtn.setStyle("-fx-padding: 8;");
                            
                            // Delete Button - Trash SVG
                            javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                            deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                            deleteIcon.setFill(javafx.scene.paint.Color.WHITE);
                            deleteIcon.setScaleX(0.8);
                            deleteIcon.setScaleY(0.8);
                            deleteBtn.setGraphic(deleteIcon);
                            deleteBtn.setTooltip(new Tooltip("Delete Tournament"));
                            deleteBtn.getStyleClass().add("logout-button");
                            deleteBtn.setStyle("-fx-padding: 8;");
                            
                            editBtn.setOnAction(event -> {
                                Tournament t = getTableView().getItems().get(getIndex());
                                handleEditTournament(t);
                            });

                            regBtn.setOnAction(event -> {
                                Tournament t = getTableView().getItems().get(getIndex());
                                handleManageRegistrations(t);
                            });
                            
                            deleteBtn.setOnAction(event -> {
                                Tournament t = getTableView().getItems().get(getIndex());
                                handleDeleteTournament(t);
                            });
                        }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        actionsColumn.setCellFactory(cellFactory);
    }

    private void loadData() {
        tournamentList.setAll(tournamentService.getAllTournaments());
        tournamentTable.setItems(tournamentList);
    }

    private void setupFilters() {
        List<String> games = tournamentService.getAllTournaments().stream()
                .map(Tournament::getGame)
                .distinct()
                .collect(Collectors.toList());
        gameFilter.setItems(FXCollections.observableArrayList(games));
        
        phaseFilter.setItems(FXCollections.observableArrayList("registrations_open", "ongoing", "finished"));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase();
        String game = gameFilter.getValue();
        String phase = phaseFilter.getValue();

        List<Tournament> filtered = tournamentService.getAllTournaments().stream()
                .filter(t -> t.getName().toLowerCase().contains(query) || t.getGame().toLowerCase().contains(query))
                .filter(t -> game == null || t.getGame().equals(game))
                .filter(t -> phase == null || t.getPhase().equals(phase))
                .collect(Collectors.toList());
        
        tournamentTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        gameFilter.setValue(null);
        phaseFilter.setValue(null);
        loadData();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard/dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTournament() {
        openForm(null);
    }

    private void handleEditTournament(Tournament t) {
        openForm(t);
    }

    private void handleManageRegistrations(Tournament t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/registration_management.fxml"));
            Parent root = loader.load();
            
            RegistrationManagementController controller = loader.getController();
            controller.setTournament(t);
            
            Stage stage = (Stage) tournamentTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDeleteTournament(Tournament t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Are you sure you want to delete tournament: " + t.getName() + "?");
        alert.setContentText("This action cannot be undone.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            tournamentService.deleteTournament(t.getId());
            loadData();
        }
    }

    private void openForm(Tournament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/form.fxml"));
            Parent root = loader.load();
            
            TournamentFormController controller = loader.getController();
            if (tournament != null) {
                controller.setTournament(tournament);
            }
            
            Stage stage = (Stage) tournamentTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
