package services.tournament;

import Iservices.tournament.ITournamentService;
import entities.tournament.Tournament;
import services.user.UserService;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentService implements ITournamentService {
    private final Connection cnx = Phantom.getInstance().getCnx();
    private final UserService userService = new UserService();
    private final EmailService emailService = new EmailService();

    private Tournament mapRow(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setGame(rs.getString("game"));

        Date startDate = rs.getDate("start_date");
        if (startDate != null) t.setStartDate(startDate.toLocalDate());

        Date endDate = rs.getDate("end_date");
        if (endDate != null) t.setEndDate(endDate.toLocalDate());

        t.setPhase(rs.getString("phase"));
        t.setOrganizerId(rs.getInt("organizer_id"));
        t.setActive(rs.getInt("is_active") == 1);
        t.setPosterPath(rs.getString("poster_path"));
        t.setPosterPrompt(rs.getString("poster_prompt"));
        t.setMaxTeams(rs.getInt("max_teams"));
        return t;
    }

    @Override
    public void createTournament(Tournament tournament) {
        if (tournamentNameExists(tournament.getName())) {
            throw new IllegalArgumentException("Tournament name already exists: " + tournament.getName());
        }
        if (tournament.getStartDate() != null && tournament.getEndDate() != null
                && tournament.getEndDate().isBefore(tournament.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        String sql = "INSERT INTO tournament (name, game, start_date, end_date, phase, organizer_id, is_active, max_teams) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getGame());
            ps.setDate(3, Date.valueOf(tournament.getStartDate()));
            ps.setDate(4, Date.valueOf(tournament.getEndDate()));
            ps.setString(5, tournament.getPhase());
            ps.setInt(6, tournament.getOrganizerId());
            ps.setInt(7, tournament.isActive() ? 1 : 0);
            ps.setInt(8, tournament.getMaxTeams());

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                tournament.setId(keys.getInt(1));
            }
            System.out.println("✅ Tournament created with ID: " + tournament.getId());

            // Notify all players about the new tournament
            try {
                emailService.sendTournamentNotification(tournament, userService.getUsersByRole("PLAYER"));
            } catch (Exception e) {
                System.err.println("⚠️ Could not initiate email notification: " + e.getMessage());
            }

        } catch (SQLException e) {
            throw new RuntimeException("createTournament failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateTournament(Tournament tournament) {
        if (tournament.getStartDate() != null && tournament.getEndDate() != null
                && tournament.getEndDate().isBefore(tournament.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        String sql = "UPDATE tournament SET name=?, game=?, start_date=?, end_date=?, phase=?, organizer_id=?, is_active=?, max_teams=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, tournament.getName());
            ps.setString(2, tournament.getGame());
            ps.setDate(3, Date.valueOf(tournament.getStartDate()));
            ps.setDate(4, Date.valueOf(tournament.getEndDate()));
            ps.setString(5, tournament.getPhase());
            ps.setInt(6, tournament.getOrganizerId());
            ps.setInt(7, tournament.isActive() ? 1 : 0);
            ps.setInt(8, tournament.getMaxTeams());
            ps.setInt(9, tournament.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No tournament found with ID: " + tournament.getId());
            System.out.println("✅ Tournament updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateTournament failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteTournament(int id) {
        String sql = "DELETE FROM tournament WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No tournament found with ID: " + id);
            System.out.println("✅ Tournament deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteTournament failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Tournament> getTournamentById(int id) {
        String sql = "SELECT * FROM tournament WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getTournamentById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Tournament> getAllTournaments() {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament ORDER BY id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllTournaments failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Tournament> getTournamentsByGame(String game) {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament WHERE LOWER(game) = LOWER(?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, game);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getTournamentsByGame failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Tournament> getTournamentsByPhase(String phase) {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament WHERE phase = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, phase);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getTournamentsByPhase failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Tournament> getActiveTournaments() {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament WHERE is_active = 1 ORDER BY start_date";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getActiveTournaments failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Tournament> searchTournaments(String query, String game, String phase) {
        List<Tournament> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM tournament WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(game) LIKE ?)");
            String pattern = "%" + query.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (game != null && !game.trim().isEmpty() && !"All".equalsIgnoreCase(game)) {
            sql.append(" AND game = ?");
            params.add(game);
        }

        if (phase != null && !phase.trim().isEmpty() && !"All".equalsIgnoreCase(phase)) {
            sql.append(" AND phase = ?");
            params.add(phase);
        }

        sql.append(" ORDER BY start_date DESC");

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("searchTournaments failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean tournamentNameExists(String name) {
        String sql = "SELECT COUNT(*) FROM tournament WHERE name = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
