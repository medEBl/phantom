package entities.user;

import java.time.LocalDateTime;

public class ResetPasswordRequest {

    private int id;
    private String selector;       // 20-char random token (public, in the URL)
    private String hashedToken;    // 100-char BCrypt hash of the full token
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
    private int userId;

    // ── Constructors ──────────────────────────────────────────────────────────
    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String selector, String hashedToken,
                                LocalDateTime requestedAt, LocalDateTime expiresAt,
                                int userId) {
        this.selector    = selector;
        this.hashedToken = hashedToken;
        this.requestedAt = requestedAt;
        this.expiresAt   = expiresAt;
        this.userId      = userId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }

    public String getHashedToken() { return hashedToken; }
    public void setHashedToken(String hashedToken) { this.hashedToken = hashedToken; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    @Override
    public String toString() {
        return "ResetPasswordRequest{id=" + id + ", userId=" + userId +
               ", expiresAt=" + expiresAt + ", expired=" + isExpired() + "}";
    }
}
