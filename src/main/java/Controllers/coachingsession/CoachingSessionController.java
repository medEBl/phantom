package Controllers.coachingsession;

import entities.coachingsession.CoachingSession;
import services.coachingsession.CoachingSessionService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class CoachingSessionController {
    private final CoachingSessionService sessionService = new CoachingSessionService();
    private final Scanner scanner = new Scanner(System.in);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createSession();
                case "2" -> listAllSessions();
                case "3" -> getSessionById();
                case "4" -> updateSession();
                case "5" -> deleteSession();
                case "6" -> listSessionsByCoach();
                case "7" -> listSessionsByTeam();
                case "8" -> listSessionsByTrainingPlan();
                case "9" -> listUpcomingSessions();
                case "10" -> listPastSessions();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
        System.out.println("👋 Back to main menu!");
    }

    private void printMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║     COACHING SESSION MANAGEMENT    ║");
        System.out.println("╠════════════════════════════════════╣");
        System.out.println("║  1. Create Session                 ║");
        System.out.println("║  2. List All Sessions              ║");
        System.out.println("║  3. Get Session by ID              ║");
        System.out.println("║  4. Update Session                 ║");
        System.out.println("║  5. Delete Session                 ║");
        System.out.println("║  6. List Sessions by Coach         ║");
        System.out.println("║  7. List Sessions by Team          ║");
        System.out.println("║  8. List Sessions by Training Plan ║");
        System.out.println("║  9. List Upcoming Sessions         ║");
        System.out.println("║ 10. List Past Sessions             ║");
        System.out.println("║  0. Back to Main Menu              ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createSession() {
        System.out.println("\n── Create New Coaching Session ──");
        try {
            System.out.print("Coach ID (must have COACH role): ");
            int coachId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Team ID: ");
            int teamId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Session Date (YYYY-MM-DD HH:MM): ");
            LocalDateTime sessionDate = LocalDateTime.parse(scanner.nextLine().trim(), dateFormatter);

            System.out.print("Duration (minutes): ");
            int duration = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Notes (optional): ");
            String notes = scanner.nextLine().trim();
            if (notes.isEmpty()) notes = null;

            System.out.print("Training Plan ID: ");
            int trainingPlanId = Integer.parseInt(scanner.nextLine().trim());

            CoachingSession session = new CoachingSession(coachId, teamId, sessionDate, duration, notes, trainingPlanId);
            sessionService.createCoachingSession(session);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Use YYYY-MM-DD HH:MM");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllSessions() {
        System.out.println("\n── All Coaching Sessions ──");
        List<CoachingSession> sessions = sessionService.getAllCoachingSessions();
        if (sessions.isEmpty()) {
            System.out.println("No coaching sessions found.");
            return;
        }
        printSessionTable(sessions);
    }

    private void getSessionById() {
        System.out.print("\nEnter Session ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            sessionService.getCoachingSessionById(id).ifPresentOrElse(
                    this::printSessionDetail,
                    () -> System.out.println("❌ Coaching session not found.")
            );
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        }
    }

    private void updateSession() {
        System.out.print("\nEnter Session ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<CoachingSession> opt = sessionService.getCoachingSessionById(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Coaching session not found.");
                return;
            }

            CoachingSession session = opt.get();

            System.out.print("New Coach ID [" + session.getCoachId() + "]: ");
            String coachStr = scanner.nextLine().trim();
            if (!coachStr.isBlank()) session.setCoachId(Integer.parseInt(coachStr));

            System.out.print("New Team ID [" + session.getTeamId() + "]: ");
            String teamStr = scanner.nextLine().trim();
            if (!teamStr.isBlank()) session.setTeamId(Integer.parseInt(teamStr));

            System.out.print("New Session Date [" + session.getSessionDate() + "]: ");
            String dateStr = scanner.nextLine().trim();
            if (!dateStr.isBlank()) session.setSessionDate(LocalDateTime.parse(dateStr, dateFormatter));

            System.out.print("New Duration [" + session.getDuration() + "]: ");
            String durationStr = scanner.nextLine().trim();
            if (!durationStr.isBlank()) session.setDuration(Integer.parseInt(durationStr));

            System.out.print("New Notes [" + session.getNotes() + "]: ");
            String notes = scanner.nextLine().trim();
            if (!notes.isBlank()) session.setNotes(notes);

            System.out.print("New Training Plan ID [" + session.getTrainingPlanId() + "]: ");
            String planStr = scanner.nextLine().trim();
            if (!planStr.isBlank()) session.setTrainingPlanId(Integer.parseInt(planStr));

            sessionService.updateCoachingSession(session);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format.");
        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deleteSession() {
        System.out.print("\nEnter Session ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Cancelled.");
                return;
            }
            sessionService.deleteCoachingSession(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listSessionsByCoach() {
        System.out.print("\nEnter Coach ID: ");
        try {
            int coachId = Integer.parseInt(scanner.nextLine().trim());
            List<CoachingSession> sessions = sessionService.getSessionsByCoach(coachId);
            if (sessions.isEmpty()) {
                System.out.println("No sessions found for coach ID: " + coachId);
                return;
            }
            printSessionTable(sessions);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid coach ID.");
        }
    }

    private void listSessionsByTeam() {
        System.out.print("\nEnter Team ID: ");
        try {
            int teamId = Integer.parseInt(scanner.nextLine().trim());
            List<CoachingSession> sessions = sessionService.getSessionsByTeam(teamId);
            if (sessions.isEmpty()) {
                System.out.println("No sessions found for team ID: " + teamId);
                return;
            }
            printSessionTable(sessions);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid team ID.");
        }
    }

    private void listSessionsByTrainingPlan() {
        System.out.print("\nEnter Training Plan ID: ");
        try {
            int planId = Integer.parseInt(scanner.nextLine().trim());
            List<CoachingSession> sessions = sessionService.getSessionsByTrainingPlan(planId);
            if (sessions.isEmpty()) {
                System.out.println("No sessions found for training plan ID: " + planId);
                return;
            }
            printSessionTable(sessions);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid training plan ID.");
        }
    }

    private void listUpcomingSessions() {
        System.out.println("\n── Upcoming Coaching Sessions ──");
        List<CoachingSession> sessions = sessionService.getUpcomingSessions();
        if (sessions.isEmpty()) {
            System.out.println("No upcoming sessions.");
            return;
        }
        printSessionTable(sessions);
    }

    private void listPastSessions() {
        System.out.println("\n── Past Coaching Sessions ──");
        List<CoachingSession> sessions = sessionService.getPastSessions();
        if (sessions.isEmpty()) {
            System.out.println("No past sessions.");
            return;
        }
        printSessionTable(sessions);
    }

    private void printSessionTable(List<CoachingSession> sessions) {
        System.out.printf("%-5s %-19s %-8s %-15s %-15s %-20s%n",
                "ID", "Session Date", "Duration", "Coach", "Team", "Training Plan");
        System.out.println("-".repeat(90));
        for (CoachingSession s : sessions) {
            System.out.printf("%-5d %-19s %-8d %-15s %-15s %-20s%n",
                    s.getId(),
                    s.getSessionDate() != null ? s.getSessionDate().toString().substring(0, 16) : "N/A",
                    s.getDuration(),
                    truncate(s.getCoachName() != null ? s.getCoachName() : "ID:" + s.getCoachId(), 15),
                    truncate(s.getTeamName() != null ? s.getTeamName() : "ID:" + s.getTeamId(), 15),
                    truncate(s.getTrainingPlanTitle() != null ? s.getTrainingPlanTitle() : "ID:" + s.getTrainingPlanId(), 20));
        }
    }

    private void printSessionDetail(CoachingSession s) {
        System.out.println("\n── Coaching Session Detail ──────────────────────");
        System.out.println("ID            : " + s.getId());
        System.out.println("Coach         : " + (s.getCoachName() != null ? s.getCoachName() : "ID: " + s.getCoachId()));
        System.out.println("Team          : " + (s.getTeamName() != null ? s.getTeamName() : "ID: " + s.getTeamId()));
        System.out.println("Session Date  : " + s.getSessionDate());
        System.out.println("Duration      : " + s.getDuration() + " minutes");
        System.out.println("Notes         : " + (s.getNotes() != null ? s.getNotes() : "N/A"));
        System.out.println("Training Plan : " + (s.getTrainingPlanTitle() != null ? s.getTrainingPlanTitle() : "ID: " + s.getTrainingPlanId()));
        System.out.println("─────────────────────────────────────────────────");
    }

    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}