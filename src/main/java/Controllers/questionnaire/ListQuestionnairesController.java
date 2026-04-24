package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import services.questionnaire.QuestionnaireService;
import tools.Phantom;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class ListQuestionnairesController {

    // --- Sidebar Buttons ---
    @FXML private Button btnNavAgents;
    @FXML private Button btnDisconnect;

    // --- Dashboard Elements ---
    @FXML private Label lblTotalAgents;
    @FXML private Label lblCompleted;
    @FXML private WebView chartWebView;

    // --- Main Content ---
    @FXML private Button btnNouveau;
    @FXML private TextField tfSearch;
    @FXML private TableView<Questionnaire> questionnaireTable;

    @FXML private TableColumn<Questionnaire, String> colGame;
    @FXML private TableColumn<Questionnaire, String> colQ1;
    @FXML private TableColumn<Questionnaire, String> colActions;

    private QuestionnaireService service;
    private ObservableList<Questionnaire> observableList;
    private FilteredList<Questionnaire> filteredData;

    @FXML
    public void initialize() {
        service = new QuestionnaireService();

        // 1. Initialize lists for Search and Sort
        observableList = FXCollections.observableArrayList();
        filteredData = new FilteredList<>(observableList, b -> true);
        SortedList<Questionnaire> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(questionnaireTable.comparatorProperty());
        questionnaireTable.setItems(sortedData);

        setupSearchFilter();

        // 2. Map Columns
        colGame.setCellValueFactory(new PropertyValueFactory<>("game"));
        colQ1.setCellValueFactory(new PropertyValueFactory<>("ques1"));

        // 3. Setup Actions Column
        colActions.setCellFactory(column -> new TableCell<Questionnaire, String>() {
            final Button btnDetails = new Button("👁");
            final Button btnModifier = new Button("✎");
            final Button btnSupprimer = new Button("🗑");
            final HBox actionBox = new HBox(8, btnDetails, btnModifier, btnSupprimer);

            {
                actionBox.setAlignment(Pos.CENTER);
                btnDetails.setTooltip(new Tooltip("Voir les détails"));
                btnModifier.setTooltip(new Tooltip("Modifier le questionnaire"));
                btnSupprimer.setTooltip(new Tooltip("Supprimer le questionnaire"));

                btnDetails.getStyleClass().addAll("btn-action-small", "btn-profil");
                btnModifier.getStyleClass().addAll("btn-action-small", "btn-edit");
                btnSupprimer.getStyleClass().addAll("btn-action-small", "btn-delete");

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

        // 4. Load Table Data & Real-Time Dashboard
        loadQuestionnaires();
        loadDashboardStats();

        // 5. Navigation Actions
        btnNouveau.setOnAction(e -> openCreate());

        if (btnNavAgents != null) {
            btnNavAgents.setOnAction(e -> {
                try {
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

    // --- DASHBOARD LOGIC ---
    private void loadDashboardStats() {
        int totalAgents = 0;
        int completed = 0;

        // Fetch Total Agents
        String sqlTotal = "SELECT COUNT(*) FROM agent";
        try {
            Connection cnx = Phantom.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(sqlTotal);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalAgents = rs.getInt(1);
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }

        // Fetch Completed Questionnaires
        String sqlCompleted = "SELECT COUNT(DISTINCT id_agent) FROM reponse_questionnaire";
        try {
            Connection cnx = Phantom.getInstance().getCnx();
            PreparedStatement ps = cnx.prepareStatement(sqlCompleted);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) completed = rs.getInt(1);
            rs.close(); ps.close();
        } catch (Exception e) { e.printStackTrace(); }

        if (lblTotalAgents != null) lblTotalAgents.setText(String.valueOf(totalAgents));
        if (lblCompleted != null) lblCompleted.setText(String.valueOf(completed));

        if (chartWebView != null) {
            int notCompleted = totalAgents - completed;
            if (notCompleted < 0) notCompleted = 0;

            // Calcul automatique du pourcentage
            int percentage = 0;
            if (totalAgents > 0) {
                percentage = (int) Math.round((double) completed / totalAgents * 100);
            }

            // On passe le pourcentage à la méthode
            generateGoogleChart(completed, notCompleted, percentage);
        }
    }

    private void generateGoogleChart(int completed, int notCompleted, int percentage) {
        WebEngine webEngine = chartWebView.getEngine();

        String htmlContent = String.format("""
            <html>
              <head>
                <script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
                <script type="text/javascript">
                  google.charts.load('current', {'packages':['corechart']});
                  google.charts.setOnLoadCallback(drawChart);

                  function drawChart() {
                    var data = google.visualization.arrayToDataTable([
                      ['Status', 'Count'],
                      ['Complété', %d],
                      ['En attente', %d]
                    ]);

                    var options = {
                      backgroundColor: '#16161e',
                      legend: 'none',
                      pieHole: 0.65,
                      pieSliceText: 'none',
                      pieSliceBorderColor: '#16161e',
                      colors: ['#00ffff', '#2a2a35'],
                      chartArea: {left:10, top:10, width:'90%%', height:'90%%'}
                    };

                    var chart = new google.visualization.PieChart(document.getElementById('donutchart'));
                    chart.draw(data, options);
                  }
                </script>
              </head>
              <body style="margin: 0; padding: 0; background-color: #16161e; overflow: hidden; position: relative;">
                <div id="donutchart" style="width: 100%%; height: 100%%;"></div>
                
                <div style="position: absolute; top: 50%%; left: 50%%; transform: translate(-50%%, -50%%); color: white; font-family: 'Segoe UI', Helvetica, sans-serif; font-size: 22px; font-weight: bold; pointer-events: none;">
                  %d%%
                </div>
              </body>
            </html>
            """, completed, notCompleted, percentage);

        webEngine.loadContent(htmlContent);
    }

    // --- RECHERCHE EN TEMPS RÉEL (JEU SEULEMENT) ---
    private void setupSearchFilter() {
        if (tfSearch != null) {
            tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(q -> {
                    if (newValue == null || newValue.isEmpty() || newValue.isBlank()) return true;
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (q.getGame() != null && q.getGame().toLowerCase().contains(lowerCaseFilter)) return true;
                    return false;
                });
            });
        }
    }

    private void loadQuestionnaires() {
        List<Questionnaire> list = service.getAllQuestionnaires();
        observableList.setAll(list);
    }

    private void deleteQuestionnaire(int id) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer ce questionnaire ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            service.deleteQuestionnaire(id);
            loadQuestionnaires();
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