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
        String sql = "UPDATE user SET full_name=?, country=?, birth_date=?, role=?, " +
                "roles=?, is_active=?, profile_photo_url=?, profile_photo_public_id=? " +
                "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getCountry());
            ps.setDate(3, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
            ps.setString(4, user.getRole());
            ps.setString(5, user.getRoles());
            ps.setBoolean(6, user.isActive());
            ps.setString(7, user.getProfilePhotoUrl());
            ps.setString(8, user.getProfilePhotoPublicId());
            ps.setInt(9, user.getId());

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

}
