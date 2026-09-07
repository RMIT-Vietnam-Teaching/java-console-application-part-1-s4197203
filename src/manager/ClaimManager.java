package manager;

import exceptions.InvalidClaimDateException;
import exceptions.InvalidStatusTransitionException;
import interfaces.ClaimManageable;
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
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class ClaimManager implements ClaimManageable {
    private ArrayList<Customer> customers;
    private ArrayList<InsuranceCard> cards;
    private ArrayList<Claim> claims;

    public ClaimManager() {
        customers = new ArrayList<>();
        cards = new ArrayList<>();
        claims = new ArrayList<>();
    }

    public ArrayList<Customer> getCustomers() { return customers; }
    public void setCustomers(ArrayList<Customer> customers) { this.customers = customers; }
    public ArrayList<InsuranceCard> getCards() { return cards; }
    public void setCards(ArrayList<InsuranceCard> cards) { this.cards = cards; }
    public ArrayList<Claim> getClaims() { return claims; }
    public void setClaims(ArrayList<Claim> claims) { this.claims = claims; }

    // ==================== CUSTOMER OPERATIONS ====================

    public String addCustomer(Customer customer) {
        String error = validateCustomer(customer);
        if (error != null) return error;
        customers.add(customer);
        return null;
    }

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

    public String deleteCustomer(String id) {
        Customer c = getCustomerById(id);
        if (c == null) return "Customer not found.";
        cards.removeIf(card -> card.getCardHolderId().equals(id) || card.getPolicyOwnerId().equals(id));
        claims.removeIf(claim -> claim.getInsuredPersonId().equals(id));
        customers.remove(c);
        return null;
    }

    public Customer getCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

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

    public String addCard(InsuranceCard card) {
        String error = validateCard(card);
        if (error != null) return error;
        cards.add(card);
        return null;
    }

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

    public String deleteCard(String cardNumber) {
        InsuranceCard card = getCardByNumber(cardNumber);
        if (card == null) return "Card not found.";
        claims.removeIf(claim -> claim.getCardNumber().equals(cardNumber));
        cards.remove(card);
        return null;
    }

    public InsuranceCard getCardByNumber(String cardNumber) {
        for (InsuranceCard c : cards) {
            if (c.getCardNumber().equals(cardNumber)) return c;
        }
        return null;
    }

    public ArrayList<InsuranceCard> getCardsByCustomerId(String customerId) {
        ArrayList<InsuranceCard> results = new ArrayList<>();
        for (InsuranceCard card : cards) {
            if (card.getCardHolderId().equals(customerId)) results.add(card);
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
        String error = validateClaim(claim);
        if (error != null) return error;
        claims.add(claim);
        return null;
    }

    @Override
    public String updateClaimStatus(String claimId, String newStatusLabel) {
        Claim claim = getClaimById(claimId);
        if (claim == null) return "Claim not found.";
        ClaimStatus newStatus;
        try {
            newStatus = ClaimStatus.fromLabel(newStatusLabel);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        ClaimStatus current = claim.getStatus();
        if (current == ClaimStatus.DONE) return "Cannot change status: claim is already Done.";
        if (!current.canTransitionTo(newStatus)) {
            return "Invalid transition from " + current.getLabel() + " to " + newStatus.getLabel() + ".";
        }
        claim.setStatus(newStatus);
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

    // ==================== CO-PAY CALCULATION ====================

    public double calculateCoPay(Claim claim) {
        InsuranceCard card = getCardByNumber(claim.getCardNumber());
        if (card == null) return claim.getClaimAmount();
        MembershipTier tier = card.getMembershipTier();
        double coverageRate = tier.getCoverageRate();
        double coPayRate = 1.0 - coverageRate;
        return Math.round(claim.getClaimAmount() * coPayRate * 100.0) / 100.0;
    }

    public double calculateCoverageAmount(Claim claim) {
        InsuranceCard card = getCardByNumber(claim.getCardNumber());
        if (card == null) return 0;
        double coverageRate = card.getMembershipTier().getCoverageRate();
        return Math.round(claim.getClaimAmount() * coverageRate * 100.0) / 100.0;
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
        double totalAmount = 0, maxAmount = 0;
        for (Claim c : claims) {
            totalAmount += c.getClaimAmount();
            if (c.getClaimAmount() > maxAmount) maxAmount = c.getClaimAmount();
        }
        double avgAmount = totalClaims > 0 ? totalAmount / totalClaims : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n==================== SYSTEM STATISTICS ====================\n");
        sb.append(String.format("  Customers:       %d total (%d PolicyHolders, %d Dependents)%n", totalCustomers, policyHolders, dependents));
        sb.append(String.format("  Insurance Cards: %d%n", totalCards));
        sb.append(String.format("  Claims:          %d total%n", totalClaims));
        sb.append(String.format("    - New:         %d%n", newClaims));
        sb.append(String.format("    - Processing:  %d%n", processingClaims));
        sb.append(String.format("    - Done:        %d%n", doneClaims));
        sb.append(String.format("  Total Claim Value: $%,.2f%n", totalAmount));
        sb.append(String.format("  Average Claim:     $%,.2f%n", avgAmount));
        sb.append(String.format("  Largest Claim:     $%,.2f%n", maxAmount));
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

    private String validateClaim(Claim claim) {
        if (!claim.getId().matches("f-\\d{10}")) {
            return "Claim ID must be 'f-' followed by exactly 10 digits.";
        }
        if (getClaimById(claim.getId()) != null) return "Claim ID already exists.";
        if (claim.getClaimAmount() <= 0) return "Claim amount must be positive.";
        if (getCustomerById(claim.getInsuredPersonId()) == null) return "Insured person not found.";
        if (getCardByNumber(claim.getCardNumber()) == null) return "Card not found.";
        if (claim.getExamDate().isAfter(claim.getClaimDate())) {
            return "Exam date must be on or before claim date.";
        }
        InsuranceCard card = getCardByNumber(claim.getCardNumber());
        if (card != null && claim.getExamDate().isAfter(card.getExpirationDate())) {
            return "Exam date must be before card expiration.";
        }
        return null;
    }
}
