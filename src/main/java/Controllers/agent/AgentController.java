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
            System.out.print("Player ID: ");
            int idPlayer = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Game: ");
            String game = scanner.nextLine().trim();
            if (game.isBlank()) {
                System.out.println("❌ Error: Game cannot be blank.");
                return;
            }

            // --- BUSINESS RULE: One Agent Per Game ---
            if (agentService.agentExistsForPlayerAndGame(idPlayer, game)) {
                System.out.println("❌ Error: Duplicate detected! Player " + idPlayer + " already has a profile for " + game + ".");
                return;
            }

            System.out.print("Pseudo: ");
            String pseudo = scanner.nextLine().trim();
            if (pseudo.length() < 3 || pseudo.length() > 50) {
                System.out.println("❌ Error: Pseudo must be between 3 and 50 characters.");
                return;
            }

            System.out.print("Team ID (or empty): ");
            String teamStr = scanner.nextLine().trim();
            Integer idTeam = teamStr.isEmpty() ? null : Integer.parseInt(teamStr);

            System.out.print("Rank: ");
            String rank = scanner.nextLine().trim();
            if (rank.isBlank()) {
                System.out.println("❌ Error: Rank cannot be blank.");
                return;
            }
            try {
                if (Double.parseDouble(rank) < 0) {
                    System.out.println("❌ Error: Rank cannot be negative.");
                    return;
                }
            } catch (NumberFormatException e) { /* Valid text rank */ }

            System.out.print("Status: ");
            String status = scanner.nextLine().trim();
            if (status.isBlank()) {
                System.out.println("❌ Error: Status cannot be blank.");
                return;
            }

            System.out.print("Socials Link: ");
            String socials = scanner.nextLine().trim();
            String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
            if (socials.isBlank() || !socials.matches(urlRegex)) {
                System.out.println("❌ Error: Socials Link must be a valid URL.");
                return;
            }

            // If it passes all checks, create the entity
            Agent a = new Agent(pseudo, idPlayer, idTeam, game, new Timestamp(System.currentTimeMillis()), rank, status, socials);
            agentService.createAgent(a);

        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter a valid number for IDs.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllAgents() {
        List<Agent> agents = agentService.getAllAgents();
        if (agents.isEmpty()) {
            System.out.println("📂 No agents found.");
        } else {
            agents.forEach(System.out::println);
        }
    }

    private void updateAgent() {
        try {
            System.out.print("Enter Agent ID to update: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            Agent agent = agentService.getAgentById(id).orElseThrow(() -> new RuntimeException("Agent not found"));

            System.out.print("New Pseudo [" + agent.getPseudo() + "]: ");
            String pseudo = scanner.nextLine().trim();
            if (!pseudo.isBlank()) {
                if (pseudo.length() < 3 || pseudo.length() > 50) {
                    System.out.println("❌ Error: Pseudo must be between 3 and 50 characters.");
                    return;
                }
                agent.setPseudo(pseudo);
            }

            System.out.print("New Rank [" + agent.getRank() + "]: ");
            String rank = scanner.nextLine().trim();
            if (!rank.isBlank()) {
                try {
                    if (Double.parseDouble(rank) < 0) {
                        System.out.println("❌ Error: Rank cannot be negative.");
                        return;
                    }
                } catch (NumberFormatException e) { /* Valid text rank */ }
                agent.setRank(rank);
            }

            System.out.print("New Status [" + agent.getStatus() + "]: ");
            String status = scanner.nextLine().trim();
            if (!status.isBlank()) agent.setStatus(status);

            // Added Socials to the update menu so you can test URL validation
            System.out.print("New Socials Link [" + agent.getSocialsLink() + "]: ");
            String socials = scanner.nextLine().trim();
            if (!socials.isBlank()) {
                String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
                if (!socials.matches(urlRegex)) {
                    System.out.println("❌ Error: Socials Link must be a valid URL.");
                    return;
                }
                agent.setSocialsLink(socials);
            }

            agentService.updateAgent(agent);

        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter a valid number for ID.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteAgent() {
        try {
            System.out.print("Enter Agent ID to delete: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            agentService.deleteAgent(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ Error: Please enter a valid number for ID.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}