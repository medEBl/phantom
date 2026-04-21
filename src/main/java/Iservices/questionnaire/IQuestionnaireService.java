package Iservices.questionnaire;

import entities.questionnaire.Questionnaire;
import java.util.List;
import java.util.Optional;

public interface IQuestionnaireService {
    void createQuestionnaire(Questionnaire q);
    List<Questionnaire> getAllQuestionnaires();
    Optional<Questionnaire> getQuestionnaireById(int id);
    void deleteQuestionnaire(int id);

    // Add these two missing dashboard methods!
    int getTotalAgentsCount();
    int getFilledQuestionnairesCount();
}