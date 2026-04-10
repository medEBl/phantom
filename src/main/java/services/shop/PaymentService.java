package services.shop;

import entities.shop.Payment;
import tools.Phantom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PaymentService {

    private final Connection cnx = Phantom.getInstance().getCnx();

    private Payment mapRow(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getDouble("amount"),
                rs.getString("payment_method"),
                rs.getString("payment_status"),
                rs.getString("transaction_ref"),
                rs.getTimestamp("payment_date"),
                rs.getInt("shop_item_id")
        );
    }

    public void createPayment(Payment payment) {
        String sql = "INSERT INTO payment (id, amount, payment_method, payment_status, " +
                "transaction_ref, payment_date, shop_item_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, payment.getId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setString(4, payment.getPaymentStatus());
            ps.setString(5, payment.getTransactionRef());
            ps.setTimestamp(6, payment.getPaymentDate());
            ps.setInt(7, payment.getShopItemId());
            ps.executeUpdate();
            System.out.println("✅ Paiement ajouté : " + payment.getTransactionRef());
        } catch (SQLException e) {
            throw new RuntimeException("createPayment failed: " + e.getMessage(), e);
        }
    }

    public void updatePaymentStatus(int id, String newStatus) {
        String sql = "UPDATE payment SET payment_status = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("✅ Statut du paiement ID " + id + " mis à jour : " + newStatus);
        } catch (SQLException e) {
            throw new RuntimeException("updatePaymentStatus failed: " + e.getMessage(), e);
        }
    }

    public void deletePayment(int id) {
        String sql = "DELETE FROM payment WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No payment found with ID: " + id);
            System.out.println("✅ Paiement supprimé : ID " + id);
        } catch (SQLException e) {
            throw new RuntimeException("deletePayment failed: " + e.getMessage(), e);
        }
    }

    public Optional<Payment> getPaymentById(int id) {
        String sql = "SELECT * FROM payment WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPaymentById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payment";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllPayments failed: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Payment> getPaymentsByStatus(String status) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payment WHERE payment_status = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPaymentsByStatus failed: " + e.getMessage(), e);
        }
        return list;
    }

    public Optional<Payment> getPaymentByTransactionRef(String transactionRef) {
        String sql = "SELECT * FROM payment WHERE transaction_ref = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, transactionRef);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getPaymentByTransactionRef failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }
}