package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.questionnaire.QuestionnaireService;

import java.io.IOException;
import java.util.List;

public class ListQuestionnairesController {

    @FXML private Button btnNouveau;
    @FXML private TextField tfSearch;
    @FXML private TableView<Questionnaire> questionnaireTable;

    @FXML private TableColumn<Questionnaire, Integer> colId;
    @FXML private TableColumn<Questionnaire, String> colGame;
    @FXML private TableColumn<Questionnaire, String> colQ1;
    @FXML private TableColumn<Questionnaire, String> colActions;

    private QuestionnaireService service;
    private ObservableList<Questionnaire> observableList;

    @FXML
    public void initialize() {
        service = new QuestionnaireService();

        // 1. Map Columns to Entity Properties
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colQ1.setCellValueFactory(new PropertyValueFactory<>("ques1"));

        // 2. Setup Actions Column (View, Edit, Delete buttons inside the table)
        colActions.setCellFactory(column -> new TableCell<Questionnaire, String>() {
            final Button btnDetails = new Button("👁");
            final Button btnModifier = new Button("✎");
            final Button btnSupprimer = new Button("🗑");
            final HBox actionButtons = new HBox(10, btnDetails, btnModifier, btnSupprimer);

            {
                // Styling the buttons
                btnDetails.setStyle("-fx-background-color: transparent; -fx-border-color: #4da6ff; -fx-text-fill: #4da6ff; -fx-cursor: hand; -fx-border-radius: 4;");
                btnModifier.setStyle("-fx-background-color: transparent; -fx-border-color: #d99846; -fx-text-fill: #d99846; -fx-cursor: hand; -fx-border-radius: 4;");
                btnSupprimer.setStyle("-fx-background-color: transparent; -fx-border-color: #ff3b3f; -fx-text-fill: #ff3b3f; -fx-cursor: hand; -fx-border-radius: 4;");
                actionButtons.setStyle("-fx-alignment: center;");

                // Button Click Actions
                btnDetails.setOnAction(e -> openDetails(getTableView().getItems().get(getIndex())));
                btnModifier.setOnAction(e -> openEdit(getTableView().getItems().get(getIndex())));
                btnSupprimer.setOnAction(e -> {
                    Questionnaire q = getTableView().getItems().get(getIndex());
                    deleteQuestionnaire(q.getId());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    setGraphic(actionButtons);
                }
                setStyle("-fx-border-width: 0;");
            }
        });

        // 3. Load Data into the Table
        loadQuestionnaires();

        // 4. Setup "Nouveau" Button
        btnNouveau.setOnAction(e -> openCreate());
    }

    private void loadQuestionnaires() {
        List<Questionnaire> list = service.getAllQuestionnaires();
        observableList = FXCollections.observableArrayList(list);
        questionnaireTable.setItems(observableList);
    }

    private void deleteQuestionnaire(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce questionnaire ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            service.deleteQuestionnaire(id);
            loadQuestionnaires(); // Refresh the table automatically
        }
    }

    // --- NAVIGATION METHODS ---

    private void openCreate() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/QuestionnaireCreate.fxml"));
            btnNouveau.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openEdit(Questionnaire q) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionnaireEdit.fxml"));
            Parent root = loader.load();
            QuestionnaireEditController controller = loader.getController();
            controller.initData(q); // Pass the data to the Edit screen
            btnNouveau.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openDetails(Questionnaire q) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionnaireDetails.fxml"));
            Parent root = loader.load();
            QuestionnaireDetailsController controller = loader.getController();
            controller.initData(q); // Pass the data to the Details screen
            btnNouveau.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}