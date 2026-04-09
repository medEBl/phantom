package entities.shop;

import java.sql.Timestamp;

public class Payment {
    private int id;
    private double amount;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionRef;
    private Timestamp paymentDate;
    private int shopItemId;

    public Payment() {}

    public Payment(int id, double amount, String paymentMethod, String paymentStatus,
                   String transactionRef, Timestamp paymentDate, int shopItemId) {
        this.id = id;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionRef = transactionRef;
        this.paymentDate = paymentDate;
        this.shopItemId = shopItemId;
    }

    // Getters
    public int getId() { return id; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getTransactionRef() { return transactionRef; }
    public Timestamp getPaymentDate() { return paymentDate; }
    public int getShopItemId() { return shopItemId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }
    public void setShopItemId(int shopItemId) { this.shopItemId = shopItemId; }

    @Override
    public String toString() {
        return "Payment{id=" + id + ", amount=" + amount + ", status='" + paymentStatus + "', ref='" + transactionRef + "'}";
    }
}