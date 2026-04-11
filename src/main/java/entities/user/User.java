package entities.user;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class User {
    private int id;
    private String email;
    private String roles;
    private String password;
    private String username;
    private String fullName;
    private String country;
    private LocalDate birthDate;
    private String role;
    private int achievementPoints;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private String googleId;
    private String googleAccessToken;
    private String googleRefreshToken;
    private String profilePhotoUrl;
    private String profilePhotoPublicId;
    public User() {}

    public User(String email, String roles, String password, String username,
                String fullName, String country, LocalDate birthDate, String role) {
        this.email    = email;
        this.roles    = roles;
        this.password = password;
        this.username = username;
        this.fullName = fullName;
        this.country  = country;
        this.birthDate = birthDate;
        this.role     = role;
        this.achievementPoints = 0;
        this.isActive  = true;
        this.createdAt = LocalDateTime.now();
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getAchievementPoints() { return achievementPoints; }
    public void setAchievementPoints(int ap) { this.achievementPoints = ap; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getGoogleAccessToken() { return googleAccessToken; }
    public void setGoogleAccessToken(String t) { this.googleAccessToken = t; }

    public String getGoogleRefreshToken() { return googleRefreshToken; }
    public void setGoogleRefreshToken(String t) { this.googleRefreshToken = t; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String url) { this.profilePhotoUrl = url; }

    public String getProfilePhotoPublicId() { return profilePhotoPublicId; }
    public void setProfilePhotoPublicId(String id) { this.profilePhotoPublicId = id; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email +
                "', role='" + role + "', isActive=" + isActive + "}";
    }
}
