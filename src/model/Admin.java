package model;

/**
 * Administrator user with full system access.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class Admin extends User {
    public Admin(String userId, String username, String password, String fullName, String email, UserStatus status) {
        super(userId, username, password, fullName, email, UserRole.ADMIN, status);
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n=============================================");
        System.out.println("  ADMIN DASHBOARD - " + getFullName());
        System.out.println("=============================================");
        System.out.println("  Role:       Administrator");
        System.out.println("  User ID:    " + getUserId());
        System.out.println("  Email:      " + getEmail());
        System.out.println("  Status:     " + getStatus().getLabel());
        System.out.println("=============================================");
        System.out.println("  Capabilities:");
        System.out.println("    - Manage Users, Customers, Cards, Claims");
        System.out.println("    - View System Statistics & Reports");
        System.out.println("    - View Activity Logs");
        System.out.println("    - Search & Filter Records");
        System.out.println("=============================================");
    }

    public static Admin fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 7) return null;
        return new Admin(
            parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
            parts[4].trim(), UserStatus.fromLabel(parts[6].trim())
        );
    }

    @Override
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s|%s|%s",
                getUserId(), getUsername(), getPassword(), getFullName(),
                getEmail(), role.getLabel(), status.getLabel());
    }
}
