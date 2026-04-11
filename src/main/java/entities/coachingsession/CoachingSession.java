package entities.coachingsession;

import java.time.LocalDateTime;

public class CoachingSession {
    private int id;
    private int coachId;
    private String coachName;       // Pour affichage
    private int teamId;
    private String teamName;        // Pour affichage
    private LocalDateTime sessionDate;
    private int duration;           // Durée en minutes
    private String notes;
    private int trainingPlanId;
    private String trainingPlanTitle; // Pour affichage

    public CoachingSession() {}

    public CoachingSession(int coachId, int teamId, LocalDateTime sessionDate,
                           int duration, String notes, int trainingPlanId) {
        this.coachId = coachId;
        this.teamId = teamId;
        this.sessionDate = sessionDate;
        this.duration = duration;
        this.notes = notes;
        this.trainingPlanId = trainingPlanId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getTrainingPlanId() { return trainingPlanId; }
    public void setTrainingPlanId(int trainingPlanId) { this.trainingPlanId = trainingPlanId; }

    public String getTrainingPlanTitle() { return trainingPlanTitle; }
    public void setTrainingPlanTitle(String trainingPlanTitle) { this.trainingPlanTitle = trainingPlanTitle; }

    @Override
    public String toString() {
        return "CoachingSession{id=" + id + ", date=" + sessionDate + ", duration=" + duration + "min}";
    }
}