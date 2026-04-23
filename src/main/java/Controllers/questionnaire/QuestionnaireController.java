package Controllers.questionnaire;

import entities.questionnaire.Questionnaire;
import services.questionnaire.QuestionnaireService;

import java.util.Scanner;

public class QuestionnaireController {
    private final QuestionnaireService qs = new QuestionnaireService();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n1. Create Questionnaire | 2. List All | 3. Delete | 0. Back");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> create();
                case "2" -> qs.getAllQuestionnaires().forEach(System.out::println);
                case "3" -> delete();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void create() {
        try {
            Integer idAgent = null;
            while (true) {
                System.out.print("Agent ID (Optional, press Enter to skip): ");
                String idStr = scanner.nextLine().trim();
                if (idStr.isEmpty()) break;
                try {
                    idAgent = Integer.parseInt(idStr);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Agent ID must be a valid number or left blank.");
                }
            }

            String game = "";
            while (true) {
                System.out.print("Game (Mandatory): ");
                game = scanner.nextLine().trim();
                if (game.isBlank()) {
                    System.out.println("❌ Error: Game cannot be empty.");
                } else break;
            }

            String q1 = "";
            while (true) {
                System.out.print("Question 1 (Mandatory): ");
                q1 = scanner.nextLine().trim();
                if (q1.isBlank()) {
                    System.out.println("❌ Error: Question 1 cannot be empty.");
                } else break;
            }

            String q2 = "";
            while (true) {
                System.out.print("Question 2 (Mandatory): ");
                q2 = scanner.nextLine().trim();
                if (q2.isBlank()) {
                    System.out.println("❌ Error: Question 2 cannot be empty.");
                } else break;
            }

            System.out.print("Question 3 (Optional, press Enter to skip): ");
            String q3 = scanner.nextLine().trim();
            if (q3.isEmpty()) q3 = null;

            System.out.print("Question 4 (Optional, press Enter to skip): ");
            String q4 = scanner.nextLine().trim();
            if (q4.isEmpty()) q4 = null;

            qs.createQuestionnaire(new Questionnaire(idAgent, game, q1, q2, q3, q4));
            System.out.println("✅ Success: Questionnaire created!");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void delete() {
        try {
            int id = 0;
            while (true) {
                System.out.print("Enter Questionnaire ID to delete: ");
                try {
                    id = Integer.parseInt(scanner.nextLine().trim());
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Questionnaire ID must be a valid number.");
                }
            }
            qs.deleteQuestionnaire(id);
            System.out.println("✅ Success: Questionnaire deleted (if it existed).");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}