package model;

public class Dependent extends User {
    private String customerId;
    private String parentPolicyHolderId;

    public Dependent(String userId, String password, String fullName, UserStatus status,
                     String customerId, String parentPolicyHolderId) {
        super(userId, password, fullName, UserRole.CUSTOMER, status);
        this.customerId = customerId;
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    public String getCustomerId() { return customerId; }
    public String getParentPolicyHolderId() { return parentPolicyHolderId; }

    public static Dependent fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 7) return null;
        return new Dependent(
            parts[0].trim(), parts[1].trim(), parts[2].trim(),
            UserStatus.fromLabel(parts[4].trim()), parts[5].trim(), parts[6].trim()
        );
    }

    @Override
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s|%s|%s", userId, password, fullName, role.getLabel(),
                status.getLabel(), customerId, parentPolicyHolderId);
    }
}
