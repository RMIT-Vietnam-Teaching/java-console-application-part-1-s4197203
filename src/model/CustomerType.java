package model;

/**
 * Represents the type of customer in the ClaimShield system.
 * PolicyHolder: Primary account holder who pays for the insurance plan.
 * Dependent: Individual covered under a PolicyHolder's plan.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public enum CustomerType {
    POLICY_HOLDER("PolicyHolder"),
    DEPENDENT("Dependent");

    private final String label;

    CustomerType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Converts a string label to the corresponding enum value (case-insensitive).
     *
     * @param label the string value to convert
     * @return the matching CustomerType enum
     * @throws IllegalArgumentException if the label does not match any type
     */
    public static CustomerType fromLabel(String label) {
        for (CustomerType type : values()) {
            if (type.label.equalsIgnoreCase(label.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid customer type: " + label + ". Must be 'PolicyHolder' or 'Dependent'.");
    }
}
