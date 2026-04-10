package Controllers.matchy;

import entities.matchy.Matchy;
import services.matchy.MatchyService;
import services.team.TeamService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class MatchyController {
    private final MatchyService matchyService = new MatchyService();
    private final TeamService teamService = new TeamService();
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createMatch();
                case "2" -> listAllMatches();
                case "3" -> getMatchById();
                case "4" -> updateMatch();
                case "5" -> deleteMatch();
                case "6" -> listMatchesByStatus();
                case "7" -> listMatchesByGame();
                case "8" -> listMatchesByTeam();
                case "9" -> updateMatchResult();
                case "10" -> cancelMatch();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
        System.out.println("👋 Back to main menu!");
    }

    private void printMenu() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║       MATCH MANAGEMENT           ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  1. Create Match                 ║");
        System.out.println("║  2. List All Matches             ║");
        System.out.println("║  3. Get Match by ID              ║");
        System.out.println("║  4. Update Match                 ║");
        System.out.println("║  5. Delete Match                 ║");
        System.out.println("║  6. List Matches by Status       ║");
        System.out.println("║  7. List Matches by Game         ║");
        System.out.println("║  8. List Matches by Team         ║");
        System.out.println("║  9. Update Match Result          ║");
        System.out.println("║ 10. Cancel Match                 ║");
        System.out.println("║  0. Back to Main Menu            ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createMatch() {
        System.out.println("\n── Create New Match ──");
        try {
            System.out.print("Game: ");
            String game = scanner.nextLine().trim().toUpperCase();
            if (game.isEmpty()) throw new IllegalArgumentException("Game is required");

            System.out.print("Match Date (YYYY-MM-DD HH:MM): ");
            LocalDateTime matchDate = LocalDateTime.parse(scanner.nextLine().trim(), dateFormatter);

            System.out.print("Status (planned/ongoing/finished): ");
            String status = scanner.nextLine().trim().toLowerCase();
            if (!status.matches("planned|ongoing|finished")) {
                throw new IllegalArgumentException("Status must be: planned, ongoing, or finished");
            }

            System.out.print("Team 1 ID (optional, press Enter to skip): ");
            String team1Str = scanner.nextLine().trim();
            Integer team1Id = team1Str.isEmpty() ? null : Integer.parseInt(team1Str);

            System.out.print("Team 2 ID (optional, press Enter to skip): ");
            String team2Str = scanner.nextLine().trim();
            Integer team2Id = team2Str.isEmpty() ? null : Integer.parseInt(team2Str);

            System.out.print("Location (optional): ");
            String location = scanner.nextLine().trim();
            if (location.isEmpty()) location = null;

            System.out.print("Latitude (optional): ");
            String latStr = scanner.nextLine().trim();
            Double latitude = latStr.isEmpty() ? null : Double.parseDouble(latStr);

            System.out.print("Longitude (optional): ");
            String lonStr = scanner.nextLine().trim();
            Double longitude = lonStr.isEmpty() ? null : Double.parseDouble(lonStr);

            Matchy match = new Matchy(game, matchDate, status, team1Id, team2Id, location, latitude, longitude);

            if ("finished".equals(status)) {
                System.out.print("Score Team 1: ");
                match.setScoreTeam1(Integer.parseInt(scanner.nextLine().trim()));
                System.out.print("Score Team 2: ");
                match.setScoreTeam2(Integer.parseInt(scanner.nextLine().trim()));

                if (match.getScoreTeam1() > match.getScoreTeam2()) {
                    match.setWinnerTeamId(team1Id);
                } else if (match.getScoreTeam2() > match.getScoreTeam1()) {
                    match.setWinnerTeamId(team2Id);
                }
            }

            matchyService.createMatch(match);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD HH:MM");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllMatches() {
        System.out.println("\n── All Matches ──");
        List<Matchy> matches = matchyService.getAllMatches();
        if (matches.isEmpty()) {
            System.out.println("No matches found.");
            return;
        }
        printMatchTable(matches);
    }

    private void getMatchById() {
        System.out.print("\nEnter Match ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            matchyService.getMatchById(id).ifPresentOrElse(
                    this::printMatchDetail,
                    () -> System.out.println("❌ Match not found.")
            );
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        }
    }

    private void updateMatch() {
        System.out.print("\nEnter Match ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<Matchy> opt = matchyService.getMatchById(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Match not found.");
                return;
            }

            Matchy match = opt.get();

            System.out.print("New Game [" + match.getGame() + "]: ");
            String game = scanner.nextLine().trim();
            if (!game.isBlank()) match.setGame(game.toUpperCase());

            System.out.print("New Match Date [" + match.getMatchDate() + "]: ");
            String dateStr = scanner.nextLine().trim();
            if (!dateStr.isBlank()) match.setMatchDate(LocalDateTime.parse(dateStr, dateFormatter));

            System.out.print("New Status [" + match.getStatus() + "]: ");
            String status = scanner.nextLine().trim();
            if (!status.isBlank()) match.setStatus(status.toLowerCase());

            System.out.print("New Team 1 ID [" + match.getTeam1Id() + "]: ");
            String team1Str = scanner.nextLine().trim();
            if (!team1Str.isBlank()) match.setTeam1Id(Integer.parseInt(team1Str));

            System.out.print("New Team 2 ID [" + match.getTeam2Id() + "]: ");
            String team2Str = scanner.nextLine().trim();
            if (!team2Str.isBlank()) match.setTeam2Id(Integer.parseInt(team2Str));

            System.out.print("New Location [" + match.getLocation() + "]: ");
            String location = scanner.nextLine().trim();
            if (!location.isBlank()) match.setLocation(location);

            matchyService.updateMatch(match);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format.");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteMatch() {
        System.out.print("\nEnter Match ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Cancelled.");
                return;
            }
            matchyService.deleteMatch(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listMatchesByStatus() {
        System.out.print("\nStatus (planned/ongoing/finished): ");
        String status = scanner.nextLine().trim().toLowerCase();
        List<Matchy> matches = matchyService.getMatchesByStatus(status);
        if (matches.isEmpty()) {
            System.out.println("No matches found with status: " + status);
            return;
        }
        printMatchTable(matches);
    }

    private void listMatchesByGame() {
        System.out.print("\nGame name: ");
        String game = scanner.nextLine().trim();
        List<Matchy> matches = matchyService.getMatchesByGame(game);
        if (matches.isEmpty()) {
            System.out.println("No matches found for game: " + game);
            return;
        }
        printMatchTable(matches);
    }

    private void listMatchesByTeam() {
        System.out.print("\nTeam ID: ");
        try {
            int teamId = Integer.parseInt(scanner.nextLine().trim());
            List<Matchy> matches = matchyService.getMatchesByTeam(teamId);
            if (matches.isEmpty()) {
                System.out.println("No matches found for team ID: " + teamId);
                return;
            }
            printMatchTable(matches);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid team ID.");
        }
    }

    private void updateMatchResult() {
        System.out.print("\nEnter Match ID: ");
        try {
            int matchId = Integer.parseInt(scanner.nextLine().trim());
            Optional<Matchy> opt = matchyService.getMatchById(matchId);
            if (opt.isEmpty()) {
                System.out.println("❌ Match not found.");
                return;
            }

            Matchy match = opt.get();
            System.out.println("Current: " + match.getTeam1Name() + " vs " + match.getTeam2Name());

            System.out.print("Score " + match.getTeam1Name() + ": ");
            int score1 = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Score " + match.getTeam2Name() + ": ");
            int score2 = Integer.parseInt(scanner.nextLine().trim());

            int winnerId;
            if (score1 > score2) {
                winnerId = match.getTeam1Id();
                System.out.println("Winner: " + match.getTeam1Name());
            } else if (score2 > score1) {
                winnerId = match.getTeam2Id();
                System.out.println("Winner: " + match.getTeam2Name());
            } else {
                System.out.println("It's a draw! No winner.");
                winnerId = 0;
            }

            matchyService.updateMatchResult(matchId, score1, score2, winnerId);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void cancelMatch() {
        System.out.print("\nEnter Match ID to cancel: ");
        try {
            int matchId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm cancellation? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Cancelled.");
                return;
            }
            matchyService.cancelMatch(matchId);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        }
    }

    private void printMatchTable(List<Matchy> matches) {
        System.out.printf("%-5s %-12s %-19s %-12s %-10s %-15s %-15s %-10s%n",
                "ID", "Game", "Date", "Status", "Score", "Team 1", "Team 2", "Winner");
        System.out.println("-".repeat(110));
        for (Matchy m : matches) {
            String score = (m.getScoreTeam1() != null && m.getScoreTeam2() != null) ?
                    m.getScoreTeam1() + " - " + m.getScoreTeam2() : "N/A";
            System.out.printf("%-5d %-12s %-19s %-12s %-10s %-15s %-15s %-10s%n",
                    m.getId(),
                    truncate(m.getGame(), 12),
                    m.getMatchDate() != null ? m.getMatchDate().toString().substring(0, 16) : "N/A",
                    m.getStatus(),
                    score,
                    truncate(m.getTeam1Name() != null ? m.getTeam1Name() : "TBD", 15),
                    truncate(m.getTeam2Name() != null ? m.getTeam2Name() : "TBD", 15),
                    truncate(m.getWinnerTeamName() != null ? m.getWinnerTeamName() : "N/A", 10));
        }
    }

    private void printMatchDetail(Matchy m) {
        System.out.println("\n── Match Detail ──────────────────────");
        System.out.println("ID          : " + m.getId());
        System.out.println("Game        : " + m.getGame());
        System.out.println("Match Date  : " + m.getMatchDate());
        System.out.println("Status      : " + m.getStatus());
        System.out.println("Team 1      : " + (m.getTeam1Name() != null ? m.getTeam1Name() : "TBD") + " (ID: " + m.getTeam1Id() + ")");
        System.out.println("Team 2      : " + (m.getTeam2Name() != null ? m.getTeam2Name() : "TBD") + " (ID: " + m.getTeam2Id() + ")");
        System.out.println("Score       : " + (m.getScoreTeam1() != null ? m.getScoreTeam1() : "?") + " - " +
                (m.getScoreTeam2() != null ? m.getScoreTeam2() : "?"));
        System.out.println("Winner      : " + (m.getWinnerTeamName() != null ? m.getWinnerTeamName() : "N/A"));
        System.out.println("Location    : " + (m.getLocation() != null ? m.getLocation() : "N/A"));
        if (m.getLatitude() != null && m.getLongitude() != null) {
            System.out.println("Coordinates : " + m.getLatitude() + ", " + m.getLongitude());
        }
        System.out.println("─────────────────────────────────────");
    }

    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}