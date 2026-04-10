package services.trainingplan;

import Iservices.trainingplan.ITrainingPlanService;
import entities.trainingplan.TrainingPlan;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainingPlanService implements ITrainingPlanService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private TrainingPlan mapRow(ResultSet rs) throws SQLException {
        TrainingPlan tp = new TrainingPlan();
        tp.setId(rs.getInt("id"));
        tp.setTitle(rs.getString("title"));
        tp.setDescription(rs.getString("description"));
        tp.setFocusArea(rs.getString("focus_area"));
        tp.setDifficultyLevel(rs.getString("difficulty_level"));
        tp.setCoachId(rs.getInt("coach_id"));
        tp.setTeamId(rs.getInt("team_id"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) tp.setCreatedAt(ts.toLocalDateTime());

        // Récupérer le nom du coach
        try {
            String coachSql = "SELECT username FROM user WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(coachSql)) {
                ps.setInt(1, tp.getCoachId());
                ResultSet rsCoach = ps.executeQuery();
                if (rsCoach.next()) {
                    tp.setCoachName(rsCoach.getString("username"));
                }
            }
        } catch (SQLException e) {
            tp.setCoachName("Unknown");
        }

        // Récupérer le nom de l'équipe
        try {
            String teamSql = "SELECT name FROM team WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(teamSql)) {
                ps.setInt(1, tp.getTeamId());
                ResultSet rsTeam = ps.executeQuery();
                if (rsTeam.next()) {
                    tp.setTeamName(rsTeam.getString("name"));
                }
            }
        } catch (SQLException e) {
            tp.setTeamName("Unknown");
        }

        return tp;
    }

    @Override
    public void createTrainingPlan(TrainingPlan plan) {
        if (titleExists(plan.getTitle())) {
            throw new IllegalArgumentException("Training plan title already exists: " + plan.getTitle());
        }

        // Vérifier que le coach existe et a le rôle COACH
        if (!isValidCoach(plan.getCoachId())) {
            throw new IllegalArgumentException("Invalid coach ID. User must exist and have COACH role.");
        }

        // Vérifier que l'équipe existe
        if (!isValidTeam(plan.getTeamId())) {
            throw new IllegalArgumentException("Invalid team ID.");
        }

        String sql = "INSERT INTO training_plan (title, description, focus_area, difficulty_level, coach_id, team_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, plan.getTitle());
            ps.setString(2, plan.getDescription());
            ps.setString(3, plan.getFocusArea());
            ps.setString(4, plan.getDifficultyLevel());
            ps.setInt(5, plan.getCoachId());
            ps.setInt(6, plan.getTeamId());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                plan.setId(keys.getInt(1));
            }
            System.out.println("✅ Training plan created with ID: " + plan.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createTrainingPlan failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateTrainingPlan(TrainingPlan plan) {
        String sql = "UPDATE training_plan SET title=?, description=?, focus_area=?, difficulty_level=?, coach_id=?, team_id=? WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, plan.getTitle());
            ps.setString(2, plan.getDescription());
            ps.setString(3, plan.getFocusArea());
            ps.setString(4, plan.getDifficultyLevel());
            ps.setInt(5, plan.getCoachId());
            ps.setInt(6, plan.getTeamId());
            ps.setInt(7, plan.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No training plan found with ID: " + plan.getId());
            System.out.println("✅ Training plan updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateTrainingPlan failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTrainingPlan(int id) {
        // Vérifier si le plan est utilisé dans des sessions
        String checkSql = "SELECT COUNT(*) FROM coaching_session WHERE training_plan_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(checkSql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new RuntimeException("Cannot delete training plan: it is used in coaching sessions");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking plan usage: " + e.getMessage(), e);
        }

        String sql = "DELETE FROM training_plan WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No training plan found with ID: " + id);
            System.out.println("✅ Training plan deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteTrainingPlan failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<TrainingPlan> getTrainingPlanById(int id) {
        String sql = "SELECT * FROM training_plan WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getTrainingPlanById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<TrainingPlan> getAllTrainingPlans() {
        List<TrainingPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM training_plan ORDER BY created_at DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllTrainingPlans failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingPlan> getPlansByCoach(int coachId) {
        List<TrainingPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM training_plan WHERE coach_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPlansByCoach failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingPlan> getPlansByTeam(int teamId) {
        List<TrainingPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM training_plan WHERE team_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPlansByTeam failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingPlan> getPlansByDifficulty(String difficultyLevel) {
        List<TrainingPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM training_plan WHERE LOWER(difficulty_level) = LOWER(?) ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, difficultyLevel);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPlansByDifficulty failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingPlan> getPlansByFocusArea(String focusArea) {
        List<TrainingPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM training_plan WHERE LOWER(focus_area) = LOWER(?) ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, focusArea);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPlansByFocusArea failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TrainingPlan> getPlansByTeamAndCoach(int teamId, int coachId) {
        List<TrainingPlan> list = new ArrayList<>();
        String sql = "SELECT * FROM training_plan WHERE team_id = ? AND coach_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, coachId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPlansByTeamAndCoach failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean titleExists(String title) {
        String sql = "SELECT COUNT(*) FROM training_plan WHERE title = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
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
}