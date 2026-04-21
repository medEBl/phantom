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
    void updateReponse(Reponse r); // Required for Edit
    Optional<Reponse> getReponseByAgentId(int idAgent); // Required for Edit
    Map<String, Object> getQuestionnaireByGame(String game); // Required for both
}