package model;

/**
 * Represents a customer in the ClaimShield health insurance management system.
 * A customer can be either a PolicyHolder (primary account holder) or a Dependent
 * (covered under another PolicyHolder's plan).
 *
 * Tracks total approved claim amount for dynamic tier determination.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class Customer {
    private String id;
    private String fullName;
    private CustomerType customerType;
    private String parentPolicyHolderId;
    private InsuranceCard insuranceCard;
    private double totalApprovedClaimAmount;
    private transient String pendingCardNumber;

    /**
     * Constructs a Customer using the CustomerType enum.
     */
    public Customer(String id, String fullName, CustomerType customerType, String parentPolicyHolderId) {
        this(id, fullName, customerType, parentPolicyHolderId, null, 0.0);
    }

    /**
     * Constructs a Customer with insurance card reference and claim total.
     */
    public Customer(String id, String fullName, CustomerType customerType, String parentPolicyHolderId,
                    InsuranceCard insuranceCard, double totalApprovedClaimAmount) {
        this.id = id;
        this.fullName = fullName;
        this.customerType = customerType;
        this.parentPolicyHolderId = parentPolicyHolderId;
        this.insuranceCard = insuranceCard;
        this.totalApprovedClaimAmount = totalApprovedClaimAmount;
    }

    /**
     * Constructs a Customer using a string customer type label.
     */
    public Customer(String id, String fullName, String customerTypeLabel, String parentPolicyHolderId) {
        this(id, fullName, CustomerType.fromLabel(customerTypeLabel), parentPolicyHolderId);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public CustomerType getCustomerType() { return customerType; }
    public String getCustomerTypeLabel() { return customerType.getLabel(); }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }
    public void setCustomerTypeFromLabel(String label) { this.customerType = CustomerType.fromLabel(label); }

    public String getParentPolicyHolderId() { return parentPolicyHolderId; }
    public void setParentPolicyHolderId(String parentPolicyHolderId) { this.parentPolicyHolderId = parentPolicyHolderId; }

    public InsuranceCard getInsuranceCard() { return insuranceCard; }
    public void setInsuranceCard(InsuranceCard insuranceCard) { this.insuranceCard = insuranceCard; }

    public String getCardNumberForLoading() { return pendingCardNumber; }
    public void setCardNumberForLoading(String cardNumber) { this.pendingCardNumber = cardNumber; }

    public double getTotalApprovedClaimAmount() { return totalApprovedClaimAmount; }
    public void setTotalApprovedClaimAmount(double totalApprovedClaimAmount) {
        this.totalApprovedClaimAmount = totalApprovedClaimAmount;
    }

    /**
     * Adds a completed claim amount to the customer's total approved spending.
     * Used when a claim transitions to DONE status.
     *
     * @param amount the approved claim amount to add
     */
    public void addApprovedClaimAmount(double amount) {
        this.totalApprovedClaimAmount += amount;
    }

    /**
     * Returns the customer's membership tier based on total approved claim spending.
     *
     * @return the dynamically determined MembershipTier
     */
    public MembershipTier getMembershipTier() {
        return MembershipTier.determineTier(totalApprovedClaimAmount);
    }

    public boolean isPolicyHolder() { return customerType == CustomerType.POLICY_HOLDER; }
    public boolean isDependent() { return customerType == CustomerType.DEPENDENT; }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", customerType=" + customerType.getLabel() +
                ", parentPolicyHolderId='" + parentPolicyHolderId + '\'' +
                ", card=" + (insuranceCard != null ? insuranceCard.getCardNumber() : "N/A") +
                ", tier=" + getMembershipTier().getLabel() +
                '}';
    }
}
