package Controllers.reponse;

import entities.reponse.Reponse;
import services.reponse.ReponseService;

import java.util.Scanner;

public class ReponseController {
    private final ReponseService rs = new ReponseService();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n1. Submit Answers | 2. List All | 3. Delete | 0. Back");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> submit();
                case "2" -> rs.getAllReponses().forEach(System.out::println);
                case "3" -> delete();
                case "0" -> running = false;
                default -> System.out.println("❌ Invalid option.");
            }
        }
    }

    private void submit() {
        try {
            System.out.print("Agent ID: ");
            int idAgent = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Questionnaire ID: ");
            int qId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Answer 1: ");
            String r1 = scanner.nextLine().trim();
            System.out.print("Answer 2: ");
            String r2 = scanner.nextLine().trim();
            System.out.print("Answer 3: ");
            String r3 = scanner.nextLine().trim();
            System.out.print("Answer 4: ");
            String r4 = scanner.nextLine().trim();

            rs.createReponse(new Reponse(idAgent, qId, r1, r2, r3, r4));
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void delete() {
        try {
            System.out.print("Reponse ID to delete: ");
            rs.deleteReponse(Integer.parseInt(scanner.nextLine().trim()));
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}