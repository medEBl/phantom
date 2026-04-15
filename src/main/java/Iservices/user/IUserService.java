package Iservices.user;
import entities.user.User;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    // CRUD
    void createUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
    Optional<User> getUserById(int id);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();

    // Auth
    Optional<User> login(String email, String password);
    boolean emailExists(String email);
    boolean usernameExists(String username);

    // Filters
    List<User> getUsersByRole(String role);
    List<User> getActiveUsers();
    
    // Search and Filter
    List<User> searchUsers(String searchTerm, String role, String status, int page, int pageSize);
    int countUsers(String searchTerm, String role, String status);
}
