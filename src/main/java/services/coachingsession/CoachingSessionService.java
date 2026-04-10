package services.coachingsession;

import Iservices.coachingsession.ICoachingSessionService;
import entities.coachingsession.CoachingSession;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CoachingSessionService implements ICoachingSessionService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private CoachingSession mapRow(ResultSet rs) throws SQLException {
        CoachingSession cs = new CoachingSession();
        cs.setId(rs.getInt("id"));
        cs.setCoachId(rs.getInt("coach_id"));
        cs.setTeamId(rs.getInt("team_id"));

        Timestamp ts = rs.getTimestamp("session_date");
        if (ts != null) cs.setSessionDate(ts.toLocalDateTime());

        cs.setDuration(rs.getInt("duration"));
        cs.setNotes(rs.getString("notes"));
        cs.setTrainingPlanId(rs.getInt("training_plan_id"));

        // Récupérer le nom du coach
        try {
            String coachSql = "SELECT username FROM user WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(coachSql)) {
                ps.setInt(1, cs.getCoachId());
                ResultSet rsCoach = ps.executeQuery();
                if (rsCoach.next()) {
                    cs.setCoachName(rsCoach.getString("username"));
                }
            }
        } catch (SQLException e) {
            cs.setCoachName("Unknown");
        }

        // Récupérer le nom de l'équipe
        try {
            String teamSql = "SELECT name FROM team WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(teamSql)) {
                ps.setInt(1, cs.getTeamId());
                ResultSet rsTeam = ps.executeQuery();
                if (rsTeam.next()) {
                    cs.setTeamName(rsTeam.getString("name"));
                }
            }
        } catch (SQLException e) {
            cs.setTeamName("Unknown");
        }

        // Récupérer le titre du plan d'entraînement
        try {
            String planSql = "SELECT title FROM training_plan WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(planSql)) {
                ps.setInt(1, cs.getTrainingPlanId());
                ResultSet rsPlan = ps.executeQuery();
                if (rsPlan.next()) {
                    cs.setTrainingPlanTitle(rsPlan.getString("title"));
                }
            }
        } catch (SQLException e) {
            cs.setTrainingPlanTitle("Unknown");
        }

        return cs;
    }

    @Override
    public void createCoachingSession(CoachingSession session) {
        // Vérifier que le coach existe et a le rôle COACH
        if (!isValidCoach(session.getCoachId())) {
            throw new IllegalArgumentException("Invalid coach ID. User must exist and have COACH role.");
        }

        // Vérifier que l'équipe existe
        if (!isValidTeam(session.getTeamId())) {
            throw new IllegalArgumentException("Invalid team ID.");
        }

        // Vérifier que le plan d'entraînement existe
        if (!isValidTrainingPlan(session.getTrainingPlanId())) {
            throw new IllegalArgumentException("Invalid training plan ID.");
        }

        // Vérifier la disponibilité du coach
        if (!isCoachAvailable(session.getCoachId(), session.getSessionDate(), session.getDuration(), null)) {
            throw new IllegalArgumentException("Coach is not available at this time.");
        }

        // Vérifier que la durée est positive
        if (session.getDuration() <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }

        String sql = "INSERT INTO coaching_session (coach_id, team_id, session_date, duration, notes, training_plan_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, session.getCoachId());
            ps.setInt(2, session.getTeamId());
            ps.setTimestamp(3, Timestamp.valueOf(session.getSessionDate()));
            ps.setInt(4, session.getDuration());
            ps.setString(5, session.getNotes());
            ps.setInt(6, session.getTrainingPlanId());

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                session.setId(keys.getInt(1));
            }
            System.out.println("✅ Coaching session created with ID: " + session.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createCoachingSession failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateCoachingSession(CoachingSession session) {
        // Vérifier la disponibilité du coach (en excluant la session actuelle)
        if (!isCoachAvailable(session.getCoachId(), session.getSessionDate(), session.getDuration(), session.getId())) {
            throw new IllegalArgumentException("Coach is not available at this time.");
        }

        String sql = "UPDATE coaching_session SET coach_id=?, team_id=?, session_date=?, duration=?, notes=?, training_plan_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, session.getCoachId());
            ps.setInt(2, session.getTeamId());
            ps.setTimestamp(3, Timestamp.valueOf(session.getSessionDate()));
            ps.setInt(4, session.getDuration());
            ps.setString(5, session.getNotes());
            ps.setInt(6, session.getTrainingPlanId());
            ps.setInt(7, session.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No coaching session found with ID: " + session.getId());
            System.out.println("✅ Coaching session updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateCoachingSession failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCoachingSession(int id) {
        String sql = "DELETE FROM coaching_session WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No coaching session found with ID: " + id);
            System.out.println("✅ Coaching session deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteCoachingSession failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<CoachingSession> getCoachingSessionById(int id) {
        String sql = "SELECT * FROM coaching_session WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getCoachingSessionById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<CoachingSession> getAllCoachingSessions() {
        List<CoachingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM coaching_session ORDER BY session_date DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllCoachingSessions failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<CoachingSession> getSessionsByCoach(int coachId) {
        List<CoachingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM coaching_session WHERE coach_id = ? ORDER BY session_date DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getSessionsByCoach failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<CoachingSession> getSessionsByTeam(int teamId) {
        List<CoachingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM coaching_session WHERE team_id = ? ORDER BY session_date DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getSessionsByTeam failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<CoachingSession> getSessionsByTrainingPlan(int trainingPlanId) {
        List<CoachingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM coaching_session WHERE training_plan_id = ? ORDER BY session_date DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, trainingPlanId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getSessionsByTrainingPlan failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<CoachingSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<CoachingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM coaching_session WHERE session_date BETWEEN ? AND ? ORDER BY session_date";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getSessionsByDateRange failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<CoachingSession> getUpcomingSessions() {
        List<CoachingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM coaching_session WHERE session_date >= NOW() ORDER BY session_date";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getUpcomingSessions failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<CoachingSession> getPastSessions() {
        List<CoachingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM coaching_session WHERE session_date < NOW() ORDER BY session_date DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPastSessions failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean isCoachAvailable(int coachId, LocalDateTime sessionDate, int duration, Integer excludeSessionId) {
        LocalDateTime sessionEnd = sessionDate.plusMinutes(duration);

        String sql = "SELECT COUNT(*) FROM coaching_session WHERE coach_id = ? AND id != ? " +
                "AND ((session_date BETWEEN ? AND ?) OR " +
                "(DATE_ADD(session_date, INTERVAL duration MINUTE) BETWEEN ? AND ?) OR " +
                "(session_date <= ? AND DATE_ADD(session_date, INTERVAL duration MINUTE) >= ?))";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ps.setInt(2, excludeSessionId != null ? excludeSessionId : -1);
            ps.setTimestamp(3, Timestamp.valueOf(sessionDate));
            ps.setTimestamp(4, Timestamp.valueOf(sessionEnd));
            ps.setTimestamp(5, Timestamp.valueOf(sessionDate));
            ps.setTimestamp(6, Timestamp.valueOf(sessionEnd));
            ps.setTimestamp(7, Timestamp.valueOf(sessionDate));
            ps.setTimestamp(8, Timestamp.valueOf(sessionEnd));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    private boolean isValidCoach(int coachId) {
        String sql = "SELECT COUNT(*) FROM user WHERE id = ? AND role = 'COACH' AND is_active = 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isValidTeam(int teamId) {
        String sql = "SELECT COUNT(*) FROM team WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isValidTrainingPlan(int trainingPlanId) {
        String sql = "SELECT COUNT(*) FROM training_plan WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, trainingPlanId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}