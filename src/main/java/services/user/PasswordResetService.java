package services.user;

import entities.user.ResetPasswordRequest;
import entities.user.User;
import org.mindrot.jbcrypt.BCrypt;
import tools.Phantom;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.mail.*;
import javax.mail.internet.*;

public class PasswordResetService {

    private final Connection cnx = Phantom.getInstance().getCnx();

    // ── Config email (change these) ───────────────────────────────────────────
    private static final String MAIL_FROM     = "noreply@phantomforce.com";
    private static final String MAIL_HOST     = "smtp.gmail.com";
    private static final String MAIL_PORT     = "587";
    private static final String MAIL_USER     = "phantomforce619@gmail.com";       // ← change
    private static final String MAIL_PASSWORD = "ribsuezxxzpxnqiz";          // ← change (Gmail App Password)
    private static final int    TOKEN_EXPIRY_HOURS = 1;

    // ═════════════════════════════════════════════════════════════════════════
    //  STEP 1 — Request a password reset
    //  - Find user by email
    //  - Generate token
    //  - Save to DB
    //  - Send email
    // ═════════════════════════════════════════════════════════════════════════
    public boolean requestReset(String email) {
        // 1. Find user
        Optional<User> userOpt = findUserByEmail(email);
        if (userOpt.isEmpty()) {
            // Return true anyway to avoid email enumeration attack
            return true;
        }
        User user = userOpt.get();

        // 2. Delete any existing token for this user
        deleteExistingTokens(user.getId());

        // 3. Generate a secure token
        //    selector = first 20 chars (goes in URL, used to find the record)
        //    verifier = rest of the UUID (hashed, stored in DB)
        String fullToken = UUID.randomUUID().toString().replace("-", "") +
                           UUID.randomUUID().toString().replace("-", "");
        String selector  = fullToken.substring(0, 20);
        String verifier  = fullToken.substring(20);
        String hashedToken = BCrypt.hashpw(verifier, BCrypt.gensalt(13));

        // 4. Save to DB
        LocalDateTime now     = LocalDateTime.now();
        LocalDateTime expires = now.plusHours(TOKEN_EXPIRY_HOURS);
        saveToken(selector, hashedToken, now, expires, user.getId());

        // 5. Send email with reset link
        String resetLink = "phantom://reset-password?selector=" + selector + "&token=" + verifier;
        sendResetEmail(email, user.getFullName(), selector, verifier);

        return true;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STEP 2 — Validate the token from the link
    // ═════════════════════════════════════════════════════════════════════════
    public Optional<ResetPasswordRequest> validateToken(String selector, String verifier) {
        String sql = "SELECT * FROM reset_password_request WHERE selector = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, selector);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();

            ResetPasswordRequest req = mapRow(rs);

            // Check expiry
            if (req.isExpired()) {
                deleteById(req.getId());
                return Optional.empty();
            }

            // Check token hash
            if (!BCrypt.checkpw(verifier, req.getHashedToken()))
                return Optional.empty();

            return Optional.of(req);
        } catch (SQLException e) {
            throw new RuntimeException("validateToken failed: " + e.getMessage(), e);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STEP 3 — Reset the password
    // ═════════════════════════════════════════════════════════════════════════
    public boolean resetPassword(String selector, String verifier, String newPassword) {
        Optional<ResetPasswordRequest> reqOpt = validateToken(selector, verifier);
        if (reqOpt.isEmpty()) return false;

        ResetPasswordRequest req = reqOpt.get();

        // Hash and update the password
        String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(13));
        String sql = "UPDATE user SET password = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, hashed);
            ps.setInt(2, req.getUserId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("resetPassword failed: " + e.getMessage(), e);
        }

        // Delete the used token
        deleteById(req.getId());
        return true;
    }

    // ── DB helpers ────────────────────────────────────────────────────────────

    private Optional<User> findUserByEmail(String email) {
        String sql = "SELECT * FROM user WHERE email = ? AND is_active = 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return Optional.empty();
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setFullName(rs.getString("full_name"));
            return Optional.of(u);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    private void saveToken(String selector, String hashedToken,
                           LocalDateTime requestedAt, LocalDateTime expiresAt, int userId) {
        String sql = "INSERT INTO reset_password_request " +
                     "(selector, hashed_token, requested_at, expires_at, user_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, selector);
            ps.setString(2, hashedToken);
            ps.setTimestamp(3, Timestamp.valueOf(requestedAt));
            ps.setTimestamp(4, Timestamp.valueOf(expiresAt));
            ps.setInt(5, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("saveToken failed: " + e.getMessage(), e);
        }
    }

    private void deleteExistingTokens(int userId) {
        String sql = "DELETE FROM reset_password_request WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteExistingTokens failed: " + e.getMessage(), e);
        }
    }

    private void deleteById(int id) {
        String sql = "DELETE FROM reset_password_request WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("deleteById failed: " + e.getMessage(), e);
        }
    }

    private ResetPasswordRequest mapRow(ResultSet rs) throws SQLException {
        ResetPasswordRequest r = new ResetPasswordRequest();
        r.setId(rs.getInt("id"));
        r.setSelector(rs.getString("selector"));
        r.setHashedToken(rs.getString("hashed_token"));
        r.setRequestedAt(rs.getTimestamp("requested_at").toLocalDateTime());
        r.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
        r.setUserId(rs.getInt("user_id"));
        return r;
    }

    // ── Email sender ──────────────────────────────────────────────────────────
    private void sendResetEmail(String toEmail, String fullName, String selector, String verifier) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", MAIL_HOST);
        props.put("mail.smtp.port", MAIL_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(MAIL_USER, MAIL_PASSWORD);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(MAIL_FROM, "Phantom Force"));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject("Reset your Phantom Force password");

            String body = """
    <html>
    <body style="font-family:Arial,sans-serif;background:#0d0d0d;color:#fff;padding:40px;">
      <div style="max-width:520px;margin:auto;background:#1a1a1a;
                  border:1px solid #ff2d2d;border-radius:12px;padding:40px;">
        <h1 style="color:#ff2d2d;margin-top:0;">PHANTOM FORCE</h1>
        <h2>Password Reset Request</h2>
        <p>Hello <strong>%s</strong>,</p>
        <p>Open the app → Forgot Password → paste the two codes below.</p>
        <p style="color:#ff8888;">⏰ Expires in <strong>1 hour</strong>.</p>
        <hr style="border-color:#333;margin:24px 0;"/>
        <p style="color:#aaa;font-size:12px;">SELECTOR CODE</p>
        <div style="background:#0d0d0d;border:1px solid #ff2d2d;border-radius:8px;
                    padding:14px;font-family:monospace;font-size:16px;
                    color:#ff2d2d;word-break:break-all;">%s</div>
        <br/>
        <p style="color:#aaa;font-size:12px;">TOKEN CODE</p>
        <div style="background:#0d0d0d;border:1px solid #ff2d2d;border-radius:8px;
                    padding:14px;font-family:monospace;font-size:16px;
                    color:#ff2d2d;word-break:break-all;">%s</div>
        <hr style="border-color:#333;margin:24px 0;"/>
        <p style="color:#888;font-size:12px;">
          If you didn't request this, ignore this email.
        </p>
      </div>
    </body>
    </html>
    """.formatted(fullName, selector, verifier);

            msg.setContent(body, "text/html; charset=utf-8");
            Transport.send(msg);
            System.out.println("✅ Reset email sent to: " + toEmail);

        } catch (Exception e) {
            System.out.println("⚠ Email could not be sent: " + e.getMessage());
        }
    }
}
