package model;

/**
 * Represents a family member covered under a PolicyHolder's plan.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class Dependent extends User {
    private String customerId;
    private String parentPolicyHolderId;

    public Dependent(String userId, String username, String password, String fullName, String email,
                     UserStatus status, String customerId, String parentPolicyHolderId) {
        super(userId, username, password, fullName, email, UserRole.CUSTOMER, status);
        this.customerId = customerId;
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    public String getCustomerId() { return customerId; }
    public String getParentPolicyHolderId() { return parentPolicyHolderId; }

    @Override
    public void displayDashboard() {
        System.out.println("\n=============================================");
        System.out.println("  CUSTOMER DASHBOARD - " + getFullName());
        System.out.println("=============================================");
        System.out.println("  Role:       Dependent");
        System.out.println("  User ID:    " + getUserId());
        System.out.println("  Customer ID:" + customerId);
        System.out.println("  Parent PH:  " + parentPolicyHolderId);
        System.out.println("  Email:      " + getEmail());
        System.out.println("=============================================");
        System.out.println("  Capabilities:");
        System.out.println("    - View Profile, Cards, Claims");
        System.out.println("    - Submit New Claims");
        System.out.println("    - Track Membership Tier Status");
        System.out.println("=============================================");
    }

    public static Dependent fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 8) return null;
        return new Dependent(
            parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
            parts[4].trim(), UserStatus.fromLabel(parts[6].trim()),
            parts[7].trim(), parts.length > 8 ? parts[8].trim() : null
        );
    }

    @Override
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s|%s",
                getUserId(), getUsername(), getPassword(), getFullName(), getEmail(),
                role.getLabel(), status.getLabel(), customerId, parentPolicyHolderId);
    }
}
