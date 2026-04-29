package Iservices.tournament;

import entities.tournament.Tournament;
import java.util.List;
import java.util.Optional;

public interface ITournamentService {
    // CRUD
    void createTournament(Tournament tournament);
    void updateTournament(Tournament tournament);
    void deleteTournament(int id);
    Optional<Tournament> getTournamentById(int id);
    List<Tournament> getAllTournaments();

    // Search and Filters
    List<Tournament> getTournamentsByGame(String game);
    List<Tournament> getTournamentsByPhase(String phase);
    List<Tournament> getActiveTournaments();
    List<Tournament> searchTournaments(String query, String game, String phase);
    boolean tournamentNameExists(String name);
}
