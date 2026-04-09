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
}