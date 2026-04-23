package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import services.questionnaire.QuestionnaireService;

import javafx.geometry.Pos;
import java.io.IOException;
import java.util.List;

public class ListQuestionnairesController {

    // --- Sidebar Buttons (Admin Panel) ---
    @FXML private Button btnNavAgents;
    @FXML private Button btnDisconnect;

    // --- Main Content ---
    @FXML private Button btnNouveau;
    @FXML private TextField tfSearch;
    @FXML private TableView<Questionnaire> questionnaireTable;

    @FXML private TableColumn<Questionnaire, String> colGame;
    @FXML private TableColumn<Questionnaire, String> colQ1;
    @FXML private TableColumn<Questionnaire, String> colActions;

    private QuestionnaireService service;
    private ObservableList<Questionnaire> observableList;
    private FilteredList<Questionnaire> filteredData; // Used for search

    @FXML
    public void initialize() {
        service = new QuestionnaireService();

        // 1. Initialize the lists for Search and Sort
        observableList = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(observableList, b -> true);

        // 2. Wrap the FilteredList in a SortedList
        SortedList<Questionnaire> sortedData = new SortedList<>(filteredData);

        // 3. Bind the SortedList comparator to the TableView comparator
        sortedData.comparatorProperty().bind(questionnaireTable.comparatorProperty());

        // 4. Add sorted (and filtered) data to the table
        questionnaireTable.setItems(sortedData);

        // 5. Setup Search Bar Listener
        setupSearchFilter();

        // 6. Map Columns to Entity Properties
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colQ1.setCellValueFactory(new PropertyValueFactory<>("ques1"));

        // 7. Setup Actions Column (View, Edit, Delete buttons)
        colActions.setCellFactory(column -> new TableCell<Questionnaire, String>() {

            // 1. ICÔNES SEULEMENT (Pour gagner de l'espace)
            final Button btnDetails = new Button("👁");
            final Button btnModifier = new Button("✎");
            final Button btnSupprimer = new Button("🗑");

            // Espacement réduit à 8px pour bien rentrer dans la colonne
            final HBox actionBox = new HBox(8, btnDetails, btnModifier, btnSupprimer);

            {
                actionBox.setAlignment(Pos.CENTER);

                // 2. AJOUT DES TOOLTIPS (Pour la compréhension de l'utilisateur)
                btnDetails.setTooltip(new Tooltip("Voir les détails"));
                btnModifier.setTooltip(new Tooltip("Modifier le questionnaire"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer le questionnaire"));

                // 3. STYLES (Utilisation de votre dark-theme.css)
                btnDetails.getStyleClass().addAll("btn-action-small", "btn-profil");
                btnModifier.getStyleClass().addAll("btn-action-small", "btn-edit");
                btnSupprimer.getStyleClass().addAll("btn-action-small", "btn-delete");

                // 4. ACTIONS DES BOUTONS
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
                    setGraphic(actionBox);
                }
            }
        });

        // 8. Load Data
        loadQuestionnaires();

        // 9. Main Button Actions
        btnNouveau.setOnAction(e -> openCreate());

        // 10. Sidebar Navigation Actions
        if (btnNavAgents != null) {
            btnNavAgents.setOnAction(e -> {
                try {
                    // Navigate to the Admin version of the Agents list
                    Parent root = FXMLLoader.load(getClass().getResource("/fxml/ListAgentsBack.fxml"));
                    btnNavAgents.getScene().setRoot(root);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (btnDisconnect != null) {
            btnDisconnect.setOnAction(e -> System.out.println("Déconnexion clicked!"));
        }
    }

    // --- RECHERCHE EN TEMPS RÉEL (JEU SEULEMENT) ---
    private void setupSearchFilter() {
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(q -> {
                    // Si le champ est vide, on affiche tout
                    if (newValue == null || newValue.isEmpty() || newValue.isBlank()) {
                        return true;
                    }

                    // On met tout en minuscules pour comparer facilement
                    String lowerCaseFilter = newValue.toLowerCase();

                    // Recherche UNIQUEMENT par le nom du Jeu
                    if (q.getGame() != null && q.getGame().toLowerCase().contains(lowerCaseFilter)) {
                        return true;
                    }

                    // Si le jeu ne correspond pas, on cache la ligne
                    return false;
                });
            });
        }

    }

    private void loadQuestionnaires() {
        List<Questionnaire> list = service.getAllQuestionnaires();
        // Utiliser setAll met à jour la liste source sans casser les liens Filtered/Sorted
        observableList.setAll(list);
    }

    private void deleteQuestionnaire(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce questionnaire ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            service.deleteQuestionnaire(id);
            loadQuestionnaires(); // Refresh the table
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
            controller.initData(q);
            btnNouveau.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openDetails(Questionnaire q) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionnaireDetails.fxml"));
            Parent root = loader.load();
            QuestionnaireDetailsController controller = loader.getController();
            controller.initData(q);
            btnNouveau.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}