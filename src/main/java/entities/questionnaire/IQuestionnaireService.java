package entities.questionnaire;

import java.util.List;
import java.util.Optional;

public interface IQuestionnaireService {
    void createQuestionnaire(Questionnaire q);
    List<Questionnaire> getAllQuestionnaires();
    Optional<Questionnaire> getQuestionnaireById(int id);
    void deleteQuestionnaire(int id);
}