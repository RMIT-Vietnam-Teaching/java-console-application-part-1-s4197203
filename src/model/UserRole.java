package model;

public enum UserRole {
    ADMIN("Admin"),
    CLAIMS_OFFICER("ClaimsOfficer"),
    CUSTOMER("Customer");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static UserRole fromLabel(String label) {
        for (UserRole role : values()) {
            if (role.label.equalsIgnoreCase(label) || role.name().equalsIgnoreCase(label)) {
                return role;
            }
        }
        return CUSTOMER;
    }
}
