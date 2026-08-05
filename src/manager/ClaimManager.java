package manager;

import model.Claim;
import model.ClaimStatus;
import model.Customer;
import model.CustomerType;
import model.InsuranceCard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized data manager for ClaimShield system.
 * Manages CRUD operations and business rule validation for customers, insurance cards, and claims.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class ClaimManager {
    private ArrayList<Customer> customers;
    private ArrayList<InsuranceCard> cards;
    private ArrayList<Claim> claims;

    public ClaimManager() {
        customers = new ArrayList<>();
        cards = new ArrayList<>();
        claims = new ArrayList<>();
    }

    public ArrayList<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public ArrayList<InsuranceCard> getCards() {
        return cards;
    }

    public void setCards(ArrayList<InsuranceCard> cards) {
        this.cards = cards;
    }

    public ArrayList<Claim> getClaims() {
        return claims;
    }

    public void setClaims(ArrayList<Claim> claims) {
        this.claims = claims;
    }

    // ==================== CUSTOMER OPERATIONS ====================

    /**
     * Adds a new customer after validating uniqueness, ID format, and type rules.
     *
     * @param customer the customer to add
     * @return error message or null if successful
     */
    public String addCustomer(Customer customer) {
        String error = validateCustomer(customer);
        if (error != null) return error;
        customers.add(customer);
        return null;
    }

    /**
     * Updates an existing customer's details. Null or empty values retain current data.
     *
     * @param id         the customer ID to update
     * @param fullName   new full name (or null to keep)
     * @param typeLabel  new customer type label (or null to keep)
     * @param parentId   new parent policy holder ID (or null to keep)
     * @return error message or null if successful
     */
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

    /**
     * Deletes a customer and cascades removal to associated cards and claims.
     *
     * @param id the customer ID to delete
     * @return error message or null if successful
     */
    public String deleteCustomer(String id) {
        Customer c = getCustomerById(id);
        if (c == null) return "Customer not found.";

        cards.removeIf(card -> card.getCardHolderId().equals(id) || card.getPolicyOwnerId().equals(id));
        claims.removeIf(claim -> claim.getInsuredPersonId().equals(id));
        customers.remove(c);
        return null;
    }

    /**
     * Finds a customer by their exact ID.
     *
     * @param id the customer ID
     * @return the matching Customer or null
     */
    public Customer getCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    /**
     * Searches customers by name (case-insensitive partial match).
     *
     * @param keyword the search term
     * @return list of matching customers
     */
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

    /**
     * Filters customers by type.
     *
     * @param type the customer type to filter by
     * @return list of matching customers
     */
    public ArrayList<Customer> getCustomersByType(CustomerType type) {
        ArrayList<Customer> results = new ArrayList<>();
        for (Customer c : customers) {
            if (c.getCustomerType() == type) {
                results.add(c);
            }
        }
        return results;
    }

    /**
     * Gets all dependents for a given policy holder.
     *
     * @param policyHolderId the policy holder's ID
     * @return list of dependent customers
     */
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

    /**
     * Adds an insurance card after validating uniqueness, existence of referenced customers, and format.
     *
     * @param card the card to add
     * @return error message or null if successful
     */
    public String addCard(InsuranceCard card) {
        String error = validateCard(card);
        if (error != null) return error;
        cards.add(card);
        return null;
    }

    /**
     * Updates an existing card's details. Null or empty values retain current data.
     */
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
        if (newExpDate != null) {
            card.setExpirationDate(newExpDate);
        }
        return null;
    }

    /**
     * Deletes a card and cascades removal to associated claims.
     *
     * @param cardNumber the card number to delete
     * @return error message or null if successful
     */
    public String deleteCard(String cardNumber) {
        InsuranceCard card = getCardByNumber(cardNumber);
        if (card == null) return "Card not found.";
        claims.removeIf(claim -> claim.getCardNumber().equals(cardNumber));
        cards.remove(card);
        return null;
    }

    /**
     * Finds a card by its number.
     *
     * @param cardNumber the card number
     * @return the matching InsuranceCard or null
     */
    public InsuranceCard getCardByNumber(String cardNumber) {
        for (InsuranceCard c : cards) {
            if (c.getCardNumber().equals(cardNumber)) return c;
        }
        return null;
    }

    /**
     * Gets all cards belonging to a specific customer.
     *
     * @param customerId the customer ID
     * @return list of cards for that customer
     */
    public ArrayList<InsuranceCard> getCardsByCustomerId(String customerId) {
        ArrayList<InsuranceCard> results = new ArrayList<>();
        for (InsuranceCard card : cards) {
            if (card.getCardHolderId().equals(customerId)) {
                results.add(card);
            }
        }
        return results;
    }

    /**
     * Gets all cards owned by a specific policy holder.
     *
     * @param policyOwnerId the policy holder ID
     * @return list of cards under that policy
     */
    public ArrayList<InsuranceCard> getCardsByPolicyOwner(String policyOwnerId) {
        ArrayList<InsuranceCard> results = new ArrayList<>();
        for (InsuranceCard card : cards) {
            if (card.getPolicyOwnerId().equals(policyOwnerId)) {
                results.add(card);
            }
        }
        return results;
    }

    // ==================== CLAIM OPERATIONS ====================

    /**
     * Adds a claim after validating ID format, uniqueness, dates, amount, and card validity.
     *
     * @param claim the claim to add
     * @return error message or null if successful
     */
    public String addClaim(Claim claim) {
        String error = validateClaim(claim);
        if (error != null) return error;
        claims.add(claim);
        return null;
    }

    /**
     * Updates a claim's status with forward-only transition validation.
     *
     * @param claimId   the claim ID
     * @param statusLabel the new status label
     * @return error message or null if successful
     */
    public String updateClaimStatus(String claimId, String statusLabel) {
        Claim claim = getClaimById(claimId);
        if (claim == null) return "Claim not found.";

        ClaimStatus newStatus;
        try {
            newStatus = ClaimStatus.fromLabel(statusLabel);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }

        ClaimStatus current = claim.getStatus();

        if (current == ClaimStatus.DONE) {
            return "Cannot change status: claim is already Done.";
        }
        if (!current.canTransitionTo(newStatus)) {
            if (newStatus == ClaimStatus.NEW) {
                return "Cannot move status backward from " + current.getLabel() + " to New.";
            }
            return "Invalid transition from " + current.getLabel() + " to " + newStatus.getLabel() +
                    ". Must go through Processing first.";
        }

        claim.setStatus(newStatus);
        return null;
    }

    /**
     * Adds a document to a claim, validating the naming format.
     *
     * @param claimId      the claim ID
     * @param documentName the document name to add
     * @return error message or null if successful
     */
    public String addDocumentToClaim(String claimId, String documentName) {
        Claim claim = getClaimById(claimId);
        if (claim == null) return "Claim not found.";

        String expectedPrefix = claimId + "_" + claim.getCardNumber() + "_";
        if (!documentName.startsWith(expectedPrefix)) {
            return "Document name must start with: " + expectedPrefix;
        }
        if (!documentName.endsWith(".pdf")) {
            return "Document name must end with .pdf";
        }
        claim.getDocuments().add(documentName);
        return null;
    }

    /**
     * Deletes a claim by ID.
     *
     * @param claimId the claim ID to delete
     * @return error message or null if successful
     */
    public String deleteClaim(String claimId) {
        Claim claim = getClaimById(claimId);
        if (claim == null) return "Claim not found.";
        claims.remove(claim);
        return null;
    }

    /**
     * Finds a claim by its exact ID.
     *
     * @param id the claim ID
     * @return the matching Claim or null
     */
    public Claim getClaimById(String id) {
        for (Claim c : claims) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    /**
     * Filters claims by their processing status.
     *
     * @param status the status to filter by
     * @return list of matching claims
     */
    public ArrayList<Claim> getClaimsByStatus(ClaimStatus status) {
        ArrayList<Claim> results = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getStatus() == status) {
                results.add(c);
            }
        }
        return results;
    }

    /**
     * Gets all claims for a specific customer.
     *
     * @param customerId the customer ID
     * @return list of claims for that customer
     */
    public ArrayList<Claim> getClaimsByCustomerId(String customerId) {
        ArrayList<Claim> results = new ArrayList<>();
        for (Claim c : claims) {
            if (c.getInsuredPersonId().equals(customerId)) {
                results.add(c);
            }
        }
        return results;
    }

    /**
     * Gets claims sorted by claim date (newest first).
     */
    public ArrayList<Claim> getClaimsSortedByDate() {
        ArrayList<Claim> sorted = new ArrayList<>(claims);
        sorted.sort((a, b) -> b.getClaimDate().compareTo(a.getClaimDate()));
        return sorted;
    }

    /**
     * Gets claims sorted by amount (highest first).
     */
    public ArrayList<Claim> getClaimsSortedByAmount() {
        ArrayList<Claim> sorted = new ArrayList<>(claims);
        sorted.sort((a, b) -> Double.compare(b.getClaimAmount(), a.getClaimAmount()));
        return sorted;
    }

    // ==================== STATISTICS ====================

    /**
     * Computes summary statistics for the claim database.
     */
    public String getSystemStatistics() {
        int totalCustomers = customers.size();
        int policyHolders = 0;
        int dependents = 0;
        for (Customer c : customers) {
            if (c.isPolicyHolder()) policyHolders++;
            else dependents++;
        }

        int totalCards = cards.size();
        int totalClaims = claims.size();

        int newClaims = getClaimsByStatus(ClaimStatus.NEW).size();
        int processingClaims = getClaimsByStatus(ClaimStatus.PROCESSING).size();
        int doneClaims = getClaimsByStatus(ClaimStatus.DONE).size();

        double totalAmount = 0;
        double maxAmount = 0;
        for (Claim c : claims) {
            totalAmount += c.getClaimAmount();
            if (c.getClaimAmount() > maxAmount) maxAmount = c.getClaimAmount();
        }
        double avgAmount = totalClaims > 0 ? totalAmount / totalClaims : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n==================== SYSTEM STATISTICS ====================\n");
        sb.append(String.format("  Customers:      %d total (%d PolicyHolders, %d Dependents)%n",
                totalCustomers, policyHolders, dependents));
        sb.append(String.format("  Insurance Cards: %d%n", totalCards));
        sb.append(String.format("  Claims:          %d total%n", totalClaims));
        sb.append(String.format("    - New:          %d%n", newClaims));
        sb.append(String.format("    - Processing:   %d%n", processingClaims));
        sb.append(String.format("    - Done:         %d%n", doneClaims));
        sb.append(String.format("  Total Claim Value: $%.2f%n", totalAmount));
        sb.append(String.format("  Average Claim:     $%.2f%n", avgAmount));
        sb.append(String.format("  Largest Claim:     $%.2f%n", maxAmount));
        sb.append("============================================================");
        return sb.toString();
    }

    // ==================== VALIDATION ====================

    private String validateCustomer(Customer c) {
        if (!c.getId().matches("c-\\d{7}")) {
            return "Customer ID must start with 'c-' followed by exactly 7 digits (e.g., c-1234567).";
        }
        if (getCustomerById(c.getId()) != null) {
            return "Customer ID " + c.getId() + " already exists.";
        }

        if (c.isPolicyHolder() && c.getParentPolicyHolderId() != null) {
            return "PolicyHolder must not have a parent policy holder ID. Leave it empty.";
        }
        if (c.isDependent()) {
            if (c.getParentPolicyHolderId() == null || c.getParentPolicyHolderId().isEmpty()) {
                return "Dependent must have a parent policy holder ID.";
            }
            Customer parent = getCustomerById(c.getParentPolicyHolderId());
            if (parent == null) {
                return "Parent policy holder ID " + c.getParentPolicyHolderId() + " not found in system.";
            }
            if (!parent.isPolicyHolder()) {
                return "Parent must be a PolicyHolder, but " + c.getParentPolicyHolderId() + " is a " +
                        parent.getCustomerTypeLabel() + ".";
            }
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
            if (parent == null) {
                return "Parent policy holder ID " + c.getParentPolicyHolderId() + " not found.";
            }
            if (!parent.isPolicyHolder()) {
                return "Parent must be a PolicyHolder.";
            }
        }
        return null;
    }

    private String validateCard(InsuranceCard card) {
        if (!card.getCardNumber().matches("\\d{10}")) {
            return "Card number must be exactly 10 digits.";
        }
        if (getCardByNumber(card.getCardNumber()) != null) {
            return "Card number " + card.getCardNumber() + " already exists.";
        }
        Customer holder = getCustomerById(card.getCardHolderId());
        if (holder == null) {
            return "Card holder ID " + card.getCardHolderId() + " not found in system.";
        }
        Customer owner = getCustomerById(card.getPolicyOwnerId());
        if (owner == null) {
            return "Policy owner ID " + card.getPolicyOwnerId() + " not found in system.";
        }
        if (!owner.isPolicyHolder()) {
            return "Policy owner " + card.getPolicyOwnerId() + " must be a PolicyHolder.";
        }
        return null;
    }

    private String validateClaim(Claim claim) {
        if (!claim.getId().matches("f-\\d{10}")) {
            return "Claim ID must start with 'f-' followed by exactly 10 digits (e.g., f-1234567890).";
        }
        if (getClaimById(claim.getId()) != null) {
            return "Claim ID " + claim.getId() + " already exists.";
        }
        if (claim.getClaimAmount() <= 0) {
            return "Claim amount must be a positive number greater than zero.";
        }
        if (getCustomerById(claim.getInsuredPersonId()) == null) {
            return "Insured person ID " + claim.getInsuredPersonId() + " not found in system.";
        }
        InsuranceCard card = getCardByNumber(claim.getCardNumber());
        if (card == null) {
            return "Card number " + claim.getCardNumber() + " not found in system.";
        }
        if (claim.getExamDate().isAfter(claim.getClaimDate())) {
            return "Exam date (" + claim.getExamDate() + ") must occur before or on the same day as the claim date (" +
                    claim.getClaimDate() + ").";
        }
        if (claim.getExamDate().isAfter(card.getExpirationDate())) {
            return "Exam date (" + claim.getExamDate() + ") must occur before the card's expiration date (" +
                    card.getExpirationDate() + ").";
        }
        return null;
    }
}
