package Iservices.agent;

import entities.agent.Agent;

import java.util.List;
import java.util.Optional;

public interface IAgentService {
    void createAgent(Agent agent);
    void updateAgent(Agent agent);
    void deleteAgent(int id);
    Optional<Agent> getAgentById(int id);
    List<Agent> getAllAgents();
    // ADD THIS LINE BELOW
    boolean agentExistsForPlayerAndGame(int idPlayer, String game);
}