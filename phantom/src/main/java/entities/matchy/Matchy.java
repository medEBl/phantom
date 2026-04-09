package entities.matchy;

import java.time.LocalDateTime;

public class Matchy {
    private int id;
    private String game;
    private LocalDateTime matchDate;
    private Integer scoreTeam1;
    private Integer scoreTeam2;
    private String status; // planned, ongoing, finished
    private Integer team1Id;
    private Integer team2Id;
    private Integer winnerTeamId;
    private String location;
    private Double latitude;
    private Double longitude;

    // Champs pour affichage (noms des équipes)
    private String team1Name;
    private String team2Name;
    private String winnerTeamName;

    public Matchy() {}

    public Matchy(String game, LocalDateTime matchDate, String status, Integer team1Id,
                  Integer team2Id, String location, Double latitude, Double longitude) {
        this.game = game;
        this.matchDate = matchDate;
        this.status = status;
        this.team1Id = team1Id;
        this.team2Id = team2Id;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }

    public Integer getScoreTeam1() { return scoreTeam1; }
    public void setScoreTeam1(Integer scoreTeam1) { this.scoreTeam1 = scoreTeam1; }

    public Integer getScoreTeam2() { return scoreTeam2; }
    public void setScoreTeam2(Integer scoreTeam2) { this.scoreTeam2 = scoreTeam2; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTeam1Id() { return team1Id; }
    public void setTeam1Id(Integer team1Id) { this.team1Id = team1Id; }

    public Integer getTeam2Id() { return team2Id; }
    public void setTeam2Id(Integer team2Id) { this.team2Id = team2Id; }

    public Integer getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(Integer winnerTeamId) { this.winnerTeamId = winnerTeamId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getTeam1Name() { return team1Name; }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }

    public String getTeam2Name() { return team2Name; }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }

    public String getWinnerTeamName() { return winnerTeamName; }
    public void setWinnerTeamName(String winnerTeamName) { this.winnerTeamName = winnerTeamName; }

    @Override
    public String toString() {
        return "Matchy{id=" + id + ", game='" + game + "', status='" + status + "'}";
    }
}