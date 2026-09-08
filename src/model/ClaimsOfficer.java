package model;

/**
 * Claims Officer user who processes and manages insurance claims.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class ClaimsOfficer extends User {
    public ClaimsOfficer(String userId, String username, String password, String fullName, String email, UserStatus status) {
        super(userId, username, password, fullName, email, UserRole.CLAIMS_OFFICER, status);
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n=============================================");
        System.out.println("  CLAIMS OFFICER DASHBOARD - " + getFullName());
        System.out.println("=============================================");
        System.out.println("  Role:       Claims Officer");
        System.out.println("  User ID:    " + getUserId());
        System.out.println("  Email:      " + getEmail());
        System.out.println("  Status:     " + getStatus().getLabel());
        System.out.println("=============================================");
        System.out.println("  Capabilities:");
        System.out.println("    - View & Process Claims");
        System.out.println("    - Add Documents to Claims");
        System.out.println("    - View Customers & Cards");
        System.out.println("    - Search & Filter Records");
        System.out.println("=============================================");
    }

    public static ClaimsOfficer fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 7) return null;
        return new ClaimsOfficer(
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
