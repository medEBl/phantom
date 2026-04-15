package Controllers.agent;

import entities.agent.Agent;
import services.agent.AgentService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

public class AgentController {
    private final AgentService agentService = new AgentService();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createAgent();
                case "2" -> listAllAgents();
                case "3" -> updateAgent();
                case "4" -> deleteAgent();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║       AGENT MANAGEMENT       ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Create Agent             ║");
        System.out.println("║  2. List All Agents          ║");
        System.out.println("║  3. Update Agent             ║");
        System.out.println("║  4. Delete Agent             ║");
        System.out.println("║  0. Back to Main Menu        ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createAgent() {
        try {
            System.out.print("Pseudo: ");
            String pseudo = scanner.nextLine().trim();
            System.out.print("Player ID: ");
            int idPlayer = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Team ID (or empty): ");
            String teamStr = scanner.nextLine().trim();
            Integer idTeam = teamStr.isEmpty() ? null : Integer.parseInt(teamStr);
            System.out.print("Game: ");
            String game = scanner.nextLine().trim();
            System.out.print("Rank: ");
            String rank = scanner.nextLine().trim();
            System.out.print("Status: ");
            String status = scanner.nextLine().trim();
            System.out.print("Socials Link: ");
            String socials = scanner.nextLine().trim();

            Agent a = new Agent(pseudo, idPlayer, idTeam, game, new Timestamp(System.currentTimeMillis()), rank, status, socials);
            agentService.createAgent(a);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllAgents() {
        List<Agent> agents = agentService.getAllAgents();
        agents.forEach(System.out::println);
    }

    private void updateAgent() {
        try {
            System.out.print("Enter Agent ID to update: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            Agent agent = agentService.getAgentById(id).orElseThrow(() -> new RuntimeException("Agent not found"));

            System.out.print("New Pseudo [" + agent.getPseudo() + "]: ");
            String pseudo = scanner.nextLine().trim();
            if (!pseudo.isBlank()) agent.setPseudo(pseudo);

            System.out.print("New Rank [" + agent.getRank() + "]: ");
            String rank = scanner.nextLine().trim();
            if (!rank.isBlank()) agent.setRank(rank);

            System.out.print("New Status [" + agent.getStatus() + "]: ");
            String status = scanner.nextLine().trim();
            if (!status.isBlank()) agent.setStatus(status);

            agentService.updateAgent(agent);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteAgent() {
        try {
            System.out.print("Enter Agent ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            agentService.deleteAgent(id);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}