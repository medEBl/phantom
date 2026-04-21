package services.reponse;

import Iservices.reponse.IReponseService;
import entities.reponse.Reponse;
import tools.Phantom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReponseService implements IReponseService {
    private final Connection cnx = Phantom.getInstance().getCnx();

    private Reponse mapRow(ResultSet rs) throws SQLException {
        Reponse r = new Reponse();
        r.setId(rs.getInt("id"));
        r.setIdAgent(rs.getInt("id_agent"));
        r.setQuestionnaireId(rs.getInt("questionnaire_id"));
        r.setRep1(rs.getString("rep1"));
        r.setRep2(rs.getString("rep2"));
        r.setRep3(rs.getString("rep3"));
        r.setRep4(rs.getString("rep4"));
        return r;
    }

    @Override
    public void createReponse(Reponse r) {
        String sql = "INSERT INTO reponse_questionnaire (id_agent, questionnaire_id, rep1, rep2, rep3, rep4) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getIdAgent());
            ps.setInt(2, r.getQuestionnaireId());
            ps.setString(3, r.getRep1());
            ps.setString(4, r.getRep2());
            ps.setString(5, r.getRep3());
            ps.setString(6, r.getRep4());

            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setId(keys.getInt(1));
            System.out.println("✅ Answers submitted successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("createReponse failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reponse> getAllReponses() {
        List<Reponse> list = new ArrayList<>();
        String sql = "SELECT * FROM reponse_questionnaire";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllReponses failed: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public Optional<Reponse> getReponseById(int id) {
        String sql = "SELECT * FROM reponse_questionnaire WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getReponseById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteReponse(int id) {
        String sql = "DELETE FROM reponse_questionnaire WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Reponse deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteReponse failed: " + e.getMessage(), e);
        }
    }
    @Override
    public void updateReponse(Reponse r) {
        String sql = "UPDATE reponse_questionnaire SET rep1=?, rep2=?, rep3=?, rep4=? WHERE id_agent=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, r.getRep1());
            ps.setString(2, r.getRep2());
            ps.setString(3, r.getRep3());
            ps.setString(4, r.getRep4());
            ps.setInt(5, r.getIdAgent());

            ps.executeUpdate();
            System.out.println("✅ Answers updated successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("updateReponse failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Reponse> getReponseByAgentId(int idAgent) {
        String sql = "SELECT * FROM reponse_questionnaire WHERE id_agent = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idAgent);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getReponseByAgentId failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public java.util.Map<String, Object> getQuestionnaireByGame(String game) {
        java.util.Map<String, Object> questions = new java.util.HashMap<>();
        String sql = "SELECT id, ques1, ques2, ques3, ques4 FROM questionnaire_agent WHERE game = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, game);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                questions.put("id", rs.getInt("id"));
                questions.put("ques1", rs.getString("ques1"));
                questions.put("ques2", rs.getString("ques2"));
                questions.put("ques3", rs.getString("ques3"));
                questions.put("ques4", rs.getString("ques4"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("getQuestionnaireByGame failed: " + e.getMessage(), e);
        }
        return questions.isEmpty() ? null : questions;
    }
}