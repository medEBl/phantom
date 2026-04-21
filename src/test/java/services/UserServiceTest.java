package services;
import services.user.UserService;
import entities.user.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private Connection mockConnection;

    @InjectMocks
    private UserService userService;

    // ── SETUP ───────────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() {
        // Mock connection setup will be done here
    }

    // ── CREATE USER TESTS ─────────────────────────────────────────────────
    @Test
    void testCreateUser_Success() throws SQLException {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User testUser = createTestUser("test_" + timestamp + "@example.com", "testuser_" + timestamp, "Test User");

        // When
        assertDoesNotThrow(() -> userService.createUser(testUser));

        // Then
        assertNotNull(testUser.getId());
        assertTrue(testUser.getId() > 0);
    }

    @Test
    void testCreateUser_EmailAlreadyExists() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        String email = "existing_" + timestamp + "@example.com";
        User existingUser = createTestUser(email, "existing_" + timestamp, "Existing User");
        userService.createUser(existingUser); // First user created

        User duplicateUser = createTestUser(email, "newuser_" + timestamp, "New User");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> userService.createUser(duplicateUser));
        
        assertTrue(exception.getMessage().contains("Email already in use"));
    }

    @Test
    void testCreateUser_UsernameAlreadyExists() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        String username = "takenuser_" + timestamp;
        User existingUser = createTestUser("existing2_" + timestamp + "@example.com", username, "Existing User");
        userService.createUser(existingUser); // First user created

        User duplicateUser = createTestUser("new_" + timestamp + "@example.com", username, "New User");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> userService.createUser(duplicateUser));
        
        assertTrue(exception.getMessage().contains("Username already in use"));
    }

    @Test
    void testCreateUser_InvalidEmail() {
        // Given
        User invalidUser = createTestUser("", "testuser", "Test User");

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> userService.createUser(invalidUser));
        
        assertTrue(exception.getMessage().contains("Email already in use")); // Will fail due to empty email
    }

    // ── EMAIL EXISTS TESTS ───────────────────────────────────────────────────
    @Test
    void testEmailExists_True() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueEmail = "exists_" + timestamp + "@example.com";
        User existingUser = createTestUser(uniqueEmail, "user1_" + timestamp, "User 1");
        userService.createUser(existingUser);

        // When
        boolean result = userService.emailExists(uniqueEmail);

        // Then
        assertTrue(result);
    }

    @Test
    void testEmailExists_False() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User existingUser = createTestUser("exists_" + timestamp + "@example.com", "user1_" + timestamp, "User 1");
        userService.createUser(existingUser);

        // When
        boolean result = userService.emailExists("nonexistent_" + timestamp + "@example.com");

        // Then
        assertFalse(result);
    }

    // ── USERNAME EXISTS TESTS ─────────────────────────────────────────────────
    @Test
    void testUsernameExists_True() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueUsername = "taken_" + timestamp;
        User existingUser = createTestUser("test_" + timestamp + "@example.com", uniqueUsername, "User 1");
        userService.createUser(existingUser);

        // When
        boolean result = userService.usernameExists(uniqueUsername);

        // Then
        assertTrue(result);
    }

    @Test
    void testUsernameExists_False() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User existingUser = createTestUser("test_" + timestamp + "@example.com", "taken_" + timestamp, "User 1");
        userService.createUser(existingUser);

        // When
        boolean result = userService.usernameExists("available_" + timestamp);

        // Then
        assertFalse(result);
    }

    // ── GET USER BY ID TESTS ─────────────────────────────────────────────────
    @Test
    void testGetUserById_Success() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User testUser = createTestUser("test_" + timestamp + "@example.com", "testuser_" + timestamp, "Test User");
        userService.createUser(testUser);

        // When
        Optional<User> result = userService.getUserById(testUser.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("test_" + timestamp + "@example.com", result.get().getEmail());
        assertEquals("testuser_" + timestamp, result.get().getUsername());
    }

    @Test
    void testGetUserById_NotFound() {
        // When
        Optional<User> result = userService.getUserById(999);

        // Then
        assertFalse(result.isPresent());
    }

    // ── GET USER BY EMAIL TESTS ────────────────────────────────────────────────
    @Test
    void testGetUserByEmail_Success() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User testUser = createTestUser("test_" + timestamp + "@example.com", "testuser_" + timestamp, "Test User");
        userService.createUser(testUser);

        // When
        Optional<User> result = userService.getUserByEmail("test_" + timestamp + "@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test_" + timestamp + "@example.com", result.get().getEmail());
        assertEquals("testuser_" + timestamp, result.get().getUsername());
    }

    @Test
    void testGetUserByEmail_NotFound() {
        // When
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    // ── UPDATE USER TESTS ───────────────────────────────────────────────────
    @Test
    void testUpdateUser_Success() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User testUser = createTestUser("old_" + timestamp + "@example.com", "olduser_" + timestamp, "Old User");
        userService.createUser(testUser);
        
        User updatedUser = new User();
        updatedUser.setId(testUser.getId());
        updatedUser.setEmail("new_" + timestamp + "@example.com");
        updatedUser.setUsername("newuser_" + timestamp);
        updatedUser.setFullName("New Name");
        updatedUser.setCountry("New Country");
        updatedUser.setBirthDate(LocalDate.of(1990, 1, 1));
        updatedUser.setRole("ADMIN");
        updatedUser.setRoles("[\"ROLE_ADMIN\"]");

        // When
        assertDoesNotThrow(() -> userService.updateUser(updatedUser));

        // Then
        Optional<User> result = userService.getUserById(testUser.getId());
        assertTrue(result.isPresent());
        assertEquals("new_" + timestamp + "@example.com", result.get().getEmail());
        assertEquals("newuser_" + timestamp, result.get().getUsername());
        assertEquals("New Name", result.get().getFullName());
        assertEquals("New Country", result.get().getCountry());
        assertEquals("ADMIN", result.get().getRole());
    }

    @Test
    void testUpdateUser_NotFound() {
        // Given
        User nonExistentUser = new User();
        nonExistentUser.setId(999);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.updateUser(nonExistentUser));
        
        assertTrue(exception.getMessage().contains("No user found with ID"));
    }

    // ── DELETE USER TESTS ───────────────────────────────────────────────────
    @Test
    void testDeleteUser_Success() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User testUser = createTestUser("test_" + timestamp + "@example.com", "testuser_" + timestamp, "Test User");
        userService.createUser(testUser);

        // When
        assertDoesNotThrow(() -> userService.deleteUser(testUser.getId()));

        // Then
        Optional<User> result = userService.getUserById(testUser.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void testDeleteUser_NotFound() {
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.deleteUser(999));
        
        assertTrue(exception.getMessage().contains("No user found with ID"));
    }

    // ── DISPLAY USER TESTS ───────────────────────────────────────────────────
    @Test
    void testGetAllUsers_Success() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User user1 = createTestUser("user1_" + timestamp + "@example.com", "user1_" + timestamp, "User One");
        User user2 = createTestUser("user2_" + timestamp + "@example.com", "user2_" + timestamp, "User Two");
        userService.createUser(user1);
        userService.createUser(user2);

        // When
        List<User> allUsers = userService.getAllUsers();

        // Then
        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2); // At least our 2 test users
        System.out.println("✅ testGetAllUsers_Success: Found " + allUsers.size() + " users");
    }

    @Test
    void testGetUsersByRole_Success() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User adminUser = createTestUser("admin_" + timestamp + "@example.com", "admin_" + timestamp, "Admin User");
        adminUser.setRole("ADMIN");
        adminUser.setRoles("[\"ROLE_ADMIN\"]");
        userService.createUser(adminUser);

        // When
        List<User> adminUsers = userService.getUsersByRole("ADMIN");

        // Then
        assertNotNull(adminUsers);
        boolean found = false;
        for (User u : adminUsers) {
            if (u.getId() == adminUser.getId()) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        System.out.println("✅ testGetUsersByRole_Success: Found " + adminUsers.size() + " admin users");
    }

    @Test
    void testGetActiveUsers_Success() {
        // Given
        String timestamp = String.valueOf(System.currentTimeMillis());
        User activeUser = createTestUser("active_" + timestamp + "@example.com", "active_" + timestamp, "Active User");
        activeUser.setActive(true);
        userService.createUser(activeUser);

        // When
        List<User> activeUsers = userService.getActiveUsers();

        // Then
        assertNotNull(activeUsers);
        boolean found = false;
        for (User u : activeUsers) {
            if (u.getId() == activeUser.getId()) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        System.out.println("✅ testGetActiveUsers_Success: Found " + activeUsers.size() + " active users");
    }

    
    // ── HELPER METHODS ───────────────────────────────────────────────────────
    private User createTestUser(String email, String username, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPassword("password123");
        user.setCountry("Test Country");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setRole("USER");
        user.setRoles("[\"ROLE_USER\",\"ROLE_PLAYER\"]");
        user.setAchievementPoints(0);
        user.setActive(true);
        return user;
    }

    
    // ── EDGE CASES ───────────────────────────────────────────────────────
    @Test
    void testCreateUser_NullUser() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> userService.createUser(null));
    }

    @Test
    void testUpdateUser_NullUser() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> userService.updateUser(null));
    }

    @Test
    void testDeleteUser_NegativeId() {
        // When & Then
        assertThrows(RuntimeException.class, 
            () -> userService.deleteUser(-1));
    }

    @Test
    void testEmailExists_NullEmail() {
        // When
        boolean result = userService.emailExists(null);

        // Then
        assertFalse(result);
    }

    @Test
    void testUsernameExists_NullUsername() {
        // When
        boolean result = userService.usernameExists(null);

        // Then
        assertFalse(result);
    }
}
