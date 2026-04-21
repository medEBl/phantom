package services.shop;

import entities.shop.ShopItem;
import tools.Phantom;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ShopService {

    private final Connection cnx = Phantom.getInstance().getCnx();

    private ShopItem mapRow(ResultSet rs) throws SQLException {
        return new ShopItem(
                rs.getInt("id"),
                rs.getInt("buyer_id"),
                rs.getString("item_name"),
                rs.getString("item_description"),
                rs.getString("item_category"),
                rs.getDouble("price"),
                rs.getInt("quantity"),
                rs.getDouble("total_price"),
                rs.getTimestamp("purchase_date"),
                rs.getString("status")
        );
    }


    public void createShopItem(ShopItem item) {
        String sql = "INSERT INTO shop_item (id, buyer_id, item_name, item_description, item_category, " +
                "price, quantity, total_price, purchase_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, item.getId());
            ps.setInt(2, item.getBuyerId());
            ps.setString(3, item.getItemName());
            ps.setString(4, item.getItemDescription());
            ps.setString(5, item.getItemCategory());
            ps.setDouble(6, item.getPrice());
            ps.setInt(7, item.getQuantity());
            ps.setDouble(8, item.getTotalPrice());
            ps.setTimestamp(9, item.getPurchaseDate());
            ps.setString(10, item.getStatus());
            ps.executeUpdate();
            System.out.println("✅ Jeu ajouté : " + item.getItemName());
        } catch (SQLException e) {
            throw new RuntimeException("createShopItem failed: " + e.getMessage(), e);
        }
    }

    public void updateShopItem(ShopItem item) {
        String sql = "UPDATE shop_item SET item_name=?, item_description=?, item_category=?, " +
                "price=?, quantity=?, total_price=?, status=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getItemDescription());
            ps.setString(3, item.getItemCategory());
            ps.setDouble(4, item.getPrice());
            ps.setInt(5, item.getQuantity());
            ps.setDouble(6, item.getTotalPrice());
            ps.setString(7, item.getStatus());
            ps.setInt(8, item.getId());
            ps.executeUpdate();
            System.out.println("✅ Jeu mis à jour : ID " + item.getId());
        } catch (SQLException e) {
            throw new RuntimeException("updateShopItem failed: " + e.getMessage(), e);
        }
    }


    public void updateQuantity(int id, int newQuantity) {
        String sql = "UPDATE shop_item SET quantity = ?, total_price = price * ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, newQuantity);
            ps.setInt(3, id);
            ps.executeUpdate();
            System.out.println("✅ Quantité mise à jour pour ID " + id + " : " + newQuantity);
        } catch (SQLException e) {
            throw new RuntimeException("updateQuantity failed: " + e.getMessage(), e);
        }
    }


    public void updatePrice(int id, double newPrice) {
        String sql = "UPDATE shop_item SET price = ?, total_price = ? * quantity WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, newPrice);
            ps.setDouble(2, newPrice);
            ps.setInt(3, id);
            ps.executeUpdate();
            System.out.println("✅ Prix mis à jour pour ID " + id + " : " + newPrice + " €");
        } catch (SQLException e) {
            throw new RuntimeException("updatePrice failed: " + e.getMessage(), e);
        }
    }

    // DELETE
    public void deleteShopItem(int id) {
        String sql = "DELETE FROM shop_item WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No item found with ID: " + id);
            System.out.println("✅ Jeu supprimé : ID " + id);
        } catch (SQLException e) {
            throw new RuntimeException("deleteShopItem failed: " + e.getMessage(), e);
        }
    }

    // READ BY ID
    public Optional<ShopItem> getShopItemById(int id) {
        String sql = "SELECT * FROM shop_item WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getShopItemById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // READ ALL
    public List<ShopItem> getAllShopItems() {
        List<ShopItem> list = new ArrayList<>();
        String sql = "SELECT * FROM shop_item";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllShopItems failed: " + e.getMessage(), e);
        }
        return list;
    }

    // READ BY CATEGORY
    public List<ShopItem> getShopItemsByCategory(String category) {
        List<ShopItem> list = new ArrayList<>();
        String sql = "SELECT * FROM shop_item WHERE item_category = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, category);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getShopItemsByCategory failed: " + e.getMessage(), e);
        }
        return list;
    }

    // READ IN STOCK (quantity > 0)
    public List<ShopItem> getShopItemsInStock() {
        List<ShopItem> list = new ArrayList<>();
        String sql = "SELECT * FROM shop_item WHERE quantity > 0";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getShopItemsInStock failed: " + e.getMessage(), e);
        }
        return list;
    }
}