package mains;

import Controllers.coachingsession.CoachingSessionController;
import Controllers.matchy.MatchyController;
import Controllers.reponse.ReponseController;
import Controllers.team.TeamController;
import Controllers.trainingplan.TrainingPlanController;
import Controllers.user.UserController;
import Controllers.shop.*;
import Controllers.agent.AgentController;
import Controllers.questionnaire.QuestionnaireController;

import java.util.Scanner;

public class testmain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> new UserController().run();
                case "2" -> new TeamController().run();
                case "3" -> new MatchyController().run();
                case "4" -> new TrainingPlanController().run();
                case "5" -> new CoachingSessionController().run();
                case "6" -> new ShopController().run();
                case "7" -> new AgentController().run();
                case "8" -> new QuestionnaireController().run();
                case "9" -> new ReponseController().run();
                case "0" -> {
                    running = false;
                    System.out.println("👋 Goodbye!");
                }
                default -> System.out.println("❌ Invalid option. Please choose 1-9 or 0.");
            }
        }
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║              MAIN MENU                     ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  1. User Management                        ║");
        System.out.println("║  2. Team Management                        ║");
        System.out.println("║  3. Match Management                       ║");
        System.out.println("║  4. Training Plan Management               ║");
        System.out.println("║  5. Coaching Session Management            ║");
        System.out.println("║  6. Shop                                   ║");
        System.out.println("║  7. Agent Management                       ║");
        System.out.println("║  8. Questionnaire Management               ║");
        System.out.println("║  9. Response Management                    ║");
        System.out.println("║  0. Exit                                   ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.print("Choice: ");
    }
}