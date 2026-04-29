package Controllers.tournament;

import entities.tournament.Tournament;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import services.tournament.TournamentService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TournamentListController {

    @FXML private BorderPane mainContainer;
    @FXML private FlowPane cardsPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> gameFilter;
    @FXML private Button addButton;
    @FXML private Button backButton;
    @FXML private Label roleLabel;

    private final TournamentService tournamentService = new TournamentService();
    private List<Tournament> allTournaments;
    private entities.user.User currentUser;

    public void setCurrentUser(entities.user.User user) {
        this.currentUser = user;
        updateUIForRole();
    }

    private void updateUIForRole() {
        if (currentUser != null) {
            System.out.println("DEBUG: TournamentListController - Current User: " + currentUser.getEmail());
            System.out.println("DEBUG: Role (singular): " + currentUser.getRole());
            System.out.println("DEBUG: Roles (plural): " + currentUser.getRoles());
            
            boolean canManage = isAuthorized();
            System.out.println("DEBUG: Can Manage: " + canManage);
            
            roleLabel.setText("ROLE: " + currentUser.getRole() + (canManage ? " (AUTHORIZED)" : " (PLAYER VIEW)"));
            
            addButton.setVisible(canManage);
            addButton.setManaged(canManage);
            // Refresh cards to pass user session
            displayTournaments(allTournaments);
        }
    }

    private boolean isAuthorized() {
        if (currentUser == null) return false;
        
        String role = currentUser.getRole();
        String jsonRoles = currentUser.getRoles();
        
        // Check singular role (with and without ROLE_ prefix)
        if ("ADMIN".equalsIgnoreCase(role) || "ORGANIZER".equalsIgnoreCase(role) || 
            "ROLE_ADMIN".equalsIgnoreCase(role) || "ROLE_ORGANIZER".equalsIgnoreCase(role)) return true;
        
        // Fallback: check plural roles JSON string (Symfony compatibility)
        if (jsonRoles != null) {
            return jsonRoles.contains("ROLE_ADMIN") || jsonRoles.contains("ROLE_ORGANIZER");
        }
        
        return false;
    }

    @FXML
    public void initialize() {
        tools.AnimatedBackground.addAnimatedBackground(mainContainer);
        allTournaments = tournamentService.getAllTournaments();
        setupFilters();
        setupSearchListeners();
        displayTournaments(allTournaments);
    }

    private void setupSearchListeners() {
        Timeline searchDelay = new Timeline(
            new KeyFrame(Duration.millis(300), event -> {
                handleSearch();
            })
        );

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDelay.stop();
            searchDelay.playFromStart();
        });

        gameFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            handleSearch();
        });
    }

    private void setupFilters() {
        List<String> games = allTournaments.stream()
                .map(Tournament::getGame)
                .distinct()
                .collect(Collectors.toList());
        gameFilter.setItems(FXCollections.observableArrayList(games));
    }

    private void displayTournaments(List<Tournament> tournaments) {
        cardsPane.getChildren().clear();
        for (Tournament t : tournaments) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/card.fxml"));
                Parent card = loader.load();
                
                TournamentCardController controller = loader.getController();
                controller.setCurrentUser(currentUser);
                controller.setData(t);
                
                cardsPane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        String selectedGame = gameFilter.getValue();

        List<Tournament> filtered = tournamentService.searchTournaments(query, selectedGame, null);
        displayTournaments(filtered);
    }

    @FXML
    private void handleReset() {
        searchField.clear();
        gameFilter.setValue(null);
        displayTournaments(allTournaments);
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tournament/fxml/form.fxml"));
            Parent root = loader.load();
            
            TournamentFormController controller = loader.getController();
            controller.initUserMode(currentUser);
            
            Stage stage = (Stage) addButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/home/home.fxml"));
            Parent root = loader.load();
            
            Controllers.home.HomeController controller = loader.getController();
            controller.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 1920, 1080));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
