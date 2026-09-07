package model;

public enum MembershipTier {
    BASIC("Basic", 0.70),
    SILVER("Silver", 0.80),
    GOLD("Gold", 0.90),
    PLATINUM("Platinum", 0.95);

    private final String label;
    private final double coverageRate;

    MembershipTier(String label, double coverageRate) {
        this.label = label;
        this.coverageRate = coverageRate;
    }

    public String getLabel() {
        return label;
    }

    public double getCoverageRate() {
        return coverageRate;
    }

    public static MembershipTier fromLabel(String label) {
        for (MembershipTier tier : values()) {
            if (tier.label.equalsIgnoreCase(label) || tier.name().equalsIgnoreCase(label)) {
                return tier;
            }
        }
        return BASIC;
    }
}
