package entities.tournament;

public class TournamentReward {
    private int id;
    private int tournamentId;
    private int rank;
    private String rewardType;
    private String rewardValue;

    public TournamentReward() {}

    public TournamentReward(int tournamentId, int rank, String rewardType, String rewardValue) {
        this.tournamentId = tournamentId;
        this.rank = rank;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }

    public String getRewardValue() { return rewardValue; }
    public void setRewardValue(String rewardValue) { this.rewardValue = rewardValue; }

    @Override
    public String toString() {
        return "TournamentReward{id=" + id + ", tournamentId=" + tournamentId + ", rank=" + rank +
                ", type='" + rewardType + "', value='" + rewardValue + "'}";
    }
}
