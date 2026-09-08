package model;

/**
 * Abstract base class for all system users.
 * Provides authentication credentials, role-based access, and status management.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public abstract class User {
    protected String userId;
    protected String username;
    protected String password;
    protected String fullName;
    protected String email;
    protected UserRole role;
    protected UserStatus status;

    public User(String userId, String username, String password, String fullName, String email,
                UserRole role, UserStatus status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }

    public void setUsername(String username) { this.username = username; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setStatus(UserStatus status) { this.status = status; }

    public boolean isActive() { return status == UserStatus.ACTIVE; }

    /**
     * Displays the role-specific dashboard after login.
     * Each subclass provides its own implementation.
     */
    public abstract void displayDashboard();

    public abstract String toFileString();

    @Override
    public String toString() {
        return String.format("%s|%s|%s|%s|%s|%s|%s",
                userId, username, password, fullName, email, role.getLabel(), status.getLabel());
    }
}
