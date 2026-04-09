package iservices.shop;

import entities.shop.ShopItem;

import java.util.List;
import java.util.Optional;

public interface IShopService {

    // CRUD de base
    void createShopItem(ShopItem item);
    void updateShopItem(ShopItem item);
    void deleteShopItem(int id);
    Optional<ShopItem> getShopItemById(int id);
    List<ShopItem> getAllShopItems();

    // Fonctionnalités supplémentaires
    void updateQuantity(int id, int newQuantity);
    void updatePrice(int id, double newPrice);
    List<ShopItem> getShopItemsByCategory(String category);
    List<ShopItem> getShopItemsInStock();
}