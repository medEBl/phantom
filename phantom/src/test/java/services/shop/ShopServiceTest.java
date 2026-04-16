package services.shop;

import entities.shop.ShopItem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private Connection mockConnection;

    @InjectMocks
    private ShopService shopService;

    // Helper method to create test shop items
    private ShopItem createTestShopItem(int id, String itemName, String category, double price, int quantity) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return new ShopItem(
            id,
            1, // buyerId
            itemName,
            "Test description for " + itemName,
            category,
            price,
            quantity,
            price * quantity, // totalPrice
            now,
            "AVAILABLE"
        );
    }

    // Helper method to create test shop item with custom status
    private ShopItem createTestShopItemWithStatus(int id, String itemName, String category, double price, int quantity, String status) {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        return new ShopItem(
            id,
            1, // buyerId
            itemName,
            "Test description for " + itemName,
            category,
            price,
            quantity,
            price * quantity, // totalPrice
            now,
            status
        );
    }

    // Helper method to create unique shop item
    private ShopItem createUniqueTestShopItem() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        // Use a much larger unique ID to avoid conflicts with existing data
        int uniqueId = (int) (System.currentTimeMillis() % 1000000000) + 9000000;
        return createTestShopItem(
            uniqueId,
            "TestItem_" + timestamp,
            "GAMES",
            29.99,
            10
        );
    }

    // ================= CREATE SHOP ITEM TESTS =================
    @Test
    void testCreateShopItem_Success() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> shopService.createShopItem(testItem));
    }

    @Test
    void testCreateShopItem_NullItem() {
        // When & Then
        assertThrows(RuntimeException.class, 
            () -> shopService.createShopItem(null));
    }

    // ================= UPDATE SHOP ITEM TESTS =================
    @Test
    void testUpdateShopItem_Success() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);
        
        ShopItem updatedItem = createTestShopItem(testItem.getId(), "Updated Item", "SOFTWARE", 39.99, 5);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> shopService.updateShopItem(updatedItem));
    }

    @Test
    void testUpdateShopItem_NullItem() {
        // When & Then
        assertThrows(RuntimeException.class, 
            () -> shopService.updateShopItem(null));
    }

    // ================= UPDATE QUANTITY TESTS =================
    @Test
    void testUpdateQuantity_Success() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> shopService.updateQuantity(testItem.getId(), 15));
    }

    @Test
    void testUpdateQuantity_NegativeId() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - ShopService doesn't validate negative IDs, so should not throw
        assertDoesNotThrow(() -> shopService.updateQuantity(-1, 10));
    }

    @Test
    void testUpdateQuantity_ZeroQuantity() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - Should not throw exception (zero quantity is valid)
        assertDoesNotThrow(() -> shopService.updateQuantity(testItem.getId(), 0));
    }

    // ================= UPDATE PRICE TESTS =================
    @Test
    void testUpdatePrice_Success() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> shopService.updatePrice(testItem.getId(), 49.99));
    }

    @Test
    void testUpdatePrice_NegativeId() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - ShopService doesn't validate negative IDs, so should not throw
        assertDoesNotThrow(() -> shopService.updatePrice(-1, 29.99));
    }

    @Test
    void testUpdatePrice_NegativePrice() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - Should not throw exception (negative price might be allowed for discounts)
        assertDoesNotThrow(() -> shopService.updatePrice(testItem.getId(), -10.0));
    }

    // ================= DELETE SHOP ITEM TESTS =================
    @Test
    void testDeleteShopItem_Success() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> shopService.deleteShopItem(testItem.getId()));
    }

    @Test
    void testDeleteShopItem_NotFound() {
        // When & Then
        assertThrows(RuntimeException.class, 
            () -> shopService.deleteShopItem(999));
    }

    @Test
    void testDeleteShopItem_NegativeId() {
        // When & Then
        assertThrows(RuntimeException.class, 
            () -> shopService.deleteShopItem(-1));
    }

    // ================= GET SHOP ITEM BY ID TESTS =================
    @Test
    void testGetShopItemById_Success() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When
        Optional<ShopItem> result = shopService.getShopItemById(testItem.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals(testItem.getItemName(), result.get().getItemName());
        assertEquals(testItem.getItemCategory(), result.get().getItemCategory());
        assertEquals(testItem.getPrice(), result.get().getPrice());
        assertEquals(testItem.getQuantity(), result.get().getQuantity());
    }

    @Test
    void testGetShopItemById_NotFound() {
        // When
        Optional<ShopItem> result = shopService.getShopItemById(999);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testGetShopItemById_NegativeId() {
        // When
        Optional<ShopItem> result = shopService.getShopItemById(-1);

        // Then
        assertFalse(result.isPresent());
    }

    // ================= GET ALL SHOP ITEMS TESTS =================
    @Test
    void testGetAllShopItems_Success() throws SQLException {
        // Given
        ShopItem item1 = createUniqueTestShopItem();
        ShopItem item2 = createUniqueTestShopItem();
        shopService.createShopItem(item1);
        shopService.createShopItem(item2);

        // When
        List<ShopItem> allItems = shopService.getAllShopItems();

        // Then
        assertNotNull(allItems);
        assertTrue(allItems.size() >= 2); // At least our 2 test items
        System.out.println("testGetAllShopItems_Success: Found " + allItems.size() + " items");
    }

    @Test
    void testGetAllShopItems_EmptyDatabase() throws SQLException {
        // When
        List<ShopItem> allItems = shopService.getAllShopItems();

        // Then
        assertNotNull(allItems);
        System.out.println("testGetAllShopItems_EmptyDatabase: Found " + allItems.size() + " items");
    }

    // ================= GET SHOP ITEMS BY CATEGORY TESTS =================
    @Test
    void testGetShopItemsByCategory_Success() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id1 = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        int id2 = Integer.parseInt(timestamp.substring(timestamp.length() - 5)) + 10000;
        ShopItem gameItem = createTestShopItem(id1, "TestGame_" + timestamp, "GAMES", 49.99, 2);
        ShopItem softwareItem = createTestShopItem(id2, "TestSoftware_" + timestamp, "SOFTWARE", 99.99, 1);
        shopService.createShopItem(gameItem);
        shopService.createShopItem(softwareItem);

        // When
        List<ShopItem> gameItems = shopService.getShopItemsByCategory("GAMES");

        // Then
        assertNotNull(gameItems);
        assertTrue(gameItems.size() >= 1); // At least our test game
        boolean found = false;
        for (ShopItem item : gameItems) {
            if (item.getId() == gameItem.getId()) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        System.out.println("testGetShopItemsByCategory_Success: Found " + gameItems.size() + " games");
    }

    @Test
    void testGetShopItemsByCategory_NotFound() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        ShopItem item = createTestShopItem(id, "TestItem_" + timestamp, "SOFTWARE", 29.99, 3);
        shopService.createShopItem(item);

        // When
        List<ShopItem> hardwareItems = shopService.getShopItemsByCategory("NONEXISTENT_CATEGORY");

        // Then
        assertNotNull(hardwareItems);
        assertEquals(0, hardwareItems.size());
        System.out.println("testGetShopItemsByCategory_NotFound: Found " + hardwareItems.size() + " items in NONEXISTENT_CATEGORY");
    }

    @Test
    void testGetShopItemsByCategory_NullCategory() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        ShopItem item = createTestShopItem(id, "TestItem_" + timestamp, "GAMES", 29.99, 3);
        shopService.createShopItem(item);

        // When - ShopService doesn't validate null category, so should not throw
        List<ShopItem> nullCategoryItems = shopService.getShopItemsByCategory(null);

        // Then
        assertNotNull(nullCategoryItems);
        assertEquals(0, nullCategoryItems.size());
        System.out.println("testGetShopItemsByCategory_NullCategory: Found " + nullCategoryItems.size() + " items in null category");
    }

    @Test
    void testGetShopItemsByCategory_EmptyCategory() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        ShopItem item = createTestShopItem(id, "TestItem_" + timestamp, "GAMES", 29.99, 3);
        shopService.createShopItem(item);

        // When
        List<ShopItem> emptyCategoryItems = shopService.getShopItemsByCategory("");

        // Then
        assertNotNull(emptyCategoryItems);
        assertEquals(0, emptyCategoryItems.size());
        System.out.println("testGetShopItemsByCategory_EmptyCategory: Found " + emptyCategoryItems.size() + " items in empty category");
    }

    // ================= GET SHOP ITEMS IN STOCK TESTS =================
    @Test
    void testGetShopItemsInStock_Success() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id1 = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        int id2 = Integer.parseInt(timestamp.substring(timestamp.length() - 5)) + 10000;
        ShopItem inStockItem = createTestShopItem(id1, "InStock_" + timestamp, "GAMES", 39.99, 5);
        ShopItem outOfStockItem = createTestShopItem(id2, "OutOfStock_" + timestamp, "SOFTWARE", 49.99, 0);
        shopService.createShopItem(inStockItem);
        shopService.createShopItem(outOfStockItem);

        // When
        List<ShopItem> inStockItems = shopService.getShopItemsInStock();

        // Then
        assertNotNull(inStockItems);
        assertTrue(inStockItems.size() >= 1); // At least our in-stock item
        boolean found = false;
        for (ShopItem item : inStockItems) {
            if (item.getId() == inStockItem.getId()) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        System.out.println("testGetShopItemsInStock_Success: Found " + inStockItems.size() + " items in stock");
    }

    @Test
    void testGetShopItemsInStock_AllOutOfStock() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id1 = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        int id2 = Integer.parseInt(timestamp.substring(timestamp.length() - 5)) + 10000;
        ShopItem outOfStockItem1 = createTestShopItem(id1, "OutOfStock1_" + timestamp, "GAMES", 29.99, 0);
        ShopItem outOfStockItem2 = createTestShopItem(id2, "OutOfStock2_" + timestamp, "SOFTWARE", 39.99, 0);
        shopService.createShopItem(outOfStockItem1);
        shopService.createShopItem(outOfStockItem2);

        // When
        List<ShopItem> inStockItems = shopService.getShopItemsInStock();

        // Then - Check that our out-of-stock items are not in the results
        assertNotNull(inStockItems);
        boolean foundOutOfStock1 = false;
        boolean foundOutOfStock2 = false;
        for (ShopItem item : inStockItems) {
            if (item.getId() == outOfStockItem1.getId()) foundOutOfStock1 = true;
            if (item.getId() == outOfStockItem2.getId()) foundOutOfStock2 = true;
        }
        assertFalse(foundOutOfStock1);
        assertFalse(foundOutOfStock2);
        System.out.println("testGetShopItemsInStock_AllOutOfStock: Found " + inStockItems.size() + " items in stock (database may contain other items)");
    }

    // ================= EDGE CASES =================
    @Test
    void testCreateShopItem_InvalidPrice() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        ShopItem invalidPriceItem = createTestShopItem(id, "InvalidPrice", "GAMES", -100.0, 5);

        // When & Then - Should not throw (negative prices might be allowed for discounts)
        assertDoesNotThrow(() -> shopService.createShopItem(invalidPriceItem));
    }

    @Test
    void testCreateShopItem_ZeroQuantity() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        ShopItem zeroQuantityItem = createTestShopItem(id, "ZeroQuantity", "SOFTWARE", 29.99, 0);

        // When & Then - Should not throw (zero quantity is valid)
        assertDoesNotThrow(() -> shopService.createShopItem(zeroQuantityItem));
    }

    @Test
    void testCreateShopItem_NegativeQuantity() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        ShopItem negativeQuantityItem = createTestShopItem(id, "NegativeQuantity", "GAMES", 19.99, -5);

        // When & Then - Should not throw (negative quantity might be allowed for inventory adjustments)
        assertDoesNotThrow(() -> shopService.createShopItem(negativeQuantityItem));
    }



    @Test
    void testUpdateQuantity_NegativeQuantity() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - Should not throw (negative quantity might be allowed for inventory adjustments)
        assertDoesNotThrow(() -> shopService.updateQuantity(testItem.getId(), -5));
    }

    @Test
    void testUpdatePrice_ZeroPrice() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        shopService.createShopItem(testItem);

        // When & Then - Should not throw (zero price might be allowed for free items)
        assertDoesNotThrow(() -> shopService.updatePrice(testItem.getId(), 0.0));
    }

    // ================= INTEGRATION TESTS =================
    @Test
    void testFullItemLifecycle() throws SQLException {
        // Given
        ShopItem testItem = createUniqueTestShopItem();
        
        // When - Create
        assertDoesNotThrow(() -> shopService.createShopItem(testItem));
        
        // Then - Verify creation
        Optional<ShopItem> created = shopService.getShopItemById(testItem.getId());
        assertTrue(created.isPresent());
        assertEquals(testItem.getItemName(), created.get().getItemName());
        
        // When - Update
        ShopItem updatedItem = createTestShopItem(testItem.getId(), "Updated " + testItem.getItemName(), 
                                                 testItem.getItemCategory(), testItem.getPrice() * 1.5, 15);
        assertDoesNotThrow(() -> shopService.updateShopItem(updatedItem));
        
        // Then - Verify update
        Optional<ShopItem> updated = shopService.getShopItemById(testItem.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated " + testItem.getItemName(), updated.get().getItemName());
        
        // When - Update quantity
        assertDoesNotThrow(() -> shopService.updateQuantity(testItem.getId(), 20));
        
        // When - Update price
        assertDoesNotThrow(() -> shopService.updatePrice(testItem.getId(), 59.99));
        
        // When - Delete
        assertDoesNotThrow(() -> shopService.deleteShopItem(testItem.getId()));
        
        // Then - Verify deletion
        Optional<ShopItem> deleted = shopService.getShopItemById(testItem.getId());
        assertFalse(deleted.isPresent());
        
        System.out.println("testFullItemLifecycle: Full lifecycle test completed successfully");
    }

    @Test
    void testMultipleItemsInSameCategory() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id1 = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        int id2 = Integer.parseInt(timestamp.substring(timestamp.length() - 5)) + 10000;
        int id3 = Integer.parseInt(timestamp.substring(timestamp.length() - 4)) + 20000;
        ShopItem game1 = createTestShopItem(id1, "Game1_" + timestamp, "GAMES", 29.99, 3);
        ShopItem game2 = createTestShopItem(id2, "Game2_" + timestamp, "GAMES", 39.99, 2);
        ShopItem game3 = createTestShopItem(id3, "Game3_" + timestamp, "GAMES", 49.99, 1);
        
        // When
        shopService.createShopItem(game1);
        shopService.createShopItem(game2);
        shopService.createShopItem(game3);
        
        // Then
        List<ShopItem> games = shopService.getShopItemsByCategory("GAMES");
        assertTrue(games.size() >= 3);
        
        List<ShopItem> inStock = shopService.getShopItemsInStock();
        assertTrue(inStock.size() >= 3); // All have quantity > 0
        
        System.out.println("testMultipleItemsInSameCategory: Found " + games.size() + " games, " + inStock.size() + " items in stock");
    }

    @Test
    void testMixedStockLevels() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        int id1 = Integer.parseInt(timestamp.substring(timestamp.length() - 6));
        int id2 = Integer.parseInt(timestamp.substring(timestamp.length() - 5)) + 10000;
        int id3 = Integer.parseInt(timestamp.substring(timestamp.length() - 4)) + 20000;
        int id4 = Integer.parseInt(timestamp.substring(timestamp.length() - 3)) + 30000;
        ShopItem inStock1 = createTestShopItem(id1, "InStock1_" + timestamp, "GAMES", 29.99, 5);
        ShopItem inStock2 = createTestShopItem(id2, "InStock2_" + timestamp, "SOFTWARE", 39.99, 1);
        ShopItem outOfStock1 = createTestShopItem(id3, "OutOfStock1_" + timestamp, "HARDWARE", 49.99, 0);
        ShopItem outOfStock2 = createTestShopItem(id4, "OutOfStock2_" + timestamp, "ACCESSORIES", 19.99, 0);
        
        // When
        shopService.createShopItem(inStock1);
        shopService.createShopItem(inStock2);
        shopService.createShopItem(outOfStock1);
        shopService.createShopItem(outOfStock2);
        
        // Then
        List<ShopItem> allItems = shopService.getAllShopItems();
        List<ShopItem> inStockItems = shopService.getShopItemsInStock();
        
        assertTrue(allItems.size() >= 4);
        assertTrue(inStockItems.size() >= 2);
        assertTrue(inStockItems.size() < allItems.size()); // Some should be out of stock
        
        System.out.println("testMixedStockLevels: Total items: " + allItems.size() + ", In stock: " + inStockItems.size());
    }
}
