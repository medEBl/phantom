package entities.trainingplan;

import java.time.LocalDateTime;

public class TrainingPlan {
    private int id;
    private String title;
    private String description;
    private String focusArea;      // Attaque, Défense, Tactique, etc.
    private String difficultyLevel; // Débutant, Intermédiaire, Avancé
    private int coachId;
    private String coachName;       // Pour affichage
    private LocalDateTime createdAt;
    private int teamId;
    private String teamName;        // Pour affichage

    public TrainingPlan() {}

    public TrainingPlan(String title, String description, String focusArea,
                        String difficultyLevel, int coachId, int teamId) {
        this.title = title;
        this.description = description;
        this.focusArea = focusArea;
        this.difficultyLevel = difficultyLevel;
        this.coachId = coachId;
        this.teamId = teamId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFocusArea() { return focusArea; }
    public void setFocusArea(String focusArea) { this.focusArea = focusArea; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public int getCoachId() { return coachId; }
    public void setCoachId(int coachId) { this.coachId = coachId; }

    public String getCoachName() { return coachName; }
    public void setCoachName(String coachName) { this.coachName = coachName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    @Override
    public String toString() {
        return "TrainingPlan{id=" + id + ", title='" + title + "', difficulty='" + difficultyLevel + "'}";
    }
}