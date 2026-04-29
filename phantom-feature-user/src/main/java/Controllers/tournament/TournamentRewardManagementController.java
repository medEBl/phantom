package Controllers.tournament;

import entities.tournament.Tournament;
import entities.tournament.TournamentReward;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import services.tournament.TournamentRewardService;

import java.io.IOException;
import java.util.List;

public class TournamentRewardManagementController {

    @FXML private BorderPane mainContainer;
    @FXML private TableView<TournamentReward> rewardTable;
    @FXML private TableColumn<TournamentReward, Integer> rankColumn;
    @FXML private TableColumn<TournamentReward, String> typeColumn;
    @FXML private TableColumn<TournamentReward, String> valueColumn;
    @FXML private TableColumn<TournamentReward, Void> actionsColumn;
    @FXML private Label tournamentNameLabel;
    @FXML private Button backButton;

    private final TournamentRewardService rewardService = new TournamentRewardService();
    private Tournament tournament;
    private entities.user.User currentUser;
    private boolean isUserMode = false;
    private ObservableList<TournamentReward> rewardList = FXCollections.observableArrayList();

    public void initUserMode(entities.user.User user) {
        this.currentUser = user;
        this.isUserMode = true;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
        tournamentNameLabel.setText("Tournament: " + tournament.getName());
        loadRewards();
    }

    @FXML
    public void initialize() {
        tools.AnimatedBackground.addAnimatedBackground(mainContainer);
        rankColumn.setCellValueFactory(new PropertyValueFactory<>("rank"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("rewardType"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("rewardValue"));
        setupActionButtons();
    }

    private void loadRewards() {
        if (tournament != null) {
            rewardList.setAll(rewardService.getRewardsByTournament(tournament.getId()));
            rewardTable.setItems(rewardList);
        }
    }

    private void setupActionButtons() {
        Callback<TableColumn<TournamentReward, Void>, TableCell<TournamentReward, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<TournamentReward, Void> call(final TableColumn<TournamentReward, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button();
                    private final Button deleteBtn = new Button();
                    private final javafx.scene.layout.HBox pane = new javafx.scene.layout.HBox(editBtn, deleteBtn);

                    {
                        pane.setSpacing(10);
                        pane.setAlignment(javafx.geometry.Pos.CENTER);

                        // Edit Button - Pencil SVG
                        javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
                        editIcon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
                        editIcon.setFill(javafx.scene.paint.Color.WHITE);
                        editIcon.setScaleX(0.8);
                        editIcon.setScaleY(0.8);
                        editBtn.setGraphic(editIcon);
                        editBtn.setTooltip(new Tooltip("Edit Reward"));
                        editBtn.getStyleClass().add("secondary-button");
                        editBtn.setStyle("-fx-padding: 8;");

                        // Delete Button - Trash SVG
                        javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
                        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
                        deleteIcon.setFill(javafx.scene.paint.Color.WHITE);
                        deleteIcon.setScaleX(0.8);
                        deleteIcon.setScaleY(0.8);
                        deleteBtn.setGraphic(deleteIcon);
                        deleteBtn.setTooltip(new Tooltip("Delete Reward"));
                        deleteBtn.getStyleClass().add("logout-button");
                        deleteBtn.setStyle("-fx-padding: 8;");
                        
                        editBtn.setOnAction(event -> handleEditReward(getTableView().getItems().get(getIndex())));
                        deleteBtn.setOnAction(event -> handleDeleteReward(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : pane);
                    }
                };
            }
        };
        actionsColumn.setCellFactory(cellFactory);
    }

    @FXML
    private void handleAddReward() {
        openForm(null);
    }

    private void handleEditReward(TournamentReward reward) {
        openForm(reward);
    }

    private void handleDeleteReward(TournamentReward reward) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Delete reward for rank " + reward.getRank() + "?");
        if (alert.showAndWait().get() == ButtonType.OK) {
            rewardService.deleteReward(reward.getId());
            loadRewards();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/form.fxml"));
            Parent root = loader.load();
            TournamentFormController controller = loader.getController();
            
            if (isUserMode) {
                controller.initUserMode(currentUser);
            }
            controller.setTournament(tournament);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openForm(TournamentReward reward) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/reward_form.fxml"));
            Parent root = loader.load();
            
            TournamentRewardFormController controller = loader.getController();
            if (isUserMode) {
                controller.initUserMode(currentUser);
            }
            controller.initData(tournament, reward);
            
            Stage stage = (Stage) rewardTable.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
