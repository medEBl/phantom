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
            System.out.print("Agent ID (or empty): ");
            String idStr = scanner.nextLine().trim();
            Integer idAgent = idStr.isEmpty() ? null : Integer.parseInt(idStr);
            System.out.print("Game: ");
            String game = scanner.nextLine().trim();
            System.out.print("Question 1: ");
            String q1 = scanner.nextLine().trim();
            System.out.print("Question 2: ");
            String q2 = scanner.nextLine().trim();
            System.out.print("Question 3: ");
            String q3 = scanner.nextLine().trim();
            System.out.print("Question 4: ");
            String q4 = scanner.nextLine().trim();

            qs.createQuestionnaire(new Questionnaire(idAgent, game, q1, q2, q3, q4));
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void delete() {
        try {
            System.out.print("Questionnaire ID to delete: ");
            qs.deleteQuestionnaire(Integer.parseInt(scanner.nextLine().trim()));
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}