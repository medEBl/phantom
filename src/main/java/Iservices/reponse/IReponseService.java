package Iservices.reponse;

import entities.reponse.Reponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IReponseService {
    void createReponse(Reponse r);
    List<Reponse> getAllReponses();
    Optional<Reponse> getReponseById(int id);
    void deleteReponse(int id);

    // Add these three missing method signatures!
    void updateReponse(Reponse r);
    Optional<Reponse> getReponseByAgentId(int idAgent);
    Map<String, Object> getQuestionnaireByGame(String game);
}