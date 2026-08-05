package model;

/**
 * Represents a customer in the ClaimShield health insurance management system.
 * A customer can be either a PolicyHolder (primary account holder) or a Dependent
 * (covered under another PolicyHolder's plan).
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class Customer {
    private String id;
    private String fullName;
    private CustomerType customerType;
    private String parentPolicyHolderId;

    /**
     * Constructs a Customer using the CustomerType enum.
     *
     * @param id                   unique customer ID (format: c-XXXXXXX)
     * @param fullName             the customer's full name
     * @param customerType         the customer's type (POLICY_HOLDER or DEPENDENT)
     * @param parentPolicyHolderId the parent policy holder ID (null for PolicyHolders)
     */
    public Customer(String id, String fullName, CustomerType customerType, String parentPolicyHolderId) {
        this.id = id;
        this.fullName = fullName;
        this.customerType = customerType;
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    /**
     * Constructs a Customer using a string customer type label.
     *
     * @param id                   unique customer ID (format: c-XXXXXXX)
     * @param fullName             the customer's full name
     * @param customerTypeLabel    string label: "PolicyHolder" or "Dependent"
     * @param parentPolicyHolderId the parent policy holder ID (null for PolicyHolders)
     */
    public Customer(String id, String fullName, String customerTypeLabel, String parentPolicyHolderId) {
        this(id, fullName, CustomerType.fromLabel(customerTypeLabel), parentPolicyHolderId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public String getCustomerTypeLabel() {
        return customerType.getLabel();
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }

    public void setCustomerTypeFromLabel(String label) {
        this.customerType = CustomerType.fromLabel(label);
    }

    public String getParentPolicyHolderId() {
        return parentPolicyHolderId;
    }

    public void setParentPolicyHolderId(String parentPolicyHolderId) {
        this.parentPolicyHolderId = parentPolicyHolderId;
    }

    public boolean isPolicyHolder() {
        return customerType == CustomerType.POLICY_HOLDER;
    }

    public boolean isDependent() {
        return customerType == CustomerType.DEPENDENT;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", customerType=" + customerType.getLabel() +
                ", parentPolicyHolderId='" + parentPolicyHolderId + '\'' +
                '}';
    }
}
