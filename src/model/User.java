package model;

public abstract class User {
    protected String userId;
    protected String password;
    protected String fullName;
    protected UserRole role;
    protected UserStatus status;

    public User(String userId, String password, String fullName, UserRole role, UserStatus status) {
        this.userId = userId;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    public String getUserId() { return userId; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setStatus(UserStatus status) { this.status = status; }

    public boolean isActive() { return status == UserStatus.ACTIVE; }

    public abstract String toFileString();

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s", userId, password, fullName, role.getLabel(), status.getLabel());
    }
}
