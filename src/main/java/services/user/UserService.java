package services.user;
import Iservices.user.IUserService;
import entities.user.User;
import org.mindrot.jbcrypt.BCrypt;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
public class UserService implements IUserService {
    // Grab the shared JDBC connection from your Phantom singleton
    private final Connection cnx = Phantom.getInstance().getCnx();

    // ── Helper: map a ResultSet row → User ───────────────────────────────────
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setRoles(rs.getString("roles"));
        u.setPassword(rs.getString("password"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setCountry(rs.getString("country"));

        Date bd = rs.getDate("birth_date");
        if (bd != null) u.setBirthDate(bd.toLocalDate());

        u.setRole(rs.getString("role"));
        u.setAchievementPoints(rs.getInt("achievement_points"));
        u.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) u.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp lastLogin = rs.getTimestamp("last_login_at");
        if (lastLogin != null) u.setLastLoginAt(lastLogin.toLocalDateTime());

        u.setGoogleId(rs.getString("google_id"));
        u.setGoogleAccessToken(rs.getString("google_access_token"));
        u.setGoogleRefreshToken(rs.getString("google_refresh_token"));
        u.setProfilePhotoUrl(rs.getString("profile_photo_url"));
        u.setProfilePhotoPublicId(rs.getString("profile_photo_public_id"));
        return u;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────
    @Override
    public void createUser(User user) {
        if (emailExists(user.getEmail()))
            throw new IllegalArgumentException("Email already in use: " + user.getEmail());
        if (usernameExists(user.getUsername()))
            throw new IllegalArgumentException("Username already in use: " + user.getUsername());

        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(13));

        String sql = "INSERT INTO user (email, roles, password, username, full_name, country, " +
                "birth_date, role, achievement_points, is_active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getRoles());
            ps.setString(3, hashed);
            ps.setString(4, user.getUsername());
            ps.setString(5, user.getFullName());
            ps.setString(6, user.getCountry());
            ps.setDate(7, Date.valueOf(user.getBirthDate()));
            ps.setString(8, user.getRole());
            ps.setInt(9, 0);
            ps.setBoolean(10, true);
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) user.setId(keys.getInt(1));
            System.out.println("✅ User created with ID: " + user.getId());
        } catch (SQLException e) {
            throw new RuntimeException("createUser failed: " + e.getMessage(), e);
        }
    }

    // ── READ ALL ──────────────────────────────────────────────────────────────
    @Override
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getAllUsers failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ── READ BY ID ────────────────────────────────────────────────────────────
    @Override
    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getUserById failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ── READ BY EMAIL ──────────────────────────────────────────────────────────
    @Override
    public Optional<User> getUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getUserByEmail failed: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // ── READ BY ROLE ──────────────────────────────────────────────────────────
    @Override
    public List<User> getUsersByRole(String role) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE role = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getUsersByRole failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ── READ ACTIVE ───────────────────────────────────────────────────────────
    @Override
    public List<User> getActiveUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE is_active = 1";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new RuntimeException("getActiveUsers failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    @Override
    public void updateUser(User user) {
        String sql = "UPDATE user SET full_name=?, username=?, email=?, country=?, " +
                "birth_date=?, role=?, roles=?, is_active=?, achievement_points=?, " +
                "profile_photo_url=?, profile_photo_public_id=? " +
                "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());           // ← was missing
            ps.setString(3, user.getEmail());              // ← was missing
            ps.setString(4, user.getCountry());
            ps.setDate(5, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
            ps.setString(6, user.getRole());
            ps.setString(7, user.getRoles());
            ps.setBoolean(8, user.isActive());
            ps.setInt(9, user.getAchievementPoints());     // ← was missing
            ps.setString(10, user.getProfilePhotoUrl());
            ps.setString(11, user.getProfilePhotoPublicId());
            ps.setInt(12, user.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No user found with ID: " + user.getId());
            System.out.println("✅ User updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updateUser failed: " + e.getMessage(), e);
        }
    }

    // ── UPDATE PASSWORD ───────────────────────────────────────────────────────
    public void updatePassword(int id, String newPassword) {
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(13));
        String sql = "UPDATE user SET password=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, hashed);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("✅ Password updated.");
        } catch (SQLException e) {
            throw new RuntimeException("updatePassword failed: " + e.getMessage(), e);
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @Override
    public void deleteUser(int id) {
        String sql = "DELETE FROM user WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new RuntimeException("No user found with ID: " + id);
            System.out.println("✅ User deleted.");
        } catch (SQLException e) {
            throw new RuntimeException("deleteUser failed: " + e.getMessage(), e);
        }
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────
    @Override
    public Optional<User> login(String email, String password) {
        String sql = "SELECT * FROM user WHERE email = ? AND is_active = 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            User user = mapRow(rs);
            if (!BCrypt.checkpw(password, user.getPassword())) return Optional.empty();

            // Update last_login_at
            String upd = "UPDATE user SET last_login_at=? WHERE id=?";
            try (PreparedStatement pu = cnx.prepareStatement(upd)) {
                pu.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pu.setInt(2, user.getId());
                pu.executeUpdate();
            }
            user.setLastLoginAt(LocalDateTime.now());
            return Optional.of(user);
        } catch (SQLException e) {
            throw new RuntimeException("login failed: " + e.getMessage(), e);
        }
    }

    // ── EXISTS CHECKS ─────────────────────────────────────────────────────────
    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM user WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    // ── SEARCH AND FILTER ─────────────────────────────────────────────────────
    @Override
    public List<User> searchUsers(String searchTerm, String role, String status, int page, int pageSize) {
        List<User> list = new ArrayList<>();
        
        // Build dynamic SQL query
        StringBuilder sql = new StringBuilder("SELECT * FROM user WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // Add search term filter (search in username, full_name, email)
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?)");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        // Add role filter
        if (role != null && !role.trim().isEmpty() && !"All".equalsIgnoreCase(role)) {
            sql.append(" AND role = ?");
            params.add(role.trim().toUpperCase());
        }
        
        // Add status filter
        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            if ("Active".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = 1");
            } else if ("Inactive".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = 0");
            }
        }
        
        // Add ordering and pagination
        sql.append(" ORDER BY created_at DESC");
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("searchUsers failed: " + e.getMessage(), e);
        }
        
        return list;
    }

    @Override
    public int countUsers(String searchTerm, String role, String status) {
        // Build dynamic SQL query for counting
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM user WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        // Add search term filter
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR full_name LIKE ? OR email LIKE ?)");
            String searchPattern = "%" + searchTerm.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        
        // Add role filter
        if (role != null && !role.trim().isEmpty() && !"All".equalsIgnoreCase(role)) {
            sql.append(" AND role = ?");
            params.add(role.trim().toUpperCase());
        }
        
        // Add status filter
        if (status != null && !status.trim().isEmpty() && !"All".equalsIgnoreCase(status)) {
            if ("Active".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = 1");
            } else if ("Inactive".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = 0");
            }
        }
        
        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("countUsers failed: " + e.getMessage(), e);
        }
        
        return 0;
    }

}
