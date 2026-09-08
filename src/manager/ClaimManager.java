package manager;

import exceptions.InvalidClaimDateException;
import exceptions.InvalidStatusTransitionException;
import interfaces.CardManageable;
import interfaces.ClaimManageable;
import interfaces.CustomerManageable;
import model.Claim;
import model.ClaimStatus;
import model.Customer;
import model.CustomerType;
import model.InsuranceCard;
import model.MembershipTier;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Centralized data manager for ClaimShield system.
 * Manages CRUD operations and business rule validation for customers, insurance cards, and claims.
 *
 * Co-pay is calculated dynamically based on customer's membership tier:
 *   Effective Co-Pay Rate = 30% * (1 - tierDiscount)
 *   Customer Co-Pay = claimAmount * effectiveCoPayRate
 *   Insurance Payout = claimAmount - customerCoPay
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class ClaimManager implements ClaimManageable, CardManageable, CustomerManageable {
    private ArrayList<Customer> customers;
    private ArrayList<InsuranceCard> cards;
    private ArrayList<Claim> claims;

    public ClaimManager() {
        customers = new ArrayList<>();
        cards = new ArrayList<>();
        claims = new ArrayList<>();
    }

    @Override
    public ArrayList<Customer> getCustomers() { return customers; }
    public void setCustomers(ArrayList<Customer> customers) { this.customers = customers; }
    @Override
    public ArrayList<InsuranceCard> getCards() { return cards; }
    public void setCards(ArrayList<InsuranceCard> cards) { this.cards = cards; }
    @Override
    public ArrayList<Claim> getClaims() { return claims; }
    public void setClaims(ArrayList<Claim> claims) { this.claims = claims; }

    // ==================== CUSTOMER OPERATIONS ====================

    @Override
    public String addCustomer(Customer customer) {
        String error = validateCustomer(customer);
        if (error != null) return error;
        customers.add(customer);
        return null;
    }

    @Override
    public String updateCustomer(String id, String fullName, String typeLabel, String parentId) {
        Customer c = getCustomerById(id);
        if (c == null) return "Customer not found.";

        String newName = (fullName != null && !fullName.isEmpty()) ? fullName : c.getFullName();
        CustomerType newType;
        try {
            newType = (typeLabel != null && !typeLabel.isEmpty()) ? CustomerType.fromLabel(typeLabel) : c.getCustomerType();
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        String newParent = (parentId != null) ? parentId : c.getParentPolicyHolderId();

        Customer temp = new Customer(id, newName, newType, newParent);
        String error = validateCustomerUpdate(temp);
        if (error != null) return error;

        c.setFullName(newName);
        c.setCustomerType(newType);
        c.setParentPolicyHolderId(newParent);
        return null;
    }

    @Override
    public String deleteCustomer(String id) {
        Customer c = getCustomerById(id);
        if (c == null) return "Customer not found.";
        cards.removeIf(card -> card.getCardHolderId().equals(id) || card.getPolicyOwnerId().equals(id));
        claims.removeIf(claim -> claim.getInsuredPersonId().equals(id));
        customers.remove(c);
        return null;
    }

    @Override
    public Customer getCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    @Override
    public ArrayList<Customer> searchCustomersByName(String keyword) {
        ArrayList<Customer> results = new ArrayList<>();
        String lower = keyword.toLowerCase();
        for (Customer c : customers) {
            if (c.getFullName().toLowerCase().contains(lower)) {
                results.add(c);
            }
        }
        return results;
    }

    @Override
    public ArrayList<Customer> getCustomersByType(CustomerType type) {
        ArrayList<Customer> results = new ArrayList<>();
        for (Customer c : customers) {
            if (c.getCustomerType() == type) results.add(c);
        }
        return results;
    }

    public ArrayList<Customer> getDependentsOf(String policyHolderId) {
        ArrayList<Customer> results = new ArrayList<>();
        for (Customer c : customers) {
            if (c.isDependent() && policyHolderId.equals(c.getParentPolicyHolderId())) {
                results.add(c);
            }
        }
        return results;
    }

    // ==================== INSURANCE CARD OPERATIONS ====================

    @Override
    public String addCard(InsuranceCard card) {
        String error = validateCard(card);
        if (error != null) return error;
        cards.add(card);
        return null;
    }

    @Override
    public String updateCard(String cardNumber, String newHolderId, String newOwnerId, LocalDateTime newExpDate) {
        InsuranceCard card = getCardByNumber(cardNumber);
        if (card == null) return "Card not found.";
        if (newHolderId != null && !newHolderId.isEmpty()) {
            if (getCustomerById(newHolderId) == null) return "Card holder ID not found.";
            card.setCardHolderId(newHolderId);
        }
        if (newOwnerId != null && !newOwnerId.isEmpty()) {
            Customer owner = getCustomerById(newOwnerId);
            if (owner == null) return "Policy owner ID not found.";
            if (!owner.isPolicyHolder()) return "Policy owner must be a PolicyHolder.";
            card.setPolicyOwnerId(newOwnerId);
        }
        if (newExpDate != null) card.setExpirationDate(newExpDate);
        return null;
    }

    @Override
    public String deleteCard(String cardNumber) {
        InsuranceCard card = getCardByNumber(cardNumber);
        if (card == null) return "Card not found.";
        claims.removeIf(claim -> claim.getCardNumber().equals(cardNumber));
        cards.remove(card);
        return null;
    }

    @Override
    public InsuranceCard getCardByNumber(String cardNumber) {
        for (InsuranceCard c : cards) {
            if (c.getCardNumber().equals(cardNumber)) return c;
        }
        return null;
    }

    @Override
    public ArrayList<InsuranceCard> getCardsByCustomerId(String customerId) {
        ArrayList<InsuranceCard> results = new ArrayList<>();
        for (InsuranceCard card : cards) {
            if (card.getCardHolderId().equals(customerId) || card.getPolicyOwnerId().equals(customerId)) {
                results.add(card);
            }
        }
        return results;
    }

    public ArrayList<InsuranceCard> getCardsByPolicyOwner(String policyOwnerId) {
        ArrayList<InsuranceCard> results = new ArrayList<>();
        for (InsuranceCard card : cards) {
            if (card.getPolicyOwnerId().equals(policyOwnerId)) results.add(card);
        }
        return results;
    }

    // ==================== CLAIM OPERATIONS ====================

    @Override
    public String addClaim(Claim claim) {
        try {
            String error = validateClaim(claim);
            if (error != null) return error;
            claims.add(claim);
            return null;
        } catch (InvalidClaimDateException e) {
            return e.getMessage();
        }
    }

    /**
     * Updates claim status with proper validation and exception handling.
     * Throws InvalidStatusTransitionException for illegal state changes.
     * Updates customer's total approved claim amount when claim reaches DONE.
     *
     * @param claimId the claim to update
     * @param newStatus the target status
     * @return null on success, error message string on validation failure
     * @throws InvalidStatusTransitionException if the transition is not allowed
     */
    @Override
    public String updateClaimStatus(String claimId, String newStatusLabel, String processedBy) throws InvalidStatusTransitionException {
        Claim claim = getClaimById(claimId);
        if (claim == null) return "Claim not found.";

        ClaimStatus newStatus;
        try {
            newStatus = ClaimStatus.fromLabel(newStatusLabel);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

        ClaimStatus current = claim.getStatus();

        if (current == ClaimStatus.DONE) {
            throw new InvalidStatusTransitionException(
                "Cannot change status: claim is already Done.");
        }

        if (!current.canTransitionTo(newStatus)) {
            throw new InvalidStatusTransitionException(
                "Invalid transition from " + current.getLabel() + " to " + newStatus.getLabel() +
                ". Only forward progression is allowed: New -> Processing -> Done.");
        }

        claim.setStatus(newStatus);
        if (processedBy != null) claim.setProcessedBy(processedBy);

        if (newStatus == ClaimStatus.DONE) {
            Customer customer = getCustomerById(claim.getInsuredPersonId());
            if (customer != null) {
                customer.addApprovedClaimAmount(claim.getClaimAmount());
            }
        }

        return null;
    }

    @Override
    public String addDocumentToClaim(String claimId, String documentName) {
        Claim claim = getClaimById(claimId);
        if (claim == null) return "Claim not found.";
        String expectedPrefix = claimId + "_" + claim.getCardNumber() + "_";
        if (!documentName.startsWith(expectedPrefix)) return "Document name must start with: " + expectedPrefix;
        if (!documentName.endsWith(".pdf")) return "Document name must end with .pdf";
        claim.getDocuments().add(documentName);
        return null;
    }

    @Override
    public void deleteClaim(String claimId) {
        Claim claim = getClaimById(claimId);
        if (claim != null) claims.remove(claim);
    }

    @Override
    public Claim getClaimById(String id) {
        for (Claim c : claims) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    @Override
    public ArrayList<Claim> getClaimsByStatus(ClaimStatus status) {
        ArrayList<Claim> results = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getStatus() == status) results.add(c);
        }
        return results;
    }

    public ArrayList<Claim> getClaimsByCustomerId(String customerId) {
        ArrayList<Claim> results = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getInsuredPersonId().equals(customerId)) results.add(c);
        }
        return results;
    }

    public ArrayList<Claim> getClaimsSortedByDate() {
        ArrayList<Claim> sorted = new ArrayList<>(claims);
        sorted.sort((a, b) -> b.getClaimDate().compareTo(a.getClaimDate()));
        return sorted;
    }

    public ArrayList<Claim> getClaimsSortedByAmount() {
        ArrayList<Claim> sorted = new ArrayList<>(claims);
        sorted.sort((a, b) -> Double.compare(b.getClaimAmount(), a.getClaimAmount()));
        return sorted;
    }

    public ArrayList<Claim> getClaimsByDateRange(LocalDateTime start, LocalDateTime end) {
        ArrayList<Claim> results = new ArrayList<>();
        for (Claim c : claims) {
            if (!c.getClaimDate().isBefore(start) && !c.getClaimDate().isAfter(end)) {
                results.add(c);
            }
        }
        return results;
    }

    public ArrayList<Claim> getDoneClaimsByDateRange(LocalDateTime start, LocalDateTime end) {
        ArrayList<Claim> results = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getStatus() == ClaimStatus.DONE &&
                !c.getClaimDate().isBefore(start) && !c.getClaimDate().isAfter(end)) {
                results.add(c);
            }
        }
        return results;
    }

    public ArrayList<Claim> getClaimsByPolicyHolderFamily(String policyHolderId) {
        ArrayList<Claim> results = new ArrayList<>();
        Customer ph = getCustomerById(policyHolderId);
        if (ph == null || !ph.isPolicyHolder()) return results;

        ArrayList<String> familyIds = new ArrayList<>();
        familyIds.add(policyHolderId);
        for (Customer dep : getDependentsOf(policyHolderId)) {
            familyIds.add(dep.getId());
        }

        for (Claim c : claims) {
            if (familyIds.contains(c.getInsuredPersonId())) {
                results.add(c);
            }
        }
        return results;
    }

    // ==================== CO-PAY & TIER CALCULATIONS ====================

    /**
     * Calculates the customer's co-pay amount based on their membership tier.
     * Tier is dynamically determined by total approved claim spending.
     *
     * @param claim the claim to calculate co-pay for
     * @return the customer co-pay amount
     */
    public double calculateCoPay(Claim claim) {
        Customer customer = getCustomerById(claim.getInsuredPersonId());
        if (customer == null) return claim.getClaimAmount();
        MembershipTier tier = customer.getMembershipTier();
        return Math.round(claim.getClaimAmount() * tier.getEffectiveCoPayRate() * 100.0) / 100.0;
    }

    /**
     * Calculates the insurance payout amount based on customer's membership tier.
     *
     * @param claim the claim to calculate payout for
     * @return the insurance payout amount
     */
    public double calculateInsurancePayout(Claim claim) {
        double coPay = calculateCoPay(claim);
        return Math.round((claim.getClaimAmount() - coPay) * 100.0) / 100.0;
    }

    /**
     * Returns the customer's effective co-pay rate based on their membership tier.
     *
     * @param customerId the customer ID
     * @return the effective co-pay rate as a percentage (e.g., 0.30 for 30%)
     */
    public double getEffectiveCoPayRate(String customerId) {
        Customer customer = getCustomerById(customerId);
        if (customer == null) return MembershipTier.STANDARD.getEffectiveCoPayRate();
        return customer.getMembershipTier().getEffectiveCoPayRate();
    }

    // ==================== STATISTICS ====================

    public String getSystemStatistics() {
        int totalCustomers = customers.size();
        int policyHolders = 0, dependents = 0;
        for (Customer c : customers) {
            if (c.isPolicyHolder()) policyHolders++;
            else dependents++;
        }
        int totalCards = cards.size();
        int totalClaims = claims.size();
        int newClaims = getClaimsByStatus(ClaimStatus.NEW).size();
        int processingClaims = getClaimsByStatus(ClaimStatus.PROCESSING).size();
        int doneClaims = getClaimsByStatus(ClaimStatus.DONE).size();
        double totalAmount = 0, maxAmount = 0, totalCoPay = 0, totalPayout = 0;
        for (Claim c : claims) {
            totalAmount += c.getClaimAmount();
            if (c.getClaimAmount() > maxAmount) maxAmount = c.getClaimAmount();
            if (c.getStatus() == ClaimStatus.DONE) {
                totalCoPay += calculateCoPay(c);
                totalPayout += calculateInsurancePayout(c);
            }
        }
        double avgAmount = totalClaims > 0 ? totalAmount / totalClaims : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n==================== SYSTEM STATISTICS ====================\n");
        sb.append(String.format("  Customers:          %d total (%d PolicyHolders, %d Dependents)%n",
                totalCustomers, policyHolders, dependents));
        sb.append(String.format("  Insurance Cards:    %d%n", totalCards));
        sb.append(String.format("  Claims:             %d total%n", totalClaims));
        sb.append(String.format("    - New:            %d%n", newClaims));
        sb.append(String.format("    - Processing:     %d%n", processingClaims));
        sb.append(String.format("    - Done:           %d%n", doneClaims));
        sb.append(String.format("  Total Claim Value:  $%,.2f%n", totalAmount));
        sb.append(String.format("  Average Claim:      $%,.2f%n", avgAmount));
        sb.append(String.format("  Largest Claim:      $%,.2f%n", maxAmount));
        sb.append(String.format("  Total Co-Pay (Done):  $%,.2f%n", totalCoPay));
        sb.append(String.format("  Total Payout (Done):  $%,.2f%n", totalPayout));
        sb.append("============================================================");
        return sb.toString();
    }

    // ==================== VALIDATION ====================

    private String validateCustomer(Customer c) {
        if (!c.getId().matches("c-\\d{7}")) {
            return "Customer ID must be 'c-' followed by exactly 7 digits.";
        }
        if (getCustomerById(c.getId()) != null) {
            return "Customer ID " + c.getId() + " already exists.";
        }
        if (c.isPolicyHolder() && c.getParentPolicyHolderId() != null) {
            return "PolicyHolder must not have a parent policy holder ID.";
        }
        if (c.isDependent()) {
            if (c.getParentPolicyHolderId() == null || c.getParentPolicyHolderId().isEmpty()) {
                return "Dependent must have a parent policy holder ID.";
            }
            Customer parent = getCustomerById(c.getParentPolicyHolderId());
            if (parent == null) return "Parent policy holder " + c.getParentPolicyHolderId() + " not found.";
            if (!parent.isPolicyHolder()) return "Parent must be a PolicyHolder.";
        }
        return null;
    }

    private String validateCustomerUpdate(Customer c) {
        if (c.isPolicyHolder() && c.getParentPolicyHolderId() != null) {
            return "PolicyHolder must not have a parent policy holder ID.";
        }
        if (c.isDependent()) {
            if (c.getParentPolicyHolderId() == null || c.getParentPolicyHolderId().isEmpty()) {
                return "Dependent must have a parent policy holder ID.";
            }
            Customer parent = getCustomerById(c.getParentPolicyHolderId());
            if (parent == null) return "Parent policy holder " + c.getParentPolicyHolderId() + " not found.";
            if (!parent.isPolicyHolder()) return "Parent must be a PolicyHolder.";
        }
        return null;
    }

    private String validateCard(InsuranceCard card) {
        if (!card.getCardNumber().matches("\\d{10}")) return "Card number must be exactly 10 digits.";
        if (getCardByNumber(card.getCardNumber()) != null) return "Card number already exists.";
        if (getCustomerById(card.getCardHolderId()) == null) return "Card holder ID not found.";
        Customer owner = getCustomerById(card.getPolicyOwnerId());
        if (owner == null) return "Policy owner ID not found.";
        if (!owner.isPolicyHolder()) return "Policy owner must be a PolicyHolder.";
        return null;
    }

    /**
     * Validates a new claim, throwing InvalidClaimDateException for date violations.
     *
     * @param claim the claim to validate
     * @return null on success, error message on validation failure
     * @throws InvalidClaimDateException if exam date is after claim date or after card expiration
     */
    private String validateClaim(Claim claim) throws InvalidClaimDateException {
        if (!claim.getId().matches("f-\\d{10}")) {
            return "Claim ID must be 'f-' followed by exactly 10 digits.";
        }
        if (getClaimById(claim.getId()) != null) return "Claim ID already exists.";
        if (claim.getClaimAmount() <= 0) return "Claim amount must be positive.";
        if (getCustomerById(claim.getInsuredPersonId()) == null) return "Insured person not found.";
        InsuranceCard card = getCardByNumber(claim.getCardNumber());
        if (card == null) return "Card not found.";

        if (claim.getExamDate().isAfter(claim.getClaimDate())) {
            throw new InvalidClaimDateException(
                "Exam date (" + claim.getExamDate() + ") must be on or before claim date (" + claim.getClaimDate() + ").");
        }

        if (claim.getExamDate().isAfter(card.getExpirationDate())) {
            throw new InvalidClaimDateException(
                "Exam date (" + claim.getExamDate() + ") must be before card expiration (" + card.getExpirationDate() + ").");
        }
        return null;
    }
}
