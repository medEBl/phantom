package Iservices.team;

import entities.team.Team;
import java.util.List;
import java.util.Optional;

public interface ITeamService {
    // CRUD
    void createTeam(Team team);
    void updateTeam(Team team);
    void deleteTeam(int id);
    Optional<Team> getTeamById(int id);
    List<Team> getAllTeams();

    // Filtres
    List<Team> getTeamsByGame(String game);
    List<Team> getTeamsByCoach(int coachId);
    boolean teamNameExists(String name);
    List<Team> getAvailableTeams(); // Équipes qui peuvent jouer un match
    
    // Coach management
    List<Integer> getValidCoachIds();
}