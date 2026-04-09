package entities.shop;

import java.sql.Timestamp;

public class ShopItem {
    private int id;
    private int buyerId;
    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private double price;
    private int quantity;
    private double totalPrice;
    private Timestamp purchaseDate;
    private String status;

    // Constructeur complet
    public ShopItem(int id, int buyerId, String itemName, String itemDescription, String itemCategory,
                    double price, int quantity, double totalPrice, Timestamp purchaseDate, String status) {
        this.id = id;
        this.buyerId = buyerId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemCategory = itemCategory;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.purchaseDate = purchaseDate;
        this.status = status;
    }

    // Constructeur simplifié
    public ShopItem(int id, int buyerId, String itemName, double price, int quantity) {
        this.id = id;
        this.buyerId = buyerId;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price * quantity;
        this.status = "pending";
        this.purchaseDate = new Timestamp(System.currentTimeMillis());
        this.itemDescription = "";
        this.itemCategory = "";
    }

    // Getters
    public int getId() { return id; }
    public int getBuyerId() { return buyerId; }
    public String getItemName() { return itemName; }
    public String getItemDescription() { return itemDescription; }
    public String getItemCategory() { return itemCategory; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return totalPrice; }
    public Timestamp getPurchaseDate() { return purchaseDate; }
    public String getStatus() { return status; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    public void setItemCategory(String itemCategory) { this.itemCategory = itemCategory; }
    public void setPrice(double price) { this.price = price; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = this.price * quantity;
    }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setPurchaseDate(Timestamp purchaseDate) { this.purchaseDate = purchaseDate; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "ShopItem{id=" + id + ", name='" + itemName + "', price=" + price + ", quantity=" + quantity + "}";
    }
}