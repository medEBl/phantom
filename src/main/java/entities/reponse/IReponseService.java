package entities.reponse;

import java.util.List;
import java.util.Optional;

public interface IReponseService {
    void createReponse(Reponse r);
    List<Reponse> getAllReponses();
    Optional<Reponse> getReponseById(int id);
    void deleteReponse(int id);
}