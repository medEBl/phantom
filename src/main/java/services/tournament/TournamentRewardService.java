package services.tournament;

import Iservices.tournament.ITournamentRewardService;
import entities.tournament.TournamentReward;
import tools.Phantom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TournamentRewardService implements ITournamentRewardService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private TournamentReward mapRow(ResultSet rs) throws SQLException {
        TournamentReward r = new TournamentReward();
        r.setId(rs.getInt("id"));
        r.setTournamentId(rs.getInt("tournament_id"));
        r.setRank(rs.getInt("rank"));
        r.setRewardType(rs.getString("reward_type"));
        r.setRewardValue(rs.getString("reward_value"));
        return r;
    }

    @Override
    public void createReward(TournamentReward reward) {
        String sql = "INSERT INTO tournament_reward (tournament_id, rank, reward_type, reward_value) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reward.getTournamentId());
            ps.setInt(2, reward.getRank());
            ps.setString(3, reward.getRewardType());
            ps.setString(4, reward.getRewardValue());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) reward.setId(keys.getInt(1));
            System.out.println("✅ Reward created with ID: " + reward.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createReward failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateReward(TournamentReward reward) {
        String sql = "UPDATE tournament_reward SET rank=?, reward_type=?, reward_value=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, reward.getRank());
            ps.setString(2, reward.getRewardType());
            ps.setString(3, reward.getRewardValue());
            ps.setInt(4, reward.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No reward found with ID: " + reward.getId());
            System.out.println("✅ Reward updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateReward failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteReward(int id) {
        String sql = "DELETE FROM tournament_reward WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No reward found with ID: " + id);
            System.out.println("✅ Reward deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteReward failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<TournamentReward> getRewardById(int id) {
        String sql = "SELECT * FROM tournament_reward WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getRewardById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<TournamentReward> getAllRewards() {
        List<TournamentReward> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament_reward ORDER BY id";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllRewards failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<TournamentReward> getRewardsByTournament(int tournamentId) {
        List<TournamentReward> list = new ArrayList<>();
        String sql = "SELECT * FROM tournament_reward WHERE tournament_id = ? ORDER BY rank";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, tournamentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getRewardsByTournament failed: " + e.getMessage(), e);
        }
        return list;
    }
}
