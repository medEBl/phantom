package entities.team;

import java.time.LocalDateTime;

public class Team {
    private int id;
    private String name;
    private String game;
    private LocalDateTime creationDate;
    private int coachId;
    private String coachName; // Pour affichage

    public Team() {}

    public Team(String name, String game, LocalDateTime creationDate, int coachId) {
        this.name = name;
        this.game = game;
        this.creationDate = creationDate;
        this.coachId = coachId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    @Override
    public String toString() {
        return "Team{id=" + id + ", name='" + name + "', game='" + game + "', coachId=" + coachId + "}";
    }
}