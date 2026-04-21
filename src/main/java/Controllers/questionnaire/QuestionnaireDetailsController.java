package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class QuestionnaireDetailsController {

    @FXML private Label lblSubtitle, lblGame;
    @FXML private Label lblQ1, lblQ2, lblQ3, lblQ4;
    @FXML private HBox boxQ3, boxQ4;
    @FXML private Button btnRetour, btnModifier;

    private Questionnaire currentQ;

    @FXML
    public void initialize() {
        btnRetour.setOnAction(e -> navigateToList());

        btnModifier.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/agent/fxml/QuestionnaireEdit.fxml"));
                Parent root = loader.load();
                QuestionnaireEditController controller = loader.getController();
                controller.initData(currentQ); // Pass data directly to the Edit screen
                btnModifier.getScene().setRoot(root);
            } catch (IOException ex) { ex.printStackTrace(); }
        });
    }

    public void initData(Questionnaire q) {
        this.currentQ = q;
        lblSubtitle.setText("ID: #" + q.getId());
        lblGame.setText(q.getGame());

        lblQ1.setText(q.getQues1());
        lblQ2.setText(q.getQues2());

        // Hide Q3 if it doesn't exist
        if (q.getQues3() == null || q.getQues3().trim().isEmpty()) {
            boxQ3.setVisible(false); boxQ3.setManaged(false);
        } else {
            lblQ3.setText(q.getQues3());
        }

        // Hide Q4 if it doesn't exist
        if (q.getQues4() == null || q.getQues4().trim().isEmpty()) {
            boxQ4.setVisible(false); boxQ4.setManaged(false);
        } else {
            lblQ4.setText(q.getQues4());
        }
    }

    private void navigateToList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/agent/fxml/ListQuestionnaires.fxml"));
            btnRetour.getScene().setRoot(root);
        } catch (IOException e) { e.printStackTrace(); }
    }
}