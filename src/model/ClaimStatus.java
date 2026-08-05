package model;

/**
 * Represents the processing status of an insurance claim.
 * Statuses progress forward only: NEW -> PROCESSING -> DONE.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public enum ClaimStatus {
    NEW("New"),
    PROCESSING("Processing"),
    DONE("Done");

    private final String label;

    ClaimStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Determines if transitioning from this status to the target status is valid.
     * Only forward progression is allowed: NEW -> PROCESSING -> DONE.
     *
     * @param target the desired next status
     * @return true if the transition is valid
     */
    public boolean canTransitionTo(ClaimStatus target) {
        return target.ordinal() == this.ordinal() + 1;
    }

    /**
     * Converts a string label to the corresponding enum value (case-insensitive).
     *
     * @param label the string value to convert
     * @return the matching ClaimStatus enum
     * @throws IllegalArgumentException if the label does not match any status
     */
    public static ClaimStatus fromLabel(String label) {
        for (ClaimStatus status : values()) {
            if (status.label.equalsIgnoreCase(label.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status: " + label + ". Must be 'New', 'Processing', or 'Done'.");
    }
}
