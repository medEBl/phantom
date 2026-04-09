package Iservices.coachingsession;

import entities.coachingsession.CoachingSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ICoachingSessionService {
    // CRUD
    void createCoachingSession(CoachingSession session);
    void updateCoachingSession(CoachingSession session);
    void deleteCoachingSession(int id);
    Optional<CoachingSession> getCoachingSessionById(int id);
    List<CoachingSession> getAllCoachingSessions();

    // Filtres
    List<CoachingSession> getSessionsByCoach(int coachId);
    List<CoachingSession> getSessionsByTeam(int teamId);
    List<CoachingSession> getSessionsByTrainingPlan(int trainingPlanId);
    List<CoachingSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end);
    List<CoachingSession> getUpcomingSessions();
    List<CoachingSession> getPastSessions();

    // Validation
    boolean isCoachAvailable(int coachId, LocalDateTime sessionDate, int duration, Integer excludeSessionId);
}