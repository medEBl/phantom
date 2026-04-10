package Controllers.user;

import entities.user.User;
import services.user.UserService;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserController {

    private final UserService userService = new UserService();
    private final Scanner scanner = new Scanner(System.in);

    // ══════════════════════════════════════════════════════════════════════════
    //  VALIDATION — ported 1-to-1 from User.php @Assert annotations
    // ══════════════════════════════════════════════════════════════════════════

    /** Email: NotBlank · valid format · max 180 chars */
    private List<String> validateEmail(String email) {
        List<String> errors = new ArrayList<>();
        if (email == null || email.isBlank()) {
            errors.add("L'email est obligatoire.");
            return errors;
        }
        if (email.length() > 180)
            errors.add("L'email ne doit pas dépasser 180 caractères.");
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            errors.add("L'email \"" + email + "\" n'est pas valide.");
        return errors;
    }

    /**
     * Username: NotBlank · 3–50 chars · only [a-zA-Z0-9_]
     * + DB uniqueness check (excludeId = null on create)
     */
    private List<String> validateUsername(String username, Integer excludeId) {
        List<String> errors = new ArrayList<>();
        if (username == null || username.isBlank()) {
            errors.add("Le nom d'utilisateur est obligatoire.");
            return errors;
        }
        if (username.length() < 3)
            errors.add("Le nom d'utilisateur doit contenir au moins 3 caractères.");
        if (username.length() > 50)
            errors.add("Le nom d'utilisateur ne doit pas dépasser 50 caractères.");
        if (!username.matches("^[a-zA-Z0-9_]+$"))
            errors.add("Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscores.");

        if (errors.isEmpty() && userService.usernameExists(username)) {
            if (excludeId == null) {
                errors.add("Ce nom d'utilisateur est déjà utilisé.");
            } else {
                userService.getUserById(excludeId).ifPresent(existing -> {
                    if (!existing.getUsername().equalsIgnoreCase(username))
                        errors.add("Ce nom d'utilisateur est déjà utilisé.");
                });
            }
        }
        return errors;
    }

    /**
     * Full Name: NotBlank · 2–100 chars
     * · only letters (incl. accented), spaces, hyphens, apostrophes
     */
    private List<String> validateFullName(String fullName) {
        List<String> errors = new ArrayList<>();
        if (fullName == null || fullName.isBlank()) {
            errors.add("Le nom complet est obligatoire.");
            return errors;
        }
        if (fullName.length() < 2)
            errors.add("Le nom complet doit contenir au moins 2 caractères.");
        if (fullName.length() > 100)
            errors.add("Le nom complet ne doit pas dépasser 100 caractères.");
        if (!fullName.matches("^[a-zA-ZÀ-ÿ\\s'\\-]+$"))
            errors.add("Le nom complet ne peut contenir que des lettres, espaces, tirets et apostrophes.");
        return errors;
    }

    /**
     * Password: min 8 chars · ≥1 uppercase · ≥1 lowercase
     * · ≥1 digit · ≥1 special character
     */
    private List<String> validatePassword(String password) {
        List<String> errors = new ArrayList<>();
        if (password == null || password.isBlank()) {
            errors.add("Le mot de passe est obligatoire.");
            return errors;
        }
        if (password.length() < 8)
            errors.add("Le mot de passe doit contenir au moins 8 caractères.");
        if (!password.matches(".*[A-Z].*"))
            errors.add("Le mot de passe doit contenir au moins une lettre majuscule.");
        if (!password.matches(".*[a-z].*"))
            errors.add("Le mot de passe doit contenir au moins une lettre minuscule.");
        if (!password.matches(".*[0-9].*"))
            errors.add("Le mot de passe doit contenir au moins un chiffre.");
        if (!password.matches(".*[^A-Za-z0-9].*"))
            errors.add("Le mot de passe doit contenir au moins un caractère spécial.");
        return errors;
    }

    /**
     * Country: NotBlank · max 50 chars
     * · only letters (incl. accented), spaces, hyphens
     */
    private List<String> validateCountry(String country) {
        List<String> errors = new ArrayList<>();
        if (country == null || country.isBlank()) {
            errors.add("Le pays est obligatoire.");
            return errors;
        }
        if (country.length() > 50)
            errors.add("Le pays ne doit pas dépasser 50 caractères.");
        if (!country.matches("^[a-zA-ZÀ-ÿ\\s\\-]+$"))
            errors.add("Le pays ne peut contenir que des lettres, espaces et tirets.");
        return errors;
    }

    /**
     * Birth Date: NotBlank · valid YYYY-MM-DD
     * · user must be between 13 and 100 years old
     */
    private List<String> validateBirthDate(String dateStr) {
        List<String> errors = new ArrayList<>();
        if (dateStr == null || dateStr.isBlank()) {
            errors.add("La date de naissance est obligatoire.");
            return errors;
        }
        LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(dateStr);
        } catch (Exception e) {
            errors.add("La date de naissance doit être une date valide (format YYYY-MM-DD).");
            return errors;
        }
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 13)
            errors.add("L'utilisateur doit avoir au moins 13 ans.");
        if (age > 100)
            errors.add("La date de naissance n'est pas valide (plus de 100 ans).");
        return errors;
    }

    /**
     * Role: NotBlank · max 20 chars
     * · must be one of PLAYER, COACH, ORGANIZER, ADMIN
     */
    private List<String> validateRole(String role) {
        List<String> errors = new ArrayList<>();
        if (role == null || role.isBlank()) {
            errors.add("Le rôle est obligatoire.");
            return errors;
        }
        if (role.length() > 20)
            errors.add("Le rôle ne doit pas dépasser 20 caractères.");
        if (!List.of("PLAYER", "COACH", "ORGANIZER", "ADMIN").contains(role.toUpperCase()))
            errors.add("Le rôle \"" + role + "\" n'est pas valide. Choisissez parmi : PLAYER, COACH, ORGANIZER, ADMIN.");
        return errors;
    }

    // ── Print errors, return true if any ──────────────────────────────────────
    private boolean hasErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            errors.forEach(e -> System.out.println("   ⚠  " + e));
            return true;
        }
        return false;
    }

    /**
     * Prompt the user repeatedly until the validator returns no errors.
     * Useful for mandatory fields on create.
     */
    private String askValidated(String prompt, java.util.function.Function<String, List<String>> validator) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            List<String> errors = validator.apply(input);
            if (errors.isEmpty()) return input;
            errors.forEach(e -> System.out.println("   ⚠  " + e));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ══════════════════════════════════════════════════════════════════════════

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createUser();
                case "2" -> listAllUsers();
                case "3" -> getUserById();
                case "4" -> updateUser();
                case "5" -> deleteUser();
                case "6" -> login();
                case "7" -> listByRole();
                case "8" -> listActiveUsers();
                case "0" -> running = false;
                default  -> System.out.println("❌ Invalid option.");
            }
        }
        System.out.println("👋 Goodbye!");
    }

    // ── Menu ──────────────────────────────────────────────────────────────────
    private void printMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║       USER MANAGEMENT        ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Create User              ║");
        System.out.println("║  2. List All Users           ║");
        System.out.println("║  3. Get User by ID           ║");
        System.out.println("║  4. Update User              ║");
        System.out.println("║  5. Delete User              ║");
        System.out.println("║  6. Login                    ║");
        System.out.println("║  7. List by Role             ║");
        System.out.println("║  8. List Active Users        ║");
        System.out.println("║  0. Exit                     ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choice: ");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CREATE — every field validated before insert
    // ══════════════════════════════════════════════════════════════════════════
    private void createUser() {
        System.out.println("\n── Create New User ──");
        try {
            // Email — format + uniqueness
            String email = askValidated("Email: ", input -> {
                List<String> errors = validateEmail(input);
                if (errors.isEmpty() && userService.emailExists(input))
                    errors.add("Cet email est déjà utilisé.");
                return errors;
            });

            // Username — format + uniqueness
            String username = askValidated("Username: ", input -> validateUsername(input, null));

            // Full Name
            String fullName = askValidated("Full Name: ", this::validateFullName);

            // Password
            String password = askValidated("Password: ", this::validatePassword);

            // Country
            String country = askValidated("Country: ", this::validateCountry);

            // Birth Date
            String dateStr = askValidated("Birth Date (YYYY-MM-DD): ", this::validateBirthDate);
            LocalDate birthDate = LocalDate.parse(dateStr);

            // Role
            String role = askValidated(
                    "Role (PLAYER / COACH / ORGANIZER / ADMIN): ",
                    input -> validateRole(input.toUpperCase())
            ).toUpperCase();

            String roles = buildRolesJson(role);
            User user = new User(email, roles, password, username, fullName, country, birthDate, role);
            userService.createUser(user);

        } catch (Exception e) {
            System.out.println("❌ Unexpected error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UPDATE — only changed fields are validated; blank = keep existing value
    // ══════════════════════════════════════════════════════════════════════════
    private void updateUser() {
        System.out.print("\nEnter User ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<User> opt = userService.getUserById(id);
            if (opt.isEmpty()) { System.out.println("❌ User not found."); return; }

            User user = opt.get();
            boolean anyError;

            // Full Name
            do {
                System.out.print("New Full Name [" + user.getFullName() + "]: ");
                String input = scanner.nextLine().trim();
                if (input.isBlank()) break;
                List<String> errors = validateFullName(input);
                anyError = hasErrors(errors);
                if (!anyError) user.setFullName(input);
            } while (anyError);

            // Country
            do {
                System.out.print("New Country [" + user.getCountry() + "]: ");
                String input = scanner.nextLine().trim();
                if (input.isBlank()) break;
                List<String> errors = validateCountry(input);
                anyError = hasErrors(errors);
                if (!anyError) user.setCountry(input);
            } while (anyError);

            // Role
            do {
                System.out.print("New Role [" + user.getRole() + "] (PLAYER/COACH/ORGANIZER/ADMIN): ");
                String input = scanner.nextLine().trim();
                if (input.isBlank()) break;
                List<String> errors = validateRole(input.toUpperCase());
                anyError = hasErrors(errors);
                if (!anyError) {
                    user.setRole(input.toUpperCase());
                    user.setRoles(buildRolesJson(input.toUpperCase()));
                }
            } while (anyError);

            // Active status
            System.out.print("Active? [" + user.isActive() + "] (true/false, blank to keep): ");
            String activeStr = scanner.nextLine().trim();
            if (!activeStr.isBlank()) {
                if (!activeStr.equalsIgnoreCase("true") && !activeStr.equalsIgnoreCase("false"))
                    System.out.println("   ⚠  Valeur ignorée — entrez true ou false.");
                else
                    user.setActive(Boolean.parseBoolean(activeStr));
            }

            // Password (optional)
            do {
                System.out.print("New Password (blank to skip): ");
                String pw = scanner.nextLine().trim();
                if (pw.isBlank()) { anyError = false; break; }
                List<String> errors = validatePassword(pw);
                anyError = hasErrors(errors);
                if (!anyError) userService.updatePassword(id, pw);
            } while (anyError);

            userService.updateUser(user);
            System.out.println("✅ User updated.");

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID — must be a number.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  READ / DELETE / LOGIN
    // ══════════════════════════════════════════════════════════════════════════

    private void listAllUsers() {
        System.out.println("\n── All Users ──");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) { System.out.println("No users found."); return; }
        printTable(users);
    }

    private void getUserById() {
        System.out.print("\nEnter User ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            userService.getUserById(id).ifPresentOrElse(
                    this::printDetail,
                    () -> System.out.println("❌ User not found.")
            );
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID — must be a number.");
        }
    }

    private void deleteUser() {
        System.out.print("\nEnter User ID to delete: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirm delete? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Cancelled.");
                return;
            }
            userService.deleteUser(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID — must be a number.");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void login() {
        System.out.println("\n── Login ──");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (email.isBlank() || password.isBlank()) {
            System.out.println("❌ Email and password are required.");
            return;
        }

        userService.login(email, password).ifPresentOrElse(
                u -> {
                    System.out.println("✅ Welcome, " + u.getFullName() + "!");
                    System.out.println("   Role: " + u.getRole() + " | Points: " + u.getAchievementPoints());
                },
                () -> System.out.println("❌ Invalid credentials or account inactive.")
        );
    }

    private void listByRole() {
        String role = askValidated(
                "Role (PLAYER / COACH / ORGANIZER / ADMIN): ",
                input -> validateRole(input.toUpperCase())
        ).toUpperCase();
        List<User> users = userService.getUsersByRole(role);
        if (users.isEmpty()) { System.out.println("No users found for role: " + role); return; }
        printTable(users);
    }

    private void listActiveUsers() {
        System.out.println("\n── Active Users ──");
        List<User> users = userService.getActiveUsers();
        if (users.isEmpty()) { System.out.println("No active users."); return; }
        printTable(users);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PRINT HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private void printTable(List<User> users) {
        System.out.printf("%-5s %-18s %-28s %-12s %-7s %-6s%n",
                "ID", "Username", "Email", "Role", "Active", "Points");
        System.out.println("-".repeat(80));
        for (User u : users) {
            System.out.printf("%-5d %-18s %-28s %-12s %-7s %-6d%n",
                    u.getId(), u.getUsername(), u.getEmail(),
                    u.getRole(), u.isActive(), u.getAchievementPoints());
        }
    }

    private void printDetail(User u) {
        System.out.println("\n── User Detail ──────────────────────");
        System.out.println("ID          : " + u.getId());
        System.out.println("Username    : " + u.getUsername());
        System.out.println("Full Name   : " + u.getFullName());
        System.out.println("Email       : " + u.getEmail());
        System.out.println("Country     : " + u.getCountry());
        System.out.println("Birth Date  : " + u.getBirthDate());
        System.out.println("Role        : " + u.getRole());
        System.out.println("Points      : " + u.getAchievementPoints());
        System.out.println("Active      : " + u.isActive());
        System.out.println("Created At  : " + u.getCreatedAt());
        System.out.println("Last Login  : " + u.getLastLoginAt());
        System.out.println("Photo URL   : " + u.getProfilePhotoUrl());
        System.out.println("─────────────────────────────────────");
    }

    // ── Role JSON builder ─────────────────────────────────────────────────────
    private String buildRolesJson(String role) {
        return switch (role) {
            case "ADMIN"     -> "[\"ROLE_ADMIN\"]";
            case "COACH"     -> "[\"ROLE_USER\",\"ROLE_COACH\"]";
            case "ORGANIZER" -> "[\"ROLE_USER\",\"ROLE_ORGANIZER\"]";
            default          -> "[\"ROLE_USER\",\"ROLE_PLAYER\"]";
        };
    }
}
