package services.questionnaire;

import Iservices.questionnaire.IQuestionnaireService;
import entities.questionnaire.Questionnaire;
import tools.Phantom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuestionnaireService implements IQuestionnaireService {

    private Questionnaire mapRow(ResultSet rs) throws SQLException {
        Questionnaire q = new Questionnaire();
        q.setId(rs.getInt("id"));
        q.setIdAgent((Integer) rs.getObject("id_agent"));
        q.setGame(rs.getString("game"));
        q.setQues1(rs.getString("ques1"));
        q.setQues2(rs.getString("ques2"));
        q.setQues3(rs.getString("ques3"));
        q.setQues4(rs.getString("ques4"));
        return q;
    }

    @Override
    public void createQuestionnaire(Questionnaire q) {
        String sql = "INSERT INTO questionnaire_agent (id_agent, game, ques1, ques2, ques3, ques4) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection cnx = Phantom.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (q.getIdAgent() != null) ps.setInt(1, q.getIdAgent());
            else ps.setNull(1, Types.INTEGER);

            ps.setString(2, q.getGame());
            ps.setString(3, q.getQues1());
            ps.setString(4, q.getQues2());
            ps.setString(5, q.getQues3());
            ps.setString(6, q.getQues4());

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) q.setId(keys.getInt(1));

        } catch (SQLException e) {
            throw new RuntimeException("createQuestionnaire failed", e);
        }
    }

    @Override
    public List<Questionnaire> getAllQuestionnaires() {
        List<Questionnaire> list = new ArrayList<>();
        String sql = "SELECT * FROM questionnaire_agent WHERE id_agent IS NULL";

        try (Connection cnx = Phantom.getInstance().getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("getAllQuestionnaires failed", e);
        }

        return list;
    }

    @Override
    public Optional<Questionnaire> getQuestionnaireById(int id) {
        String sql = "SELECT * FROM questionnaire_agent WHERE id = ?";

        try (Connection cnx = Phantom.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("getQuestionnaireById failed", e);
        }

        return Optional.empty();
    }

    @Override
    public void deleteQuestionnaire(int id) {
        String sql = "DELETE FROM questionnaire_agent WHERE id = ?";

        try (Connection cnx = Phantom.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("deleteQuestionnaire failed", e);
        }
    }

    @Override
    public int getTotalAgentsCount() {
        String sql = "SELECT COUNT(*) FROM agent";

        try (Connection cnx = Phantom.getInstance().getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int getFilledQuestionnairesCount() {
        String sql = "SELECT COUNT(DISTINCT id_agent) FROM reponse_questionnaire";

        try (Connection cnx = Phantom.getInstance().getCnx();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}