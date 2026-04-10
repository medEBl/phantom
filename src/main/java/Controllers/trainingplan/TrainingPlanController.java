package Controllers.trainingplan;

import entities.trainingplan.TrainingPlan;
import services.trainingplan.TrainingPlanService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class TrainingPlanController {
    private final TrainingPlanService planService = new TrainingPlanService();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createTrainingPlan();
                case "2" -> listAllPlans();
                case "3" -> getPlanById();
                case "4" -> updatePlan();
                case "5" -> deletePlan();
                case "6" -> listPlansByCoach();
                case "7" -> listPlansByTeam();
                case "8" -> listPlansByDifficulty();
                case "9" -> listPlansByFocusArea();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
        System.out.println("👋 Back to main menu!");
    }

    private void printMenu() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║     TRAINING PLAN MANAGEMENT       ║");
        System.out.println("╠════════════════════════════════════╣");
        System.out.println("║  1. Create Training Plan           ║");
        System.out.println("║  2. List All Training Plans        ║");
        System.out.println("║  3. Get Plan by ID                 ║");
        System.out.println("║  4. Update Training Plan           ║");
        System.out.println("║  5. Delete Training Plan           ║");
        System.out.println("║  6. List Plans by Coach            ║");
        System.out.println("║  7. List Plans by Team             ║");
        System.out.println("║  8. List Plans by Difficulty       ║");
        System.out.println("║  9. List Plans by Focus Area       ║");
        System.out.println("║  0. Back to Main Menu              ║");
        System.out.println("╚════════════════════════════════════╝");
        System.out.print("Choice: ");
    }

    private void createTrainingPlan() {
        System.out.println("\n── Create New Training Plan ──");
        try {
            System.out.print("Title: ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) throw new IllegalArgumentException("Title is required");

            System.out.print("Description: ");
            String description = scanner.nextLine().trim();

            System.out.print("Focus Area (Attaque/Défense/Tactique/Physique/Mental): ");
            String focusArea = scanner.nextLine().trim();
            if (focusArea.isEmpty()) throw new IllegalArgumentException("Focus area is required");

            System.out.print("Difficulty Level (Débutant/Intermédiaire/Avancé): ");
            String difficulty = scanner.nextLine().trim();
            if (difficulty.isEmpty()) throw new IllegalArgumentException("Difficulty level is required");

            System.out.print("Coach ID (must have COACH role): ");
            int coachId = Integer.parseInt(scanner.nextLine().trim());

            System.out.print("Team ID: ");
            int teamId = Integer.parseInt(scanner.nextLine().trim());

            TrainingPlan plan = new TrainingPlan(title, description, focusArea, difficulty, coachId, teamId);
            planService.createTrainingPlan(plan);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listAllPlans() {
        System.out.println("\n── All Training Plans ──");
        List<TrainingPlan> plans = planService.getAllTrainingPlans();
        if (plans.isEmpty()) {
            System.out.println("No training plans found.");
            return;
        }
        printPlanTable(plans);
    }

    private void getPlanById() {
        System.out.print("\nEnter Plan ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            planService.getTrainingPlanById(id).ifPresentOrElse(
                    this::printPlanDetail,
                    () -> System.out.println("❌ Training plan not found.")
            );
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        }
    }

    private void updatePlan() {
        System.out.print("\nEnter Plan ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<TrainingPlan> opt = planService.getTrainingPlanById(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Training plan not found.");
                return;
            }

            TrainingPlan plan = opt.get();

            System.out.print("New Title [" + plan.getTitle() + "]: ");
            String title = scanner.nextLine().trim();
            if (!title.isBlank()) plan.setTitle(title);

            System.out.print("New Description [" + plan.getDescription() + "]: ");
            String description = scanner.nextLine().trim();
            if (!description.isBlank()) plan.setDescription(description);

            System.out.print("New Focus Area [" + plan.getFocusArea() + "]: ");
            String focusArea = scanner.nextLine().trim();
            if (!focusArea.isBlank()) plan.setFocusArea(focusArea);

            System.out.print("New Difficulty Level [" + plan.getDifficultyLevel() + "]: ");
            String difficulty = scanner.nextLine().trim();
            if (!difficulty.isBlank()) plan.setDifficultyLevel(difficulty);

            System.out.print("New Coach ID [" + plan.getCoachId() + "]: ");
            String coachStr = scanner.nextLine().trim();
            if (!coachStr.isBlank()) plan.setCoachId(Integer.parseInt(coachStr));

            System.out.print("New Team ID [" + plan.getTeamId() + "]: ");
            String teamStr = scanner.nextLine().trim();
            if (!teamStr.isBlank()) plan.setTeamId(Integer.parseInt(teamStr));

            planService.updateTrainingPlan(plan);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void deletePlan() {
        System.out.print("\nEnter Plan ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Cancelled.");
                return;
            }
            planService.deleteTrainingPlan(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void listPlansByCoach() {
        System.out.print("\nEnter Coach ID: ");
        try {
            int coachId = Integer.parseInt(scanner.nextLine().trim());
            List<TrainingPlan> plans = planService.getPlansByCoach(coachId);
            if (plans.isEmpty()) {
                System.out.println("No plans found for coach ID: " + coachId);
                return;
            }
            printPlanTable(plans);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid coach ID.");
        }
    }

    private void listPlansByTeam() {
        System.out.print("\nEnter Team ID: ");
        try {
            int teamId = Integer.parseInt(scanner.nextLine().trim());
            List<TrainingPlan> plans = planService.getPlansByTeam(teamId);
            if (plans.isEmpty()) {
                System.out.println("No plans found for team ID: " + teamId);
                return;
            }
            printPlanTable(plans);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid team ID.");
        }
    }

    private void listPlansByDifficulty() {
        System.out.print("\nDifficulty (Débutant/Intermédiaire/Avancé): ");
        String difficulty = scanner.nextLine().trim();
        List<TrainingPlan> plans = planService.getPlansByDifficulty(difficulty);
        if (plans.isEmpty()) {
            System.out.println("No plans found with difficulty: " + difficulty);
            return;
        }
        printPlanTable(plans);
    }

    private void listPlansByFocusArea() {
        System.out.print("\nFocus Area (Attaque/Défense/Tactique/Physique/Mental): ");
        String focusArea = scanner.nextLine().trim();
        List<TrainingPlan> plans = planService.getPlansByFocusArea(focusArea);
        if (plans.isEmpty()) {
            System.out.println("No plans found with focus area: " + focusArea);
            return;
        }
        printPlanTable(plans);
    }

    private void printPlanTable(List<TrainingPlan> plans) {
        System.out.printf("%-5s %-25s %-15s %-15s %-15s %-15s%n",
                "ID", "Title", "Focus Area", "Difficulty", "Coach", "Team");
        System.out.println("-".repeat(100));
        for (TrainingPlan p : plans) {
            System.out.printf("%-5d %-25s %-15s %-15s %-15s %-15s%n",
                    p.getId(),
                    truncate(p.getTitle(), 25),
                    truncate(p.getFocusArea(), 15),
                    truncate(p.getDifficultyLevel(), 15),
                    p.getCoachName() != null ? truncate(p.getCoachName(), 15) : String.valueOf(p.getCoachId()),
                    p.getTeamName() != null ? truncate(p.getTeamName(), 15) : String.valueOf(p.getTeamId()));
        }
    }

    private void printPlanDetail(TrainingPlan p) {
        System.out.println("\n── Training Plan Detail ──────────────────────");
        System.out.println("ID            : " + p.getId());
        System.out.println("Title         : " + p.getTitle());
        System.out.println("Description   : " + p.getDescription());
        System.out.println("Focus Area    : " + p.getFocusArea());
        System.out.println("Difficulty    : " + p.getDifficultyLevel());
        System.out.println("Coach         : " + (p.getCoachName() != null ? p.getCoachName() : "ID: " + p.getCoachId()));
        System.out.println("Team          : " + (p.getTeamName() != null ? p.getTeamName() : "ID: " + p.getTeamId()));
        System.out.println("Created At    : " + p.getCreatedAt());
        System.out.println("─────────────────────────────────────────────");
    }

    private String truncate(String str, int length) {
        if (str == null) return "N/A";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}