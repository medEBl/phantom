package entities.agent;

import java.sql.Timestamp;

public class Agent {
    private int id;
    private String pseudo;
    private int idPlayer;
    private Integer idTeam;
    private String game;
    private Timestamp dateOfCreation;
    private String rank;
    private String status;
    private String socialsLink;

    public Agent() {}

    public Agent(String pseudo, int idPlayer, Integer idTeam, String game, Timestamp dateOfCreation, String rank, String status, String socialsLink) {
        this.pseudo = pseudo;
        this.idPlayer = idPlayer;
        this.idTeam = idTeam;
        this.game = game;
        this.dateOfCreation = dateOfCreation;
        this.rank = rank;
        this.status = status;
        this.socialsLink = socialsLink;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getPseudo() { return pseudo; }
    public void setPseudo(String pseudo) { this.pseudo = pseudo; }
    public int getIdPlayer() { return idPlayer; }
    public void setIdPlayer(int idPlayer) { this.idPlayer = idPlayer; }
    public Integer getIdTeam() { return idTeam; }
    public void setIdTeam(Integer idTeam) { this.idTeam = idTeam; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public Timestamp getDateOfCreation() { return dateOfCreation; }
    public void setDateOfCreation(Timestamp dateOfCreation) { this.dateOfCreation = dateOfCreation; }
    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSocialsLink() { return socialsLink; }
    public void setSocialsLink(String socialsLink) { this.socialsLink = socialsLink; }

    @Override
    public String toString() {
        return "Agent{" + "id=" + id + ", pseudo='" + pseudo + '\'' + ", game='" + game + '\'' + '}';
    }
}