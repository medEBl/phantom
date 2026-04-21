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

    private void checkConnection() {
        if (cnx == null) {
            throw new RuntimeException("❌ Pas de connexion à la base de données.");
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
        m.setTeam2Id(rs.getInt("team2_id"));

        m.setWinnerTeamId(rs.getInt("winner_team_id"));
        if (rs.wasNull()) m.setWinnerTeamId(null);

        m.setLocation(rs.getString("location"));
        m.setLatitude(rs.getDouble("latitude"));
        if (rs.wasNull()) m.setLatitude(null);

        m.setLongitude(rs.getDouble("longitude"));
        if (rs.wasNull()) m.setLongitude(null);

        // Récupérer les noms des équipes
        String teamSql = "SELECT name FROM team WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(teamSql)) {
            ps.setInt(1, m.getTeam1Id());
            ResultSet rsTeam = ps.executeQuery();
            if (rsTeam.next()) m.setTeam1Name(rsTeam.getString("name"));
        }

        try (PreparedStatement ps = cnx.prepareStatement(teamSql)) {
            ps.setInt(1, m.getTeam2Id());
            ResultSet rsTeam = ps.executeQuery();
            if (rsTeam.next()) m.setTeam2Name(rsTeam.getString("name"));
        }

        if (m.getWinnerTeamId() != null && m.getWinnerTeamId() > 0) {
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
        checkConnection();

        // Validation: Les deux équipes sont obligatoires
        if (match.getTeam1Id() <= 0 || match.getTeam2Id() <= 0) {
            throw new IllegalArgumentException("❌ Les deux équipes sont obligatoires.");
        }

        // Validation: Les deux équipes doivent être différentes
        if (match.getTeam1Id() == match.getTeam2Id()) {
            throw new IllegalArgumentException("❌ Team1 et Team2 ne peuvent pas être la même équipe.");
        }

        // Validation: Les équipes doivent exister dans la base
        if (!isValidTeam(match.getTeam1Id())) {
            throw new IllegalArgumentException("❌ Team ID " + match.getTeam1Id() + " n'existe pas.");
        }
        if (!isValidTeam(match.getTeam2Id())) {
            throw new IllegalArgumentException("❌ Team ID " + match.getTeam2Id() + " n'existe pas.");
        }

        // Validation: Date du match doit être dans le futur pour les matchs planifiés
        if (("planned".equals(match.getStatus()) || "ongoing".equals(match.getStatus())) &&
                match.getMatchDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("❌ La date du match doit être dans le futur pour les matchs planifiés ou en cours.");
        }

        // Validation: Si status est finished, les scores doivent être présents
        if ("finished".equals(match.getStatus())) {
            if (match.getScoreTeam1() == null || match.getScoreTeam2() == null) {
                throw new IllegalArgumentException("❌ Les scores sont obligatoires pour un match terminé.");
            }
            if (match.getScoreTeam1() < 0 || match.getScoreTeam2() < 0) {
                throw new IllegalArgumentException("❌ Les scores ne peuvent pas être négatifs.");
            }

            // Détermination automatique du gagnant
            if (match.getScoreTeam1() > match.getScoreTeam2()) {
                match.setWinnerTeamId(match.getTeam1Id());
            } else if (match.getScoreTeam2() > match.getScoreTeam1()) {
                match.setWinnerTeamId(match.getTeam2Id());
            } else {
                match.setWinnerTeamId(null); // Égalité, pas de gagnant
            }
        } else {
            // Pour les matchs non terminés, les scores doivent être null
            match.setScoreTeam1(null);
            match.setScoreTeam2(null);
            match.setWinnerTeamId(null);
        }

        String sql = "INSERT INTO matchy (game, match_date, score_team1, score_team2, status, " +
                "team1_id, team2_id, winner_team_id, location, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, match.getGame().toUpperCase());
            ps.setTimestamp(2, Timestamp.valueOf(match.getMatchDate()));
            ps.setObject(3, match.getScoreTeam1(), Types.INTEGER);
            ps.setObject(4, match.getScoreTeam2(), Types.INTEGER);
            ps.setString(5, match.getStatus());
            ps.setInt(6, match.getTeam1Id());
            ps.setInt(7, match.getTeam2Id());
            ps.setObject(8, match.getWinnerTeamId(), Types.INTEGER);
            ps.setString(9, match.getLocation());
            ps.setObject(10, match.getLatitude(), Types.DOUBLE);
            ps.setObject(11, match.getLongitude(), Types.DOUBLE);

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                match.setId(keys.getInt(1));
            }
            System.out.println("✅ Match créé avec ID: " + match.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createMatch failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMatch(Matchy match) {
        checkConnection();

        // Validation: Les deux équipes sont obligatoires
        if (match.getTeam1Id() <= 0 || match.getTeam2Id() <= 0) {
            throw new IllegalArgumentException("❌ Les deux équipes sont obligatoires.");
        }

        // Validation: Les deux équipes doivent être différentes
        if (match.getTeam1Id() == match.getTeam2Id()) {
            throw new IllegalArgumentException("❌ Team1 et Team2 ne peuvent pas être la même équipe.");
        }

        // Si le match devient terminé
        if ("finished".equals(match.getStatus())) {
            Optional<Matchy> existing = getMatchById(match.getId());
            if (existing.isPresent() && !"finished".equals(existing.get().getStatus())) {
                if (match.getScoreTeam1() == null || match.getScoreTeam2() == null) {
                    throw new IllegalArgumentException("❌ Les scores sont obligatoires pour terminer un match.");
                }
                if (match.getScoreTeam1() < 0 || match.getScoreTeam2() < 0) {
                    throw new IllegalArgumentException("❌ Les scores ne peuvent pas être négatifs.");
                }

                // Détermination automatique du gagnant
                if (match.getScoreTeam1() > match.getScoreTeam2()) {
                    match.setWinnerTeamId(match.getTeam1Id());
                } else if (match.getScoreTeam2() > match.getScoreTeam1()) {
                    match.setWinnerTeamId(match.getTeam2Id());
                } else {
                    match.setWinnerTeamId(null); // Égalité
                }
            }
        } else {
            // Si le match n'est pas terminé, les scores doivent être null
            match.setScoreTeam1(null);
            match.setScoreTeam2(null);
            match.setWinnerTeamId(null);
        }

        String sql = "UPDATE matchy SET game=?, match_date=?, score_team1=?, score_team2=?, " +
                "status=?, team1_id=?, team2_id=?, winner_team_id=?, location=?, latitude=?, longitude=? " +
                "WHERE id=?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, match.getGame().toUpperCase());
            ps.setTimestamp(2, Timestamp.valueOf(match.getMatchDate()));
            ps.setObject(3, match.getScoreTeam1(), Types.INTEGER);
            ps.setObject(4, match.getScoreTeam2(), Types.INTEGER);
            ps.setString(5, match.getStatus());
            ps.setInt(6, match.getTeam1Id());
            ps.setInt(7, match.getTeam2Id());
            ps.setObject(8, match.getWinnerTeamId(), Types.INTEGER);
            ps.setString(9, match.getLocation());
            ps.setObject(10, match.getLatitude(), Types.DOUBLE);
            ps.setObject(11, match.getLongitude(), Types.DOUBLE);
            ps.setInt(12, match.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("❌ Match non trouvé avec ID: " + match.getId());
            System.out.println("✅ Match mis à jour.");
        } catch (SQLException e) {
            throw new RuntimeException("updateMatch failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateMatchResult(int matchId, int scoreTeam1, int scoreTeam2) {
        checkConnection();

        Optional<Matchy> opt = getMatchById(matchId);
        if (opt.isEmpty()) {
            throw new RuntimeException("❌ Match non trouvé.");
        }

        Matchy match = opt.get();

        if (scoreTeam1 < 0 || scoreTeam2 < 0) {
            throw new IllegalArgumentException("❌ Les scores ne peuvent pas être négatifs.");
        }

        Integer winnerId = null;
        if (scoreTeam1 > scoreTeam2) {
            winnerId = match.getTeam1Id();
        } else if (scoreTeam2 > scoreTeam1) {
            winnerId = match.getTeam2Id();
        } // Si égalité, winnerId reste null

        String sql = "UPDATE matchy SET score_team1=?, score_team2=?, winner_team_id=?, status='finished' WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, scoreTeam1);
            ps.setInt(2, scoreTeam2);
            ps.setObject(3, winnerId, Types.INTEGER);
            ps.setInt(4, matchId);
            ps.executeUpdate();
            System.out.println("✅ Résultat du match mis à jour.");
            if (winnerId == null) {
                System.out.println("   Match nul ! Pas de gagnant.");
            } else {
                System.out.println("   Gagnant: Équipe ID " + winnerId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("updateMatchResult failed: " + e.getMessage(), e);
        }
    }

    // ... (les autres méthodes restent identiques)

    @Override
    public void deleteMatch(int id) {
        checkConnection();
        String sql = "DELETE FROM matchy WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("❌ Match non trouvé avec ID: " + id);
            System.out.println("✅ Match supprimé.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteMatch failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Matchy> getMatchById(int id) {
        checkConnection();
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
        checkConnection();
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

    // ... (autres méthodes de filtrage similaires)

    @Override
    public void cancelMatch(int matchId) {
        checkConnection();
        String sql = "DELETE FROM matchy WHERE id=? AND status != 'finished'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, matchId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Match annulé.");
            } else {
                System.out.println("❌ Impossible d'annuler: Match non trouvé ou déjà terminé.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("cancelMatch failed: " + e.getMessage(), e);
        }
    }

    // Méthodes de filtrage (à compléter)
    @Override
    public List<Matchy> getMatchesByGame(String game) {
        checkConnection();
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
        checkConnection();
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
        checkConnection();
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
        checkConnection();
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
        checkConnection();
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
        checkConnection();
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
}