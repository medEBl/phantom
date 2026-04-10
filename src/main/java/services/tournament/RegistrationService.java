package services.tournament;

import Iservices.tournament.IRegistrationService;
import entities.tournament.Registration;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegistrationService implements IRegistrationService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private Registration mapRow(ResultSet rs) throws SQLException {
        Registration r = new Registration();
        r.setId(rs.getInt("id"));
        r.setTeamName(rs.getString("team_name"));
        r.setContactEmail(rs.getString("contact_email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        r.setTournamentId(rs.getInt("tournament_id"));
        return r;
    }

    @Override
    public void createRegistration(Registration registration) {
        String sql = "INSERT INTO registration (team_name, contact_email, created_at, tournament_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, registration.getTeamName());
            ps.setString(2, registration.getContactEmail());
            ps.setTimestamp(3, Timestamp.valueOf(
                    registration.getCreatedAt() != null ? registration.getCreatedAt() : LocalDateTime.now()));
            ps.setInt(4, registration.getTournamentId());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) registration.setId(keys.getInt(1));
            System.out.println("✅ Registration created with ID: " + registration.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createRegistration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRegistration(int id) {
        String sql = "DELETE FROM registration WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No registration found with ID: " + id);
            System.out.println("✅ Registration deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteRegistration failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Registration> getRegistrationById(int id) {
        String sql = "SELECT * FROM registration WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getRegistrationById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Registration> getAllRegistrations() {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registration ORDER BY id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllRegistrations failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Registration> getRegistrationsByTournament(int tournamentId) {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registration WHERE tournament_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getRegistrationsByTournament failed: " + e.getMessage(), e);
        }
        return list;
    }
}
