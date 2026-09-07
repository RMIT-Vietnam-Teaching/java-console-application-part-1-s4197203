package model;

public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static UserStatus fromLabel(String label) {
        for (UserStatus status : values()) {
            if (status.label.equalsIgnoreCase(label) || status.name().equalsIgnoreCase(label)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
