package Iservices.matchy;

import entities.matchy.Matchy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface IMatchyService {
    // CRUD
    void createMatch(Matchy match);
    void updateMatch(Matchy match);
    void deleteMatch(int id);
    Optional<Matchy> getMatchById(int id);
    List<Matchy> getAllMatches();

    // Filtres
    List<Matchy> getMatchesByGame(String game);
    List<Matchy> getMatchesByStatus(String status);
    List<Matchy> getMatchesByTeam(int teamId);
    List<Matchy> getMatchesByDateRange(LocalDateTime start, LocalDateTime end);
    List<Matchy> getUpcomingMatches();
    List<Matchy> getFinishedMatches();

    // Opérations spécifiques
    void updateMatchResult(int matchId, int scoreTeam1, int scoreTeam2);
    void cancelMatch(int matchId);
}