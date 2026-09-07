package model;

public class PolicyHolder extends User {
    private String customerId;

    public PolicyHolder(String userId, String password, String fullName, UserStatus status, String customerId) {
        super(userId, password, fullName, UserRole.CUSTOMER, status);
        this.customerId = customerId;
    }

    public String getCustomerId() { return customerId; }

    public static PolicyHolder fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 6) return null;
        return new PolicyHolder(
            parts[0].trim(), parts[1].trim(), parts[2].trim(),
            UserStatus.fromLabel(parts[4].trim()), parts[5].trim()
        );
    }

    @Override
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s|%s", userId, password, fullName, role.getLabel(), status.getLabel(), customerId);
    }
}
