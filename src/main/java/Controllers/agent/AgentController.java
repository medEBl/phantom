package Controllers.agent;

import entities.agent.Agent;
import services.agent.AgentService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;
import java.util.Optional;

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
            int idPlayer = 0;
            while (true) {
                System.out.print("Player ID (Mandatory): ");
                String input = scanner.nextLine().trim();
                if (input.isBlank()) {
                    System.out.println("❌ Error: Player ID cannot be blank.");
                    continue;
                }
                try {
                    idPlayer = Integer.parseInt(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Player ID must be a valid number.");
                }
            }

            String game = "";
            while (true) {
                System.out.print("Game (Mandatory): ");
                game = scanner.nextLine().trim();
                if (game.isBlank()) {
                    System.out.println("❌ Error: Game cannot be blank.");
                } else {
                    break;
                }
            }

            if (agentService.agentExistsForPlayerAndGame(idPlayer, game)) {
                System.out.println("❌ Error: Duplicate detected! Player " + idPlayer + " already has a profile for " + game + ".");
                return;
            }

            String pseudo = "";
            while (true) {
                System.out.print("Pseudo (3-50 chars): ");
                pseudo = scanner.nextLine().trim();
                if (pseudo.length() < 3 || pseudo.length() > 50) {
                    System.out.println("❌ Error: Pseudo must be between 3 and 50 characters.");
                } else {
                    break;
                }
            }

            Integer idTeam = null;
            while (true) {
                System.out.print("Team ID (Optional, press Enter to skip): ");
                String teamStr = scanner.nextLine().trim();
                if (teamStr.isEmpty()) break;
                try {
                    idTeam = Integer.parseInt(teamStr);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Team ID must be a valid number or left blank.");
                }
            }

            String rank = "";
            while (true) {
                System.out.print("Rank (Mandatory, positive number): ");
                rank = scanner.nextLine().trim();
                if (rank.isBlank() || !rank.matches("^\\d+$")) {
                    System.out.println("❌ Error: Rank must be a valid positive number.");
                } else {
                    break;
                }
            }

            String status = "";
            while (true) {
                System.out.print("Status (Mandatory): ");
                status = scanner.nextLine().trim();
                if (status.isBlank()) {
                    System.out.println("❌ Error: Status cannot be blank.");
                } else {
                    break;
                }
            }

            String socials = "";
            String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
            while (true) {
                System.out.print("Socials Link (Mandatory URL): ");
                socials = scanner.nextLine().trim();
                if (socials.isBlank() || !socials.matches(urlRegex)) {
                    System.out.println("❌ Error: Socials Link must be a valid URL (e.g., https://twitter.com/...).");
                } else {
                    break;
                }
            }

            Agent a = new Agent(pseudo, idPlayer, idTeam, game, new Timestamp(System.currentTimeMillis()), rank, status, socials);
            agentService.createAgent(a);
            System.out.println("✅ Success: Agent created successfully!");

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
            int id = 0;
            while (true) {
                System.out.print("Enter Agent ID to update: ");
                try {
                    id = Integer.parseInt(scanner.nextLine().trim());
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Agent ID must be a valid number.");
                }
            }

            Optional<Agent> optAgent = agentService.getAgentById(id);
            if (optAgent.isEmpty()) {
                System.out.println("❌ Error: Agent with ID " + id + " not found.");
                return;
            }
            Agent agent = optAgent.get();

            while (true) {
                System.out.print("New Pseudo [" + agent.getPseudo() + "] (Press Enter to keep): ");
                String pseudo = scanner.nextLine().trim();
                if (pseudo.isBlank()) break;
                if (pseudo.length() < 3 || pseudo.length() > 50) {
                    System.out.println("❌ Error: Pseudo must be between 3 and 50 characters.");
                } else {
                    agent.setPseudo(pseudo);
                    break;
                }
            }

            while (true) {
                System.out.print("New Rank [" + agent.getRank() + "] (Press Enter to keep): ");
                String rank = scanner.nextLine().trim();
                if (rank.isBlank()) break;
                if (!rank.matches("^\\d+$")) {
                    System.out.println("❌ Error: Rank must be a positive number.");
                } else {
                    agent.setRank(rank);
                    break;
                }
            }

            System.out.print("New Status [" + agent.getStatus() + "] (Press Enter to keep): ");
            String status = scanner.nextLine().trim();
            if (!status.isBlank()) agent.setStatus(status);

            String urlRegex = "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)$";
            while (true) {
                System.out.print("New Socials Link [" + agent.getSocialsLink() + "] (Press Enter to keep): ");
                String socials = scanner.nextLine().trim();
                if (socials.isBlank()) break;
                if (!socials.matches(urlRegex)) {
                    System.out.println("❌ Error: Socials Link must be a valid URL.");
                } else {
                    agent.setSocialsLink(socials);
                    break;
                }
            }

            agentService.updateAgent(agent);
            System.out.println("✅ Success: Agent updated successfully!");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteAgent() {
        try {
            int id = 0;
            while (true) {
                System.out.print("Enter Agent ID to delete: ");
                try {
                    id = Integer.parseInt(scanner.nextLine().trim());
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Agent ID must be a valid number.");
                }
            }
            agentService.deleteAgent(id);
            System.out.println("✅ Success: Agent deleted (if it existed).");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}