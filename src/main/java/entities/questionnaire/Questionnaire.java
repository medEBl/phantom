package entities.questionnaire;

public class Questionnaire {
    private int id;
    private Integer idAgent;
    private String game;
    private String ques1;
    private String ques2;
    private String ques3;
    private String ques4;

    public Questionnaire() {}

    public Questionnaire(Integer idAgent, String game, String ques1, String ques2, String ques3, String ques4) {
        this.idAgent = idAgent;
        this.game = game;
        this.ques1 = ques1;
        this.ques2 = ques2;
        this.ques3 = ques3;
        this.ques4 = ques4;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getIdAgent() { return idAgent; }
    public void setIdAgent(Integer idAgent) { this.idAgent = idAgent; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getQues1() { return ques1; }
    public void setQues1(String ques1) { this.ques1 = ques1; }
    public String getQues2() { return ques2; }
    public void setQues2(String ques2) { this.ques2 = ques2; }
    public String getQues3() { return ques3; }
    public void setQues3(String ques3) { this.ques3 = ques3; }
    public String getQues4() { return ques4; }
    public void setQues4(String ques4) { this.ques4 = ques4; }

    @Override
    public String toString() {
        return "Questionnaire{id=" + id + ", game='" + game + "'}";
    }
}