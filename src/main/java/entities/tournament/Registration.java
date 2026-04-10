package entities.tournament;

import java.time.LocalDateTime;

public class Registration {
    private int id;
    private String teamName;
    private String contactEmail;
    private LocalDateTime createdAt;
    private int tournamentId;

    public Registration() {}

    public Registration(String teamName, String contactEmail, LocalDateTime createdAt, int tournamentId) {
        this.teamName = teamName;
        this.contactEmail = contactEmail;
        this.createdAt = createdAt;
        this.tournamentId = tournamentId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }

    @Override
    public String toString() {
        return "Registration{id=" + id + ", teamName='" + teamName + "', tournamentId=" + tournamentId + "}";
    }
}
