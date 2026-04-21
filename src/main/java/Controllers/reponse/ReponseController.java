package Controllers.reponse;

import entities.reponse.Reponse;
import services.reponse.ReponseService;

import java.util.Scanner;
import java.util.regex.Pattern;

public class ReponseController {
    private final ReponseService rs = new ReponseService();
    private final Scanner scanner = new Scanner(System.in);

    private final Pattern BAD_WORDS_PATTERN = Pattern.compile(".*\\b(badword|insult|stupid)\\b.*", Pattern.CASE_INSENSITIVE);

    public void run() {
        boolean running = true;
        while (running) {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║        GESTION DES REPONSES          ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ 1. Submit Answers                    ║");
            System.out.println("║ 2. List All                          ║");
            System.out.println("║ 3. Delete                            ║");
            System.out.println("║ 0. Back                              ║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.print("Choice: ");
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

    private boolean isValidResponse(String text) {
        if (text == null) return false;
        String t = text.trim();
        return t.length() >= 5
                && !t.equalsIgnoreCase("Aucune réponse")
                && !t.equalsIgnoreCase("Aucune reponse");
    }

    private void submit() {
        try {
            int idAgent = 0;
            while (true) {
                System.out.print("Agent ID: ");
                try {
                    idAgent = Integer.parseInt(scanner.nextLine().trim());
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Agent ID must be a valid number.");
                }
            }

            int qId = 0;
            while (true) {
                System.out.print("Questionnaire ID: ");
                try {
                    qId = Integer.parseInt(scanner.nextLine().trim());
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: Questionnaire ID must be a valid number.");
                }
            }

            String r1 = "";
            while (true) {
                System.out.print("Answer 1 (Mandatory, min 5 chars): ");
                r1 = scanner.nextLine().trim();
                if (!isValidResponse(r1)) {
                    System.out.println("❌ Error: Answer 1 is too short. Please write at least 5 characters.");
                } else if (BAD_WORDS_PATTERN.matcher(r1).matches()) {
                    System.out.println("❌ Error: Please be professional. Inappropriate language detected.");
                } else break;
            }

            String r2 = "";
            while (true) {
                System.out.print("Answer 2 (Mandatory, min 5 chars): ");
                r2 = scanner.nextLine().trim();
                if (!isValidResponse(r2)) {
                    System.out.println("❌ Error: Answer 2 is too short. Please write at least 5 characters.");
                } else break;
            }

            String r3 = "";
            while (true) {
                System.out.print("Answer 3 (Optional, press Enter to skip, or min 5 chars): ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    r3 = null;
                    break;
                } else if (!isValidResponse(input)) {
                    System.out.println("❌ Error: Answer 3 is too short. Please write at least 5 characters if answering.");
                } else {
                    r3 = input;
                    break;
                }
            }

            String r4 = "";
            while (true) {
                System.out.print("Answer 4 (Optional, press Enter to skip, or min 5 chars): ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    r4 = null;
                    break;
                } else if (!isValidResponse(input)) {
                    System.out.println("❌ Error: Answer 4 is too short. Please write at least 5 characters if answering.");
                } else {
                    r4 = input;
                    break;
                }
            }

            rs.createReponse(new Reponse(idAgent, qId, r1, r2, r3, r4));
            System.out.println("✅ Success: Answers submitted successfully!");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void delete() {
        try {
            int id = 0;
            while (true) {
                System.out.print("Enter Reponse ID to delete: ");
                try {
                    id = Integer.parseInt(scanner.nextLine().trim());
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Error: ID must be a valid number.");
                }
            }
            rs.deleteReponse(id);
            System.out.println("✅ Success: Response deleted (if it existed).");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}