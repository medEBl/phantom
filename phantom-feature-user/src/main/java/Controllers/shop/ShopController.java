package Controllers.shop;

import entities.shop.ShopItem;
import entities.shop.Payment;
import services.shop.ShopService;
import services.shop.PaymentService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ShopController {

    private final ShopService shopService = new ShopService();
    private final PaymentService paymentService = new PaymentService();
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> shopMenu();
                case "2" -> paymentMenu();
                case "0" -> running = false;
                default -> System.out.println("❌ Option invalide.");
            }
        }
        System.out.println("👋 Au revoir !");
    }

    private void printMainMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║      PHANTOM SHOP - JDBC      ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Gestion des Jeux          ║");
        System.out.println("║  2. Gestion des Paiements     ║");
        System.out.println("║  0. Quitter                   ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choix: ");
    }

    // ==================== GESTION DES JEUX ====================

    private void shopMenu() {
        boolean back = false;
        while (!back) {
            printShopMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createShopItem();
                case "2" -> listAllShopItems();
                case "3" -> getShopItemById();
                case "4" -> updateShopItem();
                case "5" -> deleteShopItem();
                case "6" -> updateQuantity();
                case "7" -> updatePrice();
                case "8" -> listByCategory();
                case "9" -> listInStock();
                case "0" -> back = true;
                default -> System.out.println("❌ Option invalide.");
            }
        }
    }

    private void printShopMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║       GESTION DES JEUX        ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Ajouter un jeu            ║");
        System.out.println("║  2. Afficher tous les jeux    ║");
        System.out.println("║  3. Chercher un jeu par ID    ║");
        System.out.println("║  4. Modifier un jeu           ║");
        System.out.println("║  5. Supprimer un jeu          ║");
        System.out.println("║  6. Mettre à jour quantité    ║");
        System.out.println("║  7. Mettre à jour prix        ║");
        System.out.println("║  8. Jeux par catégorie        ║");
        System.out.println("║  9. Jeux en stock             ║");
        System.out.println("║  0. Retour                    ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choix: ");
    }

    private void createShopItem() {
        System.out.println("\n── Ajouter un jeu ──");
        try {
            System.out.print("ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("ID Acheteur: ");
            int buyerId = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Nom: ");
            String name = scanner.nextLine().trim();
            System.out.print("Description: ");
            String desc = scanner.nextLine().trim();
            System.out.print("Catégorie: ");
            String category = scanner.nextLine().trim();
            System.out.print("Prix: ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Quantité: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());

            ShopItem item = new ShopItem(id, buyerId, name, desc, category, price, quantity, price * quantity,
                    new Timestamp(System.currentTimeMillis()), "pending");
            shopService.createShopItem(item);
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void listAllShopItems() {
        System.out.println("\n── Liste des jeux ──");
        List<ShopItem> items = shopService.getAllShopItems();
        if (items.isEmpty()) {
            System.out.println("Aucun jeu trouvé.");
            return;
        }
        printShopTable(items);
    }

    private void getShopItemById() {
        System.out.print("\nID du jeu: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<ShopItem> opt = shopService.getShopItemById(id);
            if (opt.isPresent()) {
                printShopDetail(opt.get());
            } else {
                System.out.println("❌ Jeu non trouvé.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private void updateShopItem() {
        System.out.print("\nID du jeu à modifier: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<ShopItem> opt = shopService.getShopItemById(id);
            if (opt.isEmpty()) {
                System.out.println("❌ Jeu non trouvé.");
                return;
            }
            ShopItem item = opt.get();
            System.out.print("Nouveau nom [" + item.getItemName() + "]: ");
            String name = scanner.nextLine().trim();
            if (!name.isBlank()) item.setItemName(name);
            System.out.print("Nouvelle catégorie [" + item.getItemCategory() + "]: ");
            String category = scanner.nextLine().trim();
            if (!category.isBlank()) item.setItemCategory(category);
            System.out.print("Nouveau prix [" + item.getPrice() + "]: ");
            String priceStr = scanner.nextLine().trim();
            if (!priceStr.isBlank()) item.setPrice(Double.parseDouble(priceStr));
            System.out.print("Nouvelle quantité [" + item.getQuantity() + "]: ");
            String quantityStr = scanner.nextLine().trim();
            if (!quantityStr.isBlank()) item.setQuantity(Integer.parseInt(quantityStr));
            shopService.updateShopItem(item);
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void deleteShopItem() {
        System.out.print("\nID du jeu à supprimer: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirmer ? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Annulé.");
                return;
            }
            shopService.deleteShopItem(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private void updateQuantity() {
        System.out.print("\nID du jeu: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Nouvelle quantité: ");
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            shopService.updateQuantity(id, quantity);
        } catch (NumberFormatException e) {
            System.out.println("❌ Valeur invalide.");
        }
    }

    private void updatePrice() {
        System.out.print("\nID du jeu: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Nouveau prix: ");
            double price = Double.parseDouble(scanner.nextLine().trim());
            shopService.updatePrice(id, price);
        } catch (NumberFormatException e) {
            System.out.println("❌ Valeur invalide.");
        }
    }

    private void listByCategory() {
        System.out.print("\nCatégorie: ");
        String category = scanner.nextLine().trim();
        List<ShopItem> items = shopService.getShopItemsByCategory(category);
        if (items.isEmpty()) {
            System.out.println("Aucun jeu dans cette catégorie.");
            return;
        }
        printShopTable(items);
    }

    private void listInStock() {
        System.out.println("\n── Jeux en stock ──");
        List<ShopItem> items = shopService.getShopItemsInStock();
        if (items.isEmpty()) {
            System.out.println("Aucun jeu en stock.");
            return;
        }
        printShopTable(items);
    }

    // ==================== GESTION DES PAIEMENTS ====================

    private void paymentMenu() {
        boolean back = false;
        while (!back) {
            printPaymentMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> createPayment();
                case "2" -> listAllPayments();
                case "3" -> getPaymentById();
                case "4" -> updatePaymentStatus();
                case "5" -> deletePayment();
                case "6" -> listPaymentsByStatus();
                case "7" -> getPaymentByTransactionRef();
                case "0" -> back = true;
                default -> System.out.println("❌ Option invalide.");
            }
        }
    }

    private void printPaymentMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║     GESTION DES PAIEMENTS     ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Ajouter un paiement       ║");
        System.out.println("║  2. Afficher tous paiements   ║");
        System.out.println("║  3. Chercher par ID           ║");
        System.out.println("║  4. Modifier le statut        ║");
        System.out.println("║  5. Supprimer un paiement     ║");
        System.out.println("║  6. Paiements par statut      ║");
        System.out.println("║  7. Chercher par référence    ║");
        System.out.println("║  0. Retour                    ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choix: ");
    }

    private void createPayment() {
        System.out.println("\n── Ajouter un paiement ──");
        try {
            System.out.print("ID: ");
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Montant: ");
            double amount = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Méthode (card/paypal/wallet): ");
            String method = scanner.nextLine().trim();
            System.out.print("Statut (pending/success/failed): ");
            String status = scanner.nextLine().trim();
            System.out.print("Référence transaction: ");
            String ref = scanner.nextLine().trim();
            System.out.print("ID du jeu associé: ");
            int shopItemId = Integer.parseInt(scanner.nextLine().trim());

            Payment payment = new Payment(id, amount, method, status, ref, new Timestamp(System.currentTimeMillis()), shopItemId);
            paymentService.createPayment(payment);
        } catch (Exception e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void listAllPayments() {
        System.out.println("\n── Liste des paiements ──");
        List<Payment> payments = paymentService.getAllPayments();
        if (payments.isEmpty()) {
            System.out.println("Aucun paiement trouvé.");
            return;
        }
        printPaymentTable(payments);
    }

    private void getPaymentById() {
        System.out.print("\nID du paiement: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            Optional<Payment> opt = paymentService.getPaymentById(id);
            if (opt.isPresent()) {
                printPaymentDetail(opt.get());
            } else {
                System.out.println("❌ Paiement non trouvé.");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private void updatePaymentStatus() {
        System.out.print("\nID du paiement: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Nouveau statut (pending/success/failed): ");
            String status = scanner.nextLine().trim();
            paymentService.updatePaymentStatus(id, status);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private void deletePayment() {
        System.out.print("\nID du paiement à supprimer: ");
        try {
            int id = Integer.parseInt(scanner.nextLine().trim());
            System.out.print("Confirmer ? (yes/no): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("Annulé.");
                return;
            }
            paymentService.deletePayment(id);
        } catch (NumberFormatException e) {
            System.out.println("❌ ID invalide.");
        }
    }

    private void listPaymentsByStatus() {
        System.out.print("\nStatut (pending/success/failed): ");
        String status = scanner.nextLine().trim();
        List<Payment> payments = paymentService.getPaymentsByStatus(status);
        if (payments.isEmpty()) {
            System.out.println("Aucun paiement avec ce statut.");
            return;
        }
        printPaymentTable(payments);
    }

    private void getPaymentByTransactionRef() {
        System.out.print("\nRéférence transaction: ");
        String ref = scanner.nextLine().trim();
        Optional<Payment> opt = paymentService.getPaymentByTransactionRef(ref);
        if (opt.isPresent()) {
            printPaymentDetail(opt.get());
        } else {
            System.out.println("❌ Paiement non trouvé.");
        }
    }

    // ==================== AFFICHAGE ====================

    private void printShopTable(List<ShopItem> items) {
        System.out.printf("%-5s %-25s %-8s %-8s%n", "ID", "Nom", "Prix", "Quantité");
        System.out.println("-".repeat(50));
        for (ShopItem i : items) {
            System.out.printf("%-5d %-25s %-8.2f %-8d%n", i.getId(), i.getItemName(), i.getPrice(), i.getQuantity());
        }
    }

    private void printShopDetail(ShopItem item) {
        System.out.println("\n── Détail du jeu ──────────────────────");
        System.out.println("ID          : " + item.getId());
        System.out.println("ID Acheteur : " + item.getBuyerId());
        System.out.println("Nom         : " + item.getItemName());
        System.out.println("Description : " + item.getItemDescription());
        System.out.println("Catégorie   : " + item.getItemCategory());
        System.out.println("Prix        : " + item.getPrice() + " €");
        System.out.println("Quantité    : " + item.getQuantity());
        System.out.println("Total       : " + item.getTotalPrice() + " €");
        System.out.println("Statut      : " + item.getStatus());
        System.out.println("Date        : " + item.getPurchaseDate());
        System.out.println("─────────────────────────────────────────");
    }

    private void printPaymentTable(List<Payment> payments) {
        System.out.printf("%-5s %-10s %-10s %-20s %-20s%n", "ID", "Montant", "Méthode", "Statut", "Référence");
        System.out.println("-".repeat(70));
        for (Payment p : payments) {
            System.out.printf("%-5d %-10.2f %-10s %-20s %-20s%n",
                    p.getId(), p.getAmount(), p.getPaymentMethod(), p.getPaymentStatus(), p.getTransactionRef());
        }
    }

    private void printPaymentDetail(Payment payment) {
        System.out.println("\n── Détail du paiement ──────────────────");
        System.out.println("ID            : " + payment.getId());
        System.out.println("Montant       : " + payment.getAmount() + " €");
        System.out.println("Méthode       : " + payment.getPaymentMethod());
        System.out.println("Statut        : " + payment.getPaymentStatus());
        System.out.println("Référence     : " + payment.getTransactionRef());
        System.out.println("Date          : " + payment.getPaymentDate());
        System.out.println("ID Jeu associé: " + payment.getShopItemId());
        System.out.println("─────────────────────────────────────────");
    }
}