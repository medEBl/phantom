package services.matchy;

import Iservices.matchy.IMatchyService;
import entities.matchy.Matchy;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchyService implements IMatchyService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private Matchy mapRow(ResultSet rs) throws SQLException {
        Matchy m = new Matchy();
        m.setId(rs.getInt("id"));
        m.setGame(rs.getString("game"));

        Timestamp ts = rs.getTimestamp("match_date");
        if (ts != null) m.setMatchDate(ts.toLocalDateTime());

        m.setScoreTeam1(rs.getInt("score_team1"));
        if (rs.wasNull()) m.setScoreTeam1(null);

        m.setScoreTeam2(rs.getInt("score_team2"));
        if (rs.wasNull()) m.setScoreTeam2(null);

        m.setStatus(rs.getString("status"));
        m.setTeam1Id(rs.getInt("team1_id"));
        if (rs.wasNull()) m.setTeam1Id(null);

        m.setTeam2Id(rs.getInt("team2_id"));
        if (rs.wasNull()) m.setTeam2Id(null);

        m.setWinnerTeamId(rs.getInt("winner_team_id"));
        if (rs.wasNull()) m.setWinnerTeamId(null);

        m.setLocation(rs.getString("location"));
        m.setLatitude(rs.getDouble("latitude"));
        if (rs.wasNull()) m.setLatitude(null);

        m.setLongitude(rs.getDouble("longitude"));
        if (rs.wasNull()) m.setLongitude(null);

        // Récupérer les noms des équipes
        if (m.getTeam1Id() != null) {
            String teamSql = "SELECT name FROM team WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(teamSql)) {
                ps.setInt(1, m.getTeam1Id());
                ResultSet rsTeam = ps.executeQuery();
                if (rsTeam.next()) m.setTeam1Name(rsTeam.getString("name"));
            }
        }

        if (m.getTeam2Id() != null) {
            String teamSql = "SELECT name FROM team WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(teamSql)) {
                ps.setInt(1, m.getTeam2Id());
                ResultSet rsTeam = ps.executeQuery();
                if (rsTeam.next()) m.setTeam2Name(rsTeam.getString("name"));
            }
        }

        if (m.getWinnerTeamId() != null && m.getWinnerTeamId() > 0) {
            String teamSql = "SELECT name FROM team WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(teamSql)) {
                ps.setInt(1, m.getWinnerTeamId());
                ResultSet rsTeam = ps.executeQuery();
                if (rsTeam.next()) m.setWinnerTeamName(rsTeam.getString("name"));
            }
        }

        return m;
    }

    @Override
    public void createMatch(Matchy match) {
        // Validation: les deux équipes doivent être différentes si les deux sont spécifiées
        if (match.getTeam1Id() != null && match.getTeam2Id() != null &&
                match.getTeam1Id().equals(match.getTeam2Id())) {
            throw new IllegalArgumentException("Team1 and Team2 cannot be the same team");
        }

        // Validation: si status est finished, les scores doivent être présents
        if ("finished".equals(match.getStatus()) &&
                (match.getScoreTeam1() == null || match.getScoreTeam2() == null)) {
            throw new IllegalArgumentException("Finished matches must have scores");
        }

        String sql = "INSERT INTO matchy (game, match_date, score_team1, score_team2, status, " +
                "team1_id, team2_id, winner_team_id, location, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, match.getGame());
            ps.setTimestamp(2, Timestamp.valueOf(match.getMatchDate()));
            ps.setObject(3, match.getScoreTeam1(), Types.INTEGER);
            ps.setObject(4, match.getScoreTeam2(), Types.INTEGER);
            ps.setString(5, match.getStatus());
            ps.setObject(6, match.getTeam1Id(), Types.INTEGER);
            ps.setObject(7, match.getTeam2Id(), Types.INTEGER);
            ps.setObject(8, match.getWinnerTeamId(), Types.INTEGER);
            ps.setString(9, match.getLocation());
            ps.setObject(10, match.getLatitude(), Types.DOUBLE);
            ps.setObject(11, match.getLongitude(), Types.DOUBLE);

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                match.setId(keys.getInt(1));
            }
            System.out.println("✅ Match created with ID: " + match.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createMatch failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMatch(Matchy match) {
        // Validation: si status devient finished, ajouter les scores
        if ("finished".equals(match.getStatus())) {
            Optional<Matchy> existing = getMatchById(match.getId());
            if (existing.isPresent() && !"finished".equals(existing.get().getStatus())) {
                if (match.getScoreTeam1() == null || match.getScoreTeam2() == null) {
                    throw new IllegalArgumentException("Scores required when finishing a match");
                }
                // Déterminer automatiquement le gagnant si non spécifié
                if (match.getWinnerTeamId() == null) {
                    if (match.getScoreTeam1() > match.getScoreTeam2()) {
                        match.setWinnerTeamId(match.getTeam1Id());
                    } else if (match.getScoreTeam2() > match.getScoreTeam1()) {
                        match.setWinnerTeamId(match.getTeam2Id());
                    }
                }
            }
        }

        String sql = "UPDATE matchy SET game=?, match_date=?, score_team1=?, score_team2=?, " +
                "status=?, team1_id=?, team2_id=?, winner_team_id=?, location=?, latitude=?, longitude=? " +
                "WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, match.getGame());
            ps.setTimestamp(2, Timestamp.valueOf(match.getMatchDate()));
            ps.setObject(3, match.getScoreTeam1(), Types.INTEGER);
            ps.setObject(4, match.getScoreTeam2(), Types.INTEGER);
            ps.setString(5, match.getStatus());
            ps.setObject(6, match.getTeam1Id(), Types.INTEGER);
            ps.setObject(7, match.getTeam2Id(), Types.INTEGER);
            ps.setObject(8, match.getWinnerTeamId(), Types.INTEGER);
            ps.setString(9, match.getLocation());
            ps.setObject(10, match.getLatitude(), Types.DOUBLE);
            ps.setObject(11, match.getLongitude(), Types.DOUBLE);
            ps.setInt(12, match.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No match found with ID: " + match.getId());
            System.out.println("✅ Match updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateMatch failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMatch(int id) {
        String sql = "DELETE FROM matchy WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No match found with ID: " + id);
            System.out.println("✅ Match deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteMatch failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Matchy> getMatchById(int id) {
        String sql = "SELECT * FROM matchy WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getMatchById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Matchy> getAllMatches() {
        List<Matchy> list = new ArrayList<>();
        String sql = "SELECT * FROM matchy ORDER BY match_date DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllMatches failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Matchy> getMatchesByGame(String game) {
        List<Matchy> list = new ArrayList<>();
        String sql = "SELECT * FROM matchy WHERE LOWER(game) = LOWER(?) ORDER BY match_date DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, game);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getMatchesByGame failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Matchy> getMatchesByStatus(String status) {
        List<Matchy> list = new ArrayList<>();
        String sql = "SELECT * FROM matchy WHERE status = ? ORDER BY match_date DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getMatchesByStatus failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Matchy> getMatchesByTeam(int teamId) {
        List<Matchy> list = new ArrayList<>();
        String sql = "SELECT * FROM matchy WHERE team1_id = ? OR team2_id = ? ORDER BY match_date DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, teamId);
            ps.setInt(2, teamId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getMatchesByTeam failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Matchy> getMatchesByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Matchy> list = new ArrayList<>();
        String sql = "SELECT * FROM matchy WHERE match_date BETWEEN ? AND ? ORDER BY match_date";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getMatchesByDateRange failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Matchy> getUpcomingMatches() {
        List<Matchy> list = new ArrayList<>();
        String sql = "SELECT * FROM matchy WHERE status IN ('planned', 'ongoing') AND match_date >= NOW() ORDER BY match_date";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getUpcomingMatches failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Matchy> getFinishedMatches() {
        List<Matchy> list = new ArrayList<>();
        String sql = "SELECT * FROM matchy WHERE status = 'finished' ORDER BY match_date DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getFinishedMatches failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void updateMatchResult(int matchId, int scoreTeam1, int scoreTeam2, int winnerTeamId) {
        String sql = "UPDATE matchy SET score_team1=?, score_team2=?, winner_team_id=?, status='finished' WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, scoreTeam1);
            ps.setInt(2, scoreTeam2);
            ps.setInt(3, winnerTeamId);
            ps.setInt(4, matchId);
            ps.executeUpdate();
            System.out.println("✅ Match result updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateMatchResult failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelMatch(int matchId) {
        String sql = "DELETE FROM matchy WHERE id=? AND status != 'finished'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Match cancelled.");
            } else {
                System.out.println("❌ Cannot cancel: Match not found or already finished.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("cancelMatch failed: " + e.getMessage(), e);
        }
    }
}