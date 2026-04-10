package services.team;

import Iservices.team.ITeamService;
import entities.team.Team;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TeamService implements ITeamService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private Team mapRow(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setGame(rs.getString("game"));

        Timestamp ts = rs.getTimestamp("creation_date");
        if (ts != null) t.setCreationDate(ts.toLocalDateTime());

        t.setCoachId(rs.getInt("coach_id"));

        // Récupérer le nom du coach si disponible
        try {
            String coachSql = "SELECT username FROM user WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(coachSql)) {
                ps.setInt(1, t.getCoachId());
                ResultSet rsCoach = ps.executeQuery();
                if (rsCoach.next()) {
                    t.setCoachName(rsCoach.getString("username"));
                }
            }
        } catch (SQLException e) {
            t.setCoachName("Unknown");
        }
        return t;
    }

    @Override
    public void createTeam(Team team) {
        if (teamNameExists(team.getName())) {
            throw new IllegalArgumentException("Team name already exists: " + team.getName());
        }

        String sql = "INSERT INTO team (name, game, creation_date, coach_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, team.getName());
            ps.setString(2, team.getGame());
            ps.setTimestamp(3, Timestamp.valueOf(team.getCreationDate() != null ?
                    team.getCreationDate() : LocalDateTime.now()));
            ps.setInt(4, team.getCoachId());

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                team.setId(keys.getInt(1));
            }
            System.out.println("✅ Team created with ID: " + team.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createTeam failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateTeam(Team team) {
        String sql = "UPDATE team SET name=?, game=?, coach_id=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, team.getName());
            ps.setString(2, team.getGame());
            ps.setInt(3, team.getCoachId());
            ps.setInt(4, team.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No team found with ID: " + team.getId());
            System.out.println("✅ Team updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateTeam failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTeam(int id) {
        // Vérifier si l'équipe est utilisée dans des matchs
        String checkSql = "SELECT COUNT(*) FROM matchy WHERE team1_id = ? OR team2_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(checkSql)) {
            ps.setInt(1, id);
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new RuntimeException("Cannot delete team: it is used in matches");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking team usage: " + e.getMessage(), e);
        }

        String sql = "DELETE FROM team WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No team found with ID: " + id);
            System.out.println("✅ Team deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteTeam failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Team> getTeamById(int id) {
        String sql = "SELECT * FROM team WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getTeamById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Team> getAllTeams() {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT * FROM team ORDER BY id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllTeams failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> getTeamsByGame(String game) {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT * FROM team WHERE LOWER(game) = LOWER(?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, game);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getTeamsByGame failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Team> getTeamsByCoach(int coachId) {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT * FROM team WHERE coach_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, coachId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getTeamsByCoach failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean teamNameExists(String name) {
        String sql = "SELECT COUNT(*) FROM team WHERE name = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Team> getAvailableTeams() {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT * FROM team ORDER BY name";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAvailableTeams failed: " + e.getMessage(), e);
        }
        return list;
    }
}