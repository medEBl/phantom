package mains;

import Controllers.agent.AgentController;
import Controllers.questionnaire.QuestionnaireController;
import Controllers.reponse.ReponseController;
import java.util.Scanner;

public class testmain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> new AgentController().run();
                case "2" -> new QuestionnaireController().run();
                case "3" -> new ReponseController().run();
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
        System.out.println("║  1. Agent Management                       ║");
        System.out.println("║  2. Questionnaire Management               ║");
        System.out.println("║  3. Response Management                    ║");
        System.out.println("║  0. Exit                                   ║");
        System.out.println("╚════════════════════════════════════════════╝");
        System.out.print("Choice: ");
    }
}