package Iservices.trainingplan;

import entities.trainingplan.TrainingPlan;
import java.util.List;
import java.util.Optional;

public interface ITrainingPlanService {
    // CRUD
    void createTrainingPlan(TrainingPlan plan);
    void updateTrainingPlan(TrainingPlan plan);
    void deleteTrainingPlan(int id);
    Optional<TrainingPlan> getTrainingPlanById(int id);
    List<TrainingPlan> getAllTrainingPlans();

    // Filtres
    List<TrainingPlan> getPlansByCoach(int coachId);
    List<TrainingPlan> getPlansByTeam(int teamId);
    List<TrainingPlan> getPlansByDifficulty(String difficultyLevel);
    List<TrainingPlan> getPlansByFocusArea(String focusArea);
    List<TrainingPlan> getPlansByTeamAndCoach(int teamId, int coachId);

    boolean titleExists(String title);
}