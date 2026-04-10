package Controllers.tournament;

import entities.tournament.Tournament;
import entities.tournament.Registration;
import entities.tournament.TournamentReward;
import services.tournament.TournamentService;
import services.tournament.RegistrationService;
import services.tournament.TournamentRewardService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TournamentController {
    private final TournamentService tournamentService = new TournamentService();
    private final RegistrationService registrationService = new RegistrationService();
    private final TournamentRewardService rewardService = new TournamentRewardService();
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> tournamentMenu();
                case "2" -> registrationMenu();
                case "3" -> rewardMenu();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
        System.out.println("👋 Back to main menu!");
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║      TOURNAMENT MANAGEMENT       ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  1. Tournaments                  ║");
        System.out.println("║  2. Registrations                ║");
        System.out.println("║  3. Rewards                      ║");
        System.out.println("║  0. Back to Main Menu            ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    // ==================== TOURNAMENT SUB-MENU ====================

    private void tournamentMenu() {
        boolean back = false;
        while (!back) {
            printTournamentMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createTournament();
                case "2" -> listAllTournaments();
                case "3" -> getTournamentById();
                case "4" -> updateTournament();
                case "5" -> deleteTournament();
                case "6" -> listTournamentsByGame();
                case "7" -> listTournamentsByPhase();
                case "8" -> listActiveTournaments();
                case "0" -> back = true;
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void printTournamentMenu() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║         TOURNAMENTS              ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  1. Create Tournament            ║");
        System.out.println("║  2. List All                     ║");
        System.out.println("║  3. Get by ID                    ║");
        System.out.println("║  4. Update Tournament            ║");
        System.out.println("║  5. Delete Tournament            ║");
        System.out.println("║  6. Filter by Game               ║");
        System.out.println("║  7. Filter by Phase              ║");
        System.out.println("║  8. List Active                  ║");
        System.out.println("║  0. Back                         ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createTournament() {
        System.out.println("\n── Create New Tournament ──");
        try {
            System.out.print("Tournament Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) throw new IllegalArgumentException("Tournament name is required.");

            System.out.print("Game: ");
            String game = scanner.nextLine().trim();
            if (game.isEmpty()) throw new IllegalArgumentException("Game is required.");

            System.out.print("Start Date (YYYY-MM-DD): ");
            LocalDate startDate = LocalDate.parse(scanner.nextLine().trim(), dateFormatter);

            System.out.print("End Date (YYYY-MM-DD): ");
            LocalDate endDate = LocalDate.parse(scanner.nextLine().trim(), dateFormatter);

            System.out.print("Phase (registrations_open / ongoing / finished): ");
            String phase = scanner.nextLine().trim();
            if (phase.isEmpty()) phase = "registrations_open";

            System.out.print("Organizer ID: ");
            int organizerId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Max Teams: ");
            int maxTeams = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Active? (yes/no): ");
            boolean isActive = scanner.nextLine().trim().equalsIgnoreCase("yes");

            Tournament t = new Tournament(name, game, startDate, endDate, phase, organizerId, isActive, maxTeams);
            tournamentService.createTournament(t);
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD.");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllTournaments() {
        System.out.println("\n── All Tournaments ──");
        List<Tournament> list = tournamentService.getAllTournaments();
        if (list.isEmpty()) { System.out.println("No tournaments found."); return; }
        printTournamentTable(list);
    }

    private void getTournamentById() {
        System.out.print("\nEnter Tournament ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            tournamentService.getTournamentById(id).ifPresentOrElse(
                    this::printTournamentDetail,
                    () -> System.out.println("❌ Tournament not found.")
            );
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
    }

    private void updateTournament() {
        System.out.print("\nEnter Tournament ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<Tournament> opt = tournamentService.getTournamentById(id);
            if (opt.isEmpty()) { System.out.println("❌ Tournament not found."); return; }
            Tournament t = opt.get();

            System.out.print("New Name [" + t.getName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isBlank()) t.setName(name);

            System.out.print("New Game [" + t.getGame() + "]: ");
            String game = scanner.nextLine().trim();
            if (!game.isBlank()) t.setGame(game);

            System.out.print("New Start Date [" + t.getStartDate() + "] (YYYY-MM-DD): ");
            String startStr = scanner.nextLine().trim();
            if (!startStr.isBlank()) t.setStartDate(LocalDate.parse(startStr, dateFormatter));

            System.out.print("New End Date [" + t.getEndDate() + "] (YYYY-MM-DD): ");
            String endStr = scanner.nextLine().trim();
            if (!endStr.isBlank()) t.setEndDate(LocalDate.parse(endStr, dateFormatter));

            System.out.print("New Phase [" + t.getPhase() + "]: ");
            String phase = scanner.nextLine().trim();
            if (!phase.isBlank()) t.setPhase(phase);

            System.out.print("New Max Teams [" + t.getMaxTeams() + "]: ");
            String maxTeamsStr = scanner.nextLine().trim();
            if (!maxTeamsStr.isBlank()) t.setMaxTeams(Integer.parseInt(maxTeamsStr));

            System.out.print("Active? (yes/no) [" + (t.isActive() ? "yes" : "no") + "]: ");
            String activeStr = scanner.nextLine().trim();
            if (!activeStr.isBlank()) t.setActive(activeStr.equalsIgnoreCase("yes"));

            tournamentService.updateTournament(t);
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD.");
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteTournament() {
        System.out.print("\nEnter Tournament ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) { System.out.println("Cancelled."); return; }
            tournamentService.deleteTournament(id);
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
        catch (Exception e) { System.out.println("❌ Error: " + e.getMessage()); }
    }

    private void listTournamentsByGame() {
        System.out.print("\nEnter Game name: ");
        String game = scanner.nextLine().trim();
        List<Tournament> list = tournamentService.getTournamentsByGame(game);
        if (list.isEmpty()) { System.out.println("No tournaments found for game: " + game); return; }
        printTournamentTable(list);
    }

    private void listTournamentsByPhase() {
        System.out.println("\nPhases: registrations_open / ongoing / finished");
        System.out.print("Enter Phase: ");
        String phase = scanner.nextLine().trim();
        List<Tournament> list = tournamentService.getTournamentsByPhase(phase);
        if (list.isEmpty()) { System.out.println("No tournaments found for phase: " + phase); return; }
        printTournamentTable(list);
    }

    private void listActiveTournaments() {
        System.out.println("\n── Active Tournaments ──");
        List<Tournament> list = tournamentService.getActiveTournaments();
        if (list.isEmpty()) { System.out.println("No active tournaments found."); return; }
        printTournamentTable(list);
    }

    // ==================== REGISTRATION SUB-MENU ====================

    private void registrationMenu() {
        boolean back = false;
        while (!back) {
            printRegistrationMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createRegistration();
                case "2" -> listAllRegistrations();
                case "3" -> getRegistrationById();
                case "4" -> listRegistrationsByTournament();
                case "5" -> deleteRegistration();
                case "0" -> back = true;
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void printRegistrationMenu() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║         REGISTRATIONS            ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  1. Register a Team              ║");
        System.out.println("║  2. List All Registrations       ║");
        System.out.println("║  3. Get by ID                    ║");
        System.out.println("║  4. List by Tournament           ║");
        System.out.println("║  5. Delete Registration          ║");
        System.out.println("║  0. Back                         ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createRegistration() {
        System.out.println("\n── Register a Team ──");
        try {
            System.out.print("Team Name: ");
            String teamName = scanner.nextLine().trim();
            if (teamName.isEmpty()) throw new IllegalArgumentException("Team name is required.");

            System.out.print("Contact Email: ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) throw new IllegalArgumentException("Email is required.");

            System.out.print("Tournament ID: ");
            int tournamentId = Integer.parseInt(scanner.nextLine().trim());

            Registration reg = new Registration(teamName, email, LocalDateTime.now(), tournamentId);
            registrationService.createRegistration(reg);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllRegistrations() {
        System.out.println("\n── All Registrations ──");
        List<Registration> list = registrationService.getAllRegistrations();
        if (list.isEmpty()) { System.out.println("No registrations found."); return; }
        printRegistrationTable(list);
    }

    private void getRegistrationById() {
        System.out.print("\nEnter Registration ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            registrationService.getRegistrationById(id).ifPresentOrElse(
                    this::printRegistrationDetail,
                    () -> System.out.println("❌ Registration not found.")
            );
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
    }

    private void listRegistrationsByTournament() {
        System.out.print("\nEnter Tournament ID: ");
        try {
            int tournamentId = Integer.parseInt(scanner.nextLine().trim());
            List<Registration> list = registrationService.getRegistrationsByTournament(tournamentId);
            if (list.isEmpty()) { System.out.println("No registrations for tournament ID: " + tournamentId); return; }
            printRegistrationTable(list);
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
    }

    private void deleteRegistration() {
        System.out.print("\nEnter Registration ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) { System.out.println("Cancelled."); return; }
            registrationService.deleteRegistration(id);
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
        catch (Exception e) { System.out.println("❌ Error: " + e.getMessage()); }
    }

    // ==================== REWARD SUB-MENU ====================

    private void rewardMenu() {
        boolean back = false;
        while (!back) {
            printRewardMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createReward();
                case "2" -> listAllRewards();
                case "3" -> getRewardById();
                case "4" -> listRewardsByTournament();
                case "5" -> updateReward();
                case "6" -> deleteReward();
                case "0" -> back = true;
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void printRewardMenu() {
        System.out.println("\n╔══════════════════════════════════╗");
        System.out.println("║           REWARDS                ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  1. Add Reward                   ║");
        System.out.println("║  2. List All Rewards             ║");
        System.out.println("║  3. Get by ID                    ║");
        System.out.println("║  4. List by Tournament           ║");
        System.out.println("║  5. Update Reward                ║");
        System.out.println("║  6. Delete Reward                ║");
        System.out.println("║  0. Back                         ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createReward() {
        System.out.println("\n── Add Reward ──");
        try {
            System.out.print("Tournament ID: ");
            int tournamentId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Rank (1st, 2nd, etc.): ");
            int rank = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Reward Type (Cash / Skin / Trophy): ");
            String type = scanner.nextLine().trim();

            System.out.print("Reward Value (e.g. 5000 or skin name): ");
            String value = scanner.nextLine().trim();

            TournamentReward reward = new TournamentReward(tournamentId, rank, type, value);
            rewardService.createReward(reward);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllRewards() {
        System.out.println("\n── All Rewards ──");
        List<TournamentReward> list = rewardService.getAllRewards();
        if (list.isEmpty()) { System.out.println("No rewards found."); return; }
        printRewardTable(list);
    }

    private void getRewardById() {
        System.out.print("\nEnter Reward ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            rewardService.getRewardById(id).ifPresentOrElse(
                    this::printRewardDetail,
                    () -> System.out.println("❌ Reward not found.")
            );
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
    }

    private void listRewardsByTournament() {
        System.out.print("\nEnter Tournament ID: ");
        try {
            int tournamentId = Integer.parseInt(scanner.nextLine().trim());
            List<TournamentReward> list = rewardService.getRewardsByTournament(tournamentId);
            if (list.isEmpty()) { System.out.println("No rewards for tournament ID: " + tournamentId); return; }
            printRewardTable(list);
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
    }

    private void updateReward() {
        System.out.print("\nEnter Reward ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<TournamentReward> opt = rewardService.getRewardById(id);
            if (opt.isEmpty()) { System.out.println("❌ Reward not found."); return; }
            TournamentReward r = opt.get();

            System.out.print("New Rank [" + r.getRank() + "]: ");
            String rankStr = scanner.nextLine().trim();
            if (!rankStr.isBlank()) r.setRank(Integer.parseInt(rankStr));

            System.out.print("New Reward Type [" + r.getRewardType() + "]: ");
            String type = scanner.nextLine().trim();
            if (!type.isBlank()) r.setRewardType(type);

            System.out.print("New Reward Value [" + r.getRewardValue() + "]: ");
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) r.setRewardValue(value);

            rewardService.updateReward(r);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteReward() {
        System.out.print("\nEnter Reward ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) { System.out.println("Cancelled."); return; }
            rewardService.deleteReward(id);
        } catch (NumberFormatException e) { System.out.println("❌ Invalid ID."); }
        catch (Exception e) { System.out.println("❌ Error: " + e.getMessage()); }
    }

    // ==================== DISPLAY HELPERS ====================

    private void printTournamentTable(List<Tournament> list) {
        System.out.printf("%-5s %-20s %-15s %-12s %-12s %-22s %-6s %-6s%n",
                "ID", "Name", "Game", "Start", "End", "Phase", "Max", "Active");
        System.out.println("-".repeat(100));
        for (Tournament t : list) {
            System.out.printf("%-5d %-20s %-15s %-12s %-12s %-22s %-6d %-6s%n",
                    t.getId(), truncate(t.getName(), 20), truncate(t.getGame(), 15),
                    t.getStartDate() != null ? t.getStartDate().toString() : "N/A",
                    t.getEndDate() != null ? t.getEndDate().toString() : "N/A",
                    truncate(t.getPhase(), 22), t.getMaxTeams(), t.isActive() ? "✅" : "❌");
        }
    }

    private void printTournamentDetail(Tournament t) {
        System.out.println("\n── Tournament Detail ────────────────────");
        System.out.println("ID           : " + t.getId());
        System.out.println("Name         : " + t.getName());
        System.out.println("Game         : " + t.getGame());
        System.out.println("Start Date   : " + t.getStartDate());
        System.out.println("End Date     : " + t.getEndDate());
        System.out.println("Phase        : " + t.getPhase());
        System.out.println("Organizer ID : " + t.getOrganizerId());
        System.out.println("Max Teams    : " + t.getMaxTeams());
        System.out.println("Active       : " + (t.isActive() ? "Yes" : "No"));
        System.out.println("Poster       : " + (t.getPosterPath() != null ? t.getPosterPath() : "N/A"));
        System.out.println("─────────────────────────────────────────");
    }

    private void printRegistrationTable(List<Registration> list) {
        System.out.printf("%-5s %-25s %-30s %-20s %-12s%n", "ID", "Team Name", "Email", "Registered At", "Tournament");
        System.out.println("-".repeat(95));
        for (Registration r : list) {
            System.out.printf("%-5d %-25s %-30s %-20s %-12d%n",
                    r.getId(), truncate(r.getTeamName(), 25), truncate(r.getContactEmail(), 30),
                    r.getCreatedAt() != null ? r.getCreatedAt().toString().substring(0, 16) : "N/A",
                    r.getTournamentId());
        }
    }

    private void printRegistrationDetail(Registration r) {
        System.out.println("\n── Registration Detail ──────────────────");
        System.out.println("ID            : " + r.getId());
        System.out.println("Team Name     : " + r.getTeamName());
        System.out.println("Contact Email : " + r.getContactEmail());
        System.out.println("Registered At : " + r.getCreatedAt());
        System.out.println("Tournament ID : " + r.getTournamentId());
        System.out.println("─────────────────────────────────────────");
    }

    private void printRewardTable(List<TournamentReward> list) {
        System.out.printf("%-5s %-12s %-6s %-15s %-20s%n", "ID", "Tournament", "Rank", "Type", "Value");
        System.out.println("-".repeat(62));
        for (TournamentReward r : list) {
            System.out.printf("%-5d %-12d %-6d %-15s %-20s%n",
                    r.getId(), r.getTournamentId(), r.getRank(),
                    truncate(r.getRewardType(), 15), truncate(r.getRewardValue(), 20));
        }
    }

    private void printRewardDetail(TournamentReward r) {
        System.out.println("\n── Reward Detail ────────────────────────");
        System.out.println("ID            : " + r.getId());
        System.out.println("Tournament ID : " + r.getTournamentId());
        System.out.println("Rank          : " + r.getRank());
        System.out.println("Reward Type   : " + r.getRewardType());
        System.out.println("Reward Value  : " + r.getRewardValue());
        System.out.println("─────────────────────────────────────────");
    }

    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}
