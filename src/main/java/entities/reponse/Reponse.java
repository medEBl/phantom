package entities.reponse;

public class Reponse {
    private int id;
    private int idAgent;
    private int questionnaireId;
    private String rep1;
    private String rep2;
    private String rep3;
    private String rep4;

    public Reponse() {}

    public Reponse(int idAgent, int questionnaireId, String rep1, String rep2, String rep3, String rep4) {
        this.idAgent = idAgent;
        this.questionnaireId = questionnaireId;
        this.rep1 = rep1;
        this.rep2 = rep2;
        this.rep3 = rep3;
        this.rep4 = rep4;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdAgent() { return idAgent; }
    public void setIdAgent(int idAgent) { this.idAgent = idAgent; }
    public int getQuestionnaireId() { return questionnaireId; }
    public void setQuestionnaireId(int questionnaireId) { this.questionnaireId = questionnaireId; }
    public String getRep1() { return rep1; }
    public void setRep1(String rep1) { this.rep1 = rep1; }
    public String getRep2() { return rep2; }
    public void setRep2(String rep2) { this.rep2 = rep2; }
    public String getRep3() { return rep3; }
    public void setRep3(String rep3) { this.rep3 = rep3; }
    public String getRep4() { return rep4; }
    public void setRep4(String rep4) { this.rep4 = rep4; }

    @Override
    public String toString() {
        return "Reponse{id=" + id + ", idAgent=" + idAgent + ", questionnaireId=" + questionnaireId + "}";
    }
}