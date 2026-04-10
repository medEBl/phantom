package services.agent;

import Iservices.agent.IAgentService;
import entities.agent.Agent;
import tools.Phantom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgentService implements IAgentService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private Agent mapRow(ResultSet rs) throws SQLException {
        Agent a = new Agent();
        a.setId(rs.getInt("id"));
        a.setPseudo(rs.getString("pseudo"));
        a.setIdPlayer(rs.getInt("id_player"));
        a.setIdTeam((Integer) rs.getObject("id_team"));
        a.setGame(rs.getString("game"));
        a.setDateOfCreation(rs.getTimestamp("date_of_creation"));
        a.setRank(rs.getString("rank"));
        a.setStatus(rs.getString("status"));
        a.setSocialsLink(rs.getString("socials_link"));
        return a;
    }

    @Override
    public void createAgent(Agent agent) {
        String sql = "INSERT INTO agent (pseudo, id_player, id_team, game, date_of_creation, rank, status, socials_link) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, agent.getPseudo());
            ps.setInt(2, agent.getIdPlayer());
            if (agent.getIdTeam() != null) ps.setInt(3, agent.getIdTeam());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, agent.getGame());
            ps.setTimestamp(5, agent.getDateOfCreation() != null ? agent.getDateOfCreation() : new Timestamp(System.currentTimeMillis()));
            ps.setString(6, agent.getRank());
            ps.setString(7, agent.getStatus());
            ps.setString(8, agent.getSocialsLink());

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) agent.setId(keys.getInt(1));
            System.out.println("✅ Agent created with ID: " + agent.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createAgent failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateAgent(Agent agent) {
        String sql = "UPDATE agent SET pseudo=?, rank=?, status=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, agent.getPseudo());
            ps.setString(2, agent.getRank());
            ps.setString(3, agent.getStatus());
            ps.setInt(4, agent.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No agent found with ID: " + agent.getId());
            System.out.println("✅ Agent updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateAgent failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAgent(int id) {
        String sql = "DELETE FROM agent WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No agent found with ID: " + id);
            System.out.println("✅ Agent deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteAgent failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Agent> getAgentById(int id) {
        String sql = "SELECT * FROM agent WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAgentById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Agent> getAllAgents() {
        List<Agent> list = new ArrayList<>();
        String sql = "SELECT * FROM agent ORDER BY id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllAgents failed: " + e.getMessage(), e);
        }
        return list;
    }
}