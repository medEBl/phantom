package Controllers.team;

import entities.team.Team;
import services.team.TeamService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TeamController {
    private final TeamService teamService = new TeamService();
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createTeam();
                case "2" -> listAllTeams();
                case "3" -> getTeamById();
                case "4" -> updateTeam();
                case "5" -> deleteTeam();
                case "6" -> listTeamsByGame();
                case "7" -> listTeamsByCoach();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
        System.out.println("👋 Back to main menu!");
    }

    private void printMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║       TEAM MANAGEMENT        ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Create Team              ║");
        System.out.println("║  2. List All Teams           ║");
        System.out.println("║  3. Get Team by ID           ║");
        System.out.println("║  4. Update Team              ║");
        System.out.println("║  5. Delete Team              ║");
        System.out.println("║  6. List Teams by Game       ║");
        System.out.println("║  7. List Teams by Coach      ║");
        System.out.println("║  0. Back to Main Menu        ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createTeam() {
        System.out.println("\n── Create New Team ──");
        try {
            System.out.print("Team Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Team name is required");

            System.out.print("Game (VALORANT, LOL, CS:GO, ROCKET LEAGUE, etc.): ");
            String game = scanner.nextLine().trim().toUpperCase();
            if (game.isEmpty()) throw new IllegalArgumentException("Game is required");

            System.out.print("Coach ID (from user table with role COACH): ");
            int coachId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Creation Date (YYYY-MM-DD HH:MM) [Press Enter for now]: ");
            String dateStr = scanner.nextLine().trim();
            LocalDateTime creationDate = dateStr.isEmpty() ? LocalDateTime.now() :
                    LocalDateTime.parse(dateStr, dateFormatter);

            Team team = new Team(name, game, creationDate, coachId);
            teamService.createTeam(team);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid coach ID format.");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD HH:MM");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllTeams() {
        System.out.println("\n── All Teams ──");
        List<Team> teams = teamService.getAllTeams();
        if (teams.isEmpty()) {
            System.out.println("No teams found.");
            return;
        }
        printTeamTable(teams);
    }

    private void getTeamById() {
        System.out.print("\nEnter Team ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            teamService.getTeamById(id).ifPresentOrElse(
                    this::printTeamDetail,
                    () -> System.out.println("❌ Team not found.")
            );
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        }
    }

    private void updateTeam() {
        System.out.print("\nEnter Team ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<Team> opt = teamService.getTeamById(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Team not found.");
                return;
            }

            Team team = opt.get();

            System.out.print("New Team Name [" + team.getName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isBlank()) team.setName(name);

            System.out.print("New Game [" + team.getGame() + "]: ");
            String game = scanner.nextLine().trim();
            if (!game.isBlank()) team.setGame(game.toUpperCase());

            System.out.print("New Coach ID [" + team.getCoachId() + "]: ");
            String coachStr = scanner.nextLine().trim();
            if (!coachStr.isBlank()) team.setCoachId(Integer.parseInt(coachStr));

            teamService.updateTeam(team);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteTeam() {
        System.out.print("\nEnter Team ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Cancelled.");
                return;
            }
            teamService.deleteTeam(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listTeamsByGame() {
        System.out.print("\nEnter Game name: ");
        String game = scanner.nextLine().trim();
        List<Team> teams = teamService.getTeamsByGame(game);
        if (teams.isEmpty()) {
            System.out.println("No teams found for game: " + game);
            return;
        }
        printTeamTable(teams);
    }

    private void listTeamsByCoach() {
        System.out.print("\nEnter Coach ID: ");
        try {
            int coachId = Integer.parseInt(scanner.nextLine().trim());
            List<Team> teams = teamService.getTeamsByCoach(coachId);
            if (teams.isEmpty()) {
                System.out.println("No teams found for coach ID: " + coachId);
                return;
            }
            printTeamTable(teams);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid coach ID.");
        }
    }

    private void printTeamTable(List<Team> teams) {
        System.out.printf("%-5s %-20s %-15s %-20s %-10s%n", "ID", "Name", "Game", "Creation Date", "Coach");
        System.out.println("-".repeat(75));
        for (Team t : teams) {
            System.out.printf("%-5d %-20s %-15s %-20s %-10s%n",
                    t.getId(),
                    truncate(t.getName(), 20),
                    truncate(t.getGame(), 15),
                    t.getCreationDate() != null ? t.getCreationDate().toString().substring(0, 16) : "N/A",
                    t.getCoachName() != null ? truncate(t.getCoachName(), 10) : String.valueOf(t.getCoachId()));
        }
    }

    private void printTeamDetail(Team t) {
        System.out.println("\n── Team Detail ──────────────────────");
        System.out.println("ID          : " + t.getId());
        System.out.println("Name        : " + t.getName());
        System.out.println("Game        : " + t.getGame());
        System.out.println("Creation Date: " + t.getCreationDate());
        System.out.println("Coach ID    : " + t.getCoachId());
        System.out.println("Coach Name  : " + (t.getCoachName() != null ? t.getCoachName() : "N/A"));
        System.out.println("─────────────────────────────────────");
    }

    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}