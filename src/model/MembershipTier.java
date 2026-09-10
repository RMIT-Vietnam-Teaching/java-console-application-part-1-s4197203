package model;

/**
 * Membership tiers determined dynamically by customer's total approved claim spending.
 * Each tier provides a discount on the base 30% co-pay rate.
 *
 * Co-pay calculation:
 *   Effective Co-Pay Rate = 30% * (1 - tierDiscount)
 *   Customer Co-Pay = claimAmount * effectiveCoPayRate
 *   Insurance Payout = claimAmount - customerCoPay
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public enum MembershipTier {
    STANDARD("Standard", 0.00),
    SILVER("Silver", 0.05),
    GOLD("Gold", 0.10),
    PLATINUM("Platinum", 0.15);

    private final String label;
    private final double tierDiscount;

    /** Base co-pay rate applied to all claims before any tier discount. */
    public static final double BASE_COPAY_RATE = 0.30;

    MembershipTier(String label, double tierDiscount) {
        this.label = label;
        this.tierDiscount = tierDiscount;
    }

    public String getLabel() { return label; }
    public double getTierDiscount() { return tierDiscount; }

    /**
     * Effective co-pay rate derived dynamically from the base rate and tier discount.
     * Effective Co-Pay Rate = BASE_COPAY_RATE * (1 - tierDiscount).
     *
     * @return the effective co-pay rate (e.g., 0.285 for Silver)
     */
    public double getEffectiveCoPayRate() {
        return Math.round(BASE_COPAY_RATE * (1.0 - tierDiscount) * 1000.0) / 1000.0;
    }

    /**
     * Determines the membership tier based on total approved claim spending.
     *
     * @param totalApprovedSpending sum of all DONE claims for a customer
     * @return the applicable MembershipTier
     */
    public static MembershipTier determineTier(double totalApprovedSpending) {
        if (totalApprovedSpending >= 10000) return PLATINUM;
        if (totalApprovedSpending >= 5000) return GOLD;
        if (totalApprovedSpending >= 2000) return SILVER;
        return STANDARD;
    }

    public static MembershipTier fromLabel(String label) {
        for (MembershipTier tier : values()) {
            if (tier.label.equalsIgnoreCase(label) || tier.name().equalsIgnoreCase(label)) {
                return tier;
            }
        }
        return STANDARD;
    }
}
