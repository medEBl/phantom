package entities.tournament;

import java.time.LocalDate;

public class Tournament {
    private int id;
    private String name;
    private String game;
    private LocalDate startDate;
    private LocalDate endDate;
    private String phase;
    private int organizerId;
    private boolean isActive;
    private String posterPath;
    private String posterPrompt;
    private int maxTeams;

    public Tournament() {}

    public Tournament(String name, String game, LocalDate startDate, LocalDate endDate,
                      String phase, int organizerId, boolean isActive, int maxTeams) {
        this.name = name;
        this.game = game;
        this.startDate = startDate;
        this.endDate = endDate;
        this.phase = phase;
        this.organizerId = organizerId;
        this.isActive = isActive;
        this.maxTeams = maxTeams;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getPhase() { return phase; }
    public void setPhase(String phase) { this.phase = phase; }

    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getPosterPrompt() { return posterPrompt; }
    public void setPosterPrompt(String posterPrompt) { this.posterPrompt = posterPrompt; }

    public int getMaxTeams() { return maxTeams; }
    public void setMaxTeams(int maxTeams) { this.maxTeams = maxTeams; }

    @Override
    public String toString() {
        return "Tournament{id=" + id + ", name='" + name + "', game='" + game +
                "', phase='" + phase + "', organizerId=" + organizerId + ", active=" + isActive + "}";
    }
}
