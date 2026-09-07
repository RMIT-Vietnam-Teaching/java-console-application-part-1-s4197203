package model;

public class Admin extends User {
    public Admin(String userId, String password, String fullName, UserStatus status) {
        super(userId, password, fullName, UserRole.ADMIN, status);
    }

    public static Admin fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) return null;
        return new Admin(
            parts[0].trim(), parts[1].trim(), parts[2].trim(),
            UserStatus.fromLabel(parts[4].trim())
        );
    }

    @Override
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s", userId, password, fullName, role.getLabel(), status.getLabel());
    }
}
