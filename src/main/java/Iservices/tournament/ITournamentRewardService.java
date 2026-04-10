package Iservices.tournament;

import entities.tournament.TournamentReward;
import java.util.List;
import java.util.Optional;

public interface ITournamentRewardService {
    void createReward(TournamentReward reward);
    void updateReward(TournamentReward reward);
    void deleteReward(int id);
    Optional<TournamentReward> getRewardById(int id);
    List<TournamentReward> getAllRewards();
    List<TournamentReward> getRewardsByTournament(int tournamentId);
}
