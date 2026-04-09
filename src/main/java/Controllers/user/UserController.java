package Controllers.user;
import entities.user.User;
import services.user.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
public class UserController {
    private final UserService userService = new UserService();
    private final Scanner scanner = new Scanner(System.in);

    // ── Entry Point ───────────────────────────────────────────────────────────
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

    // ── CREATE ────────────────────────────────────────────────────────────────
    private void createUser() {
        System.out.println("\n── Create New User ──");
        try {
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Username: ");
            String username = scanner.nextLine().trim();

            System.out.print("Full Name: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            System.out.print("Country: ");
            String country = scanner.nextLine().trim();

            System.out.print("Birth Date (YYYY-MM-DD): ");
            LocalDate birthDate = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Role (PLAYER / COACH / ORGANIZER / ADMIN): ");
            String role = scanner.nextLine().trim().toUpperCase();

            String roles = buildRolesJson(role);
            User user = new User(email, roles, password, username, fullName, country, birthDate, role);
            userService.createUser(user);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ── LIST ALL ──────────────────────────────────────────────────────────────
    private void listAllUsers() {
        System.out.println("\n── All Users ──");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) { System.out.println("No users found."); return; }
        printTable(users);
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────
    private void getUserById() {
        System.out.print("\nEnter User ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            userService.getUserById(id).ifPresentOrElse(
                    this::printDetail,
                    () -> System.out.println("❌ User not found.")
            );
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID.");
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    private void updateUser() {
        System.out.print("\nEnter User ID to update: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<User> opt = userService.getUserById(id);
            if (opt.isEmpty()) { System.out.println("❌ User not found."); return; }

            User user = opt.get();

            System.out.print("New Full Name [" + user.getFullName() + "]: ");
            String fullName = scanner.nextLine().trim();
            if (!fullName.isBlank()) user.setFullName(fullName);

            System.out.print("New Country [" + user.getCountry() + "]: ");
            String country = scanner.nextLine().trim();
            if (!country.isBlank()) user.setCountry(country);

            System.out.print("New Role [" + user.getRole() + "] (PLAYER/COACH/ORGANIZER/ADMIN): ");
            String role = scanner.nextLine().trim();
            if (!role.isBlank()) {
                user.setRole(role.toUpperCase());
                user.setRoles(buildRolesJson(role.toUpperCase()));
            }

            System.out.print("Active? [" + user.isActive() + "] (true/false): ");
            String activeStr = scanner.nextLine().trim();
            if (!activeStr.isBlank()) user.setActive(Boolean.parseBoolean(activeStr));

            System.out.print("New Password (leave blank to skip): ");
            String pw = scanner.nextLine().trim();

            userService.updateUser(user);

            if (!pw.isBlank()) userService.updatePassword(id, pw);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
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
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    private void login() {
        System.out.println("\n── Login ──");
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        userService.login(email, password).ifPresentOrElse(
                u -> {
                    System.out.println("✅ Welcome, " + u.getFullName() + "!");
                    System.out.println("   Role: " + u.getRole() + " | Points: " + u.getAchievementPoints());
                },
                () -> System.out.println("❌ Invalid credentials or account inactive.")
        );
    }

    // ── LIST BY ROLE ──────────────────────────────────────────────────────────
    private void listByRole() {
        System.out.print("\nRole (PLAYER / COACH / ORGANIZER / ADMIN): ");
        String role = scanner.nextLine().trim().toUpperCase();
        List<User> users = userService.getUsersByRole(role);
        if (users.isEmpty()) { System.out.println("No users found."); return; }
        printTable(users);
    }

    // ── LIST ACTIVE ───────────────────────────────────────────────────────────
    private void listActiveUsers() {
        System.out.println("\n── Active Users ──");
        List<User> users = userService.getActiveUsers();
        if (users.isEmpty()) { System.out.println("No active users."); return; }
        printTable(users);
    }

    // ── Print Helpers ─────────────────────────────────────────────────────────
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
