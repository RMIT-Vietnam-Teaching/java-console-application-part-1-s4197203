package ui;

import manager.ClaimManager;
import manager.FileManager;
import model.Claim;
import model.ClaimStatus;
import model.Customer;
import model.CustomerType;
import model.InsuranceCard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Interactive console-based user interface for the ClaimShield system.
 * Provides menu-driven navigation for managing customers, insurance cards,
 * claims, displaying system statistics, and saving/loading data.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class ConsoleUI {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Scanner scanner;
    private InputHelper input;
    private ClaimManager manager;
    private FileManager fileManager;
    private String dataDir;

    public ConsoleUI(ClaimManager manager, String dataDir) {
        this.scanner = new Scanner(System.in);
        this.input = new InputHelper(scanner);
        this.manager = manager;
        this.fileManager = new FileManager();
        this.dataDir = dataDir;
    }

    public void run() {
        while (true) {
            printMainMenu();
            int choice = input.promptInt("Choose an option: ", 1, 6);
            switch (choice) {
                case 1: manageCustomers(); break;
                case 2: manageCards(); break;
                case 3: manageClaims(); break;
                case 4: showStatistics(); break;
                case 5: searchRecords(); break;
                case 6: saveAndExit(); return;
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n========================================");
        System.out.println("        ClaimShield - Main Menu");
        System.out.println("========================================");
        System.out.println("  1. Manage Customer Directory");
        System.out.println("  2. Manage Insurance Cards");
        System.out.println("  3. Process Claims");
        System.out.println("  4. View System Statistics");
        System.out.println("  5. Search & Filter Records");
        System.out.println("  6. Save and Exit");
        System.out.println("========================================");
    }

    // ==================== STATISTICS ====================

    private void showStatistics() {
        System.out.println(manager.getSystemStatistics());
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ==================== SEARCH ====================

    private void searchRecords() {
        while (true) {
            System.out.println("\n--- Search & Filter Records ---");
            System.out.println("  1. Search Customers by Name");
            System.out.println("  2. Filter Customers by Type");
            System.out.println("  3. Find Customer by ID");
            System.out.println("  4. Filter Claims by Status");
            System.out.println("  5. View Claims for a Customer");
            System.out.println("  6. View Cards for a Customer");
            System.out.println("  7. Back to Main Menu");
            int choice = input.promptInt("Choose an option: ", 1, 7);
            switch (choice) {
                case 1: searchCustomersByName(); break;
                case 2: filterCustomersByType(); break;
                case 3: findCustomerById(); break;
                case 4: filterClaimsByStatus(); break;
                case 5: viewClaimsForCustomer(); break;
                case 6: viewCardsForCustomer(); break;
                case 7: return;
            }
        }
    }

    private void searchCustomersByName() {
        String keyword = input.promptString("Enter name keyword: ");
        ArrayList<Customer> results = manager.searchCustomersByName(keyword);
        if (results.isEmpty()) {
            System.out.println("  No customers found matching \"" + keyword + "\".");
        } else {
            System.out.println("\n  Search results (" + results.size() + " found):");
            printCustomerTable(results);
        }
    }

    private void filterCustomersByType() {
        System.out.println("  1. PolicyHolder\n  2. Dependent");
        int c = input.promptInt("Choose type: ", 1, 2);
        CustomerType type = (c == 1) ? CustomerType.POLICY_HOLDER : CustomerType.DEPENDENT;
        ArrayList<Customer> results = manager.getCustomersByType(type);
        System.out.println("\n  " + type.getLabel() + "s (" + results.size() + "):");
        printCustomerTable(results);
    }

    private void findCustomerById() {
        String id = input.promptCustomerId("Enter customer ID");
        Customer found = manager.getCustomerById(id);
        if (found == null) {
            System.out.println("  Customer not found.");
        } else {
            printCustomerDetail(found);
        }
    }

    private void filterClaimsByStatus() {
        System.out.println("  1. New\n  2. Processing\n  3. Done");
        int c = input.promptInt("Choose status: ", 1, 3);
        ClaimStatus status;
        switch (c) {
            case 1: status = ClaimStatus.NEW; break;
            case 2: status = ClaimStatus.PROCESSING; break;
            default: status = ClaimStatus.DONE; break;
        }
        ArrayList<Claim> results = manager.getClaimsByStatus(status);
        System.out.println("\n  Claims with status: " + status.getLabel() + " (" + results.size() + "):");
        printClaimTable(results);
    }

    private void viewClaimsForCustomer() {
        String id = input.promptString("Enter customer ID: ");
        Customer cust = manager.getCustomerById(id);
        if (cust == null) {
            System.out.println("  Customer not found.");
            return;
        }
        ArrayList<Claim> results = manager.getClaimsByCustomerId(id);
        System.out.println("\n  Claims for " + cust.getFullName() + " (" + results.size() + "):");
        printClaimTable(results);
    }

    private void viewCardsForCustomer() {
        String id = input.promptString("Enter customer ID: ");
        Customer cust = manager.getCustomerById(id);
        if (cust == null) {
            System.out.println("  Customer not found.");
            return;
        }
        ArrayList<InsuranceCard> results = manager.getCardsByCustomerId(id);
        System.out.println("\n  Cards for " + cust.getFullName() + " (" + results.size() + "):");
        printCardTable(results);
    }

    // ==================== CUSTOMER MENU ====================

    private void manageCustomers() {
        while (true) {
            System.out.println("\n--- Customer Directory ---");
            System.out.println("  1. View All Customers");
            System.out.println("  2. Add New Customer");
            System.out.println("  3. Update Customer");
            System.out.println("  4. Delete Customer");
            System.out.println("  5. View Customer Detail (with cards & claims)");
            System.out.println("  6. Back to Main Menu");
            int choice = input.promptInt("Choose an option: ", 1, 6);
            switch (choice) {
                case 1: viewAllCustomers(); break;
                case 2: addCustomer(); break;
                case 3: updateCustomer(); break;
                case 4: deleteCustomer(); break;
                case 5: viewCustomerDetail(); break;
                case 6: return;
            }
        }
    }

    private void viewAllCustomers() {
        ArrayList<Customer> list = manager.getCustomers();
        if (list.isEmpty()) {
            System.out.println("  No customers in the system.");
            return;
        }
        System.out.println("\n--- All Customers (" + list.size() + ") ---");
        printCustomerTable(list);
    }

    private void addCustomer() {
        System.out.println("\n--- Add New Customer ---");
        String id = input.promptCustomerId("Customer ID");
        if (manager.getCustomerById(id) != null) {
            System.out.println("  Error: Customer ID " + id + " already exists.");
            return;
        }
        String fullName = input.promptString("Full Name: ");
        System.out.println("  Customer Type: 1 = PolicyHolder, 2 = Dependent");
        int typeChoice = input.promptInt("Choose type: ", 1, 2);
        String typeLabel = (typeChoice == 1) ? "PolicyHolder" : "Dependent";
        String parentId = null;
        if (typeChoice == 2) {
            parentId = input.promptString("Parent Policy Holder ID: ");
        }
        String error = manager.addCustomer(new Customer(id, fullName, typeLabel, parentId));
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            System.out.println("  Customer added successfully.");
        }
    }

    private void updateCustomer() {
        System.out.println("\n--- Update Customer ---");
        String id = input.promptString("Customer ID to update: ");
        Customer existing = manager.getCustomerById(id);
        if (existing == null) {
            System.out.println("  Customer not found.");
            return;
        }
        System.out.println("  Current: " + existing.getFullName() + " | " + existing.getCustomerTypeLabel() +
                " | Parent: " + (existing.getParentPolicyHolderId() == null ? "N/A" : existing.getParentPolicyHolderId()));
        System.out.println("  (Leave blank to keep current value)");
        String name = input.promptOptionalString("New Full Name");
        String typeLabel = input.promptOptionalString("New Type (PolicyHolder/Dependent)");
        String parentId = input.promptOptionalString("New Parent Policy Holder ID");
        String error = manager.updateCustomer(id, name, typeLabel, parentId);
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            System.out.println("  Customer updated successfully.");
        }
    }

    private void deleteCustomer() {
        System.out.println("\n--- Delete Customer ---");
        String id = input.promptString("Customer ID to delete: ");
        Customer c = manager.getCustomerById(id);
        if (c == null) {
            System.out.println("  Customer not found.");
            return;
        }
        int cardCount = manager.getCardsByCustomerId(id).size() + manager.getCardsByPolicyOwner(id).size();
        int claimCount = manager.getClaimsByCustomerId(id).size();
        System.out.println("  This will also delete " + cardCount + " associated card(s) and " +
                claimCount + " claim(s).");
        if (!input.confirm("Are you sure you want to delete " + c.getFullName() + "?")) {
            System.out.println("  Deletion cancelled.");
            return;
        }
        manager.deleteCustomer(id);
        System.out.println("  Customer and all associated records deleted.");
    }

    private void viewCustomerDetail() {
        String id = input.promptString("Enter customer ID: ");
        Customer c = manager.getCustomerById(id);
        if (c == null) {
            System.out.println("  Customer not found.");
            return;
        }
        printCustomerDetail(c);
    }

    private void printCustomerDetail(Customer c) {
        System.out.println("\n=============================================");
        System.out.println("  Customer Detail");
        System.out.println("=============================================");
        System.out.printf("  ID:          %s%n", c.getId());
        System.out.printf("  Name:        %s%n", c.getFullName());
        System.out.printf("  Type:        %s%n", c.getCustomerTypeLabel());
        System.out.printf("  Parent PH:   %s%n", c.getParentPolicyHolderId() == null ? "N/A (Primary)" : c.getParentPolicyHolderId());

        if (c.isPolicyHolder()) {
            ArrayList<Customer> deps = manager.getDependentsOf(c.getId());
            System.out.printf("  Dependents:  %d%n", deps.size());
            for (Customer dep : deps) {
                System.out.printf("    - %s (%s)%n", dep.getFullName(), dep.getId());
            }
        }

        ArrayList<InsuranceCard> cards = manager.getCardsByCustomerId(c.getId());
        System.out.printf("  Cards:       %d%n", cards.size());
        for (InsuranceCard card : cards) {
            System.out.printf("    - %s (Expires: %s)%n", card.getCardNumber(),
                    card.getExpirationDate().format(DATE_FMT));
        }

        ArrayList<Claim> claims = manager.getClaimsByCustomerId(c.getId());
        System.out.printf("  Claims:      %d%n", claims.size());
        for (Claim claim : claims) {
            System.out.printf("    - %s | $%.2f | %s%n", claim.getId(),
                    claim.getClaimAmount(), claim.getStatusLabel());
        }
        System.out.println("=============================================");
    }

    // ==================== CARD MENU ====================

    private void manageCards() {
        while (true) {
            System.out.println("\n--- Insurance Cards ---");
            System.out.println("  1. View All Cards");
            System.out.println("  2. Add New Card");
            System.out.println("  3. Update Card");
            System.out.println("  4. Delete Card");
            System.out.println("  5. Back to Main Menu");
            int choice = input.promptInt("Choose an option: ", 1, 5);
            switch (choice) {
                case 1: viewAllCards(); break;
                case 2: addCard(); break;
                case 3: updateCard(); break;
                case 4: deleteCard(); break;
                case 5: return;
            }
        }
    }

    private void viewAllCards() {
        ArrayList<InsuranceCard> list = manager.getCards();
        if (list.isEmpty()) {
            System.out.println("  No cards in the system.");
            return;
        }
        System.out.println("\n--- All Insurance Cards (" + list.size() + ") ---");
        printCardTable(list);
    }

    private void addCard() {
        System.out.println("\n--- Add New Insurance Card ---");
        String cardNumber = input.promptCardNumber("Card Number");
        if (manager.getCardByNumber(cardNumber) != null) {
            System.out.println("  Error: Card number already exists.");
            return;
        }
        String holderId = input.promptString("Card Holder ID: ");
        if (manager.getCustomerById(holderId) == null) {
            System.out.println("  Error: Holder ID not found.");
            return;
        }
        String ownerId = input.promptString("Policy Owner ID: ");
        Customer owner = manager.getCustomerById(ownerId);
        if (owner == null) {
            System.out.println("  Error: Policy owner ID not found.");
            return;
        }
        if (!owner.isPolicyHolder()) {
            System.out.println("  Error: Policy owner must be a PolicyHolder.");
            return;
        }
        LocalDateTime expDate = input.promptDateTime("Expiration Date");
        String error = manager.addCard(new InsuranceCard(cardNumber, holderId, ownerId, expDate));
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            System.out.println("  Card added successfully.");
        }
    }

    private void updateCard() {
        System.out.println("\n--- Update Insurance Card ---");
        String cardNumber = input.promptString("Card Number to update: ");
        InsuranceCard card = manager.getCardByNumber(cardNumber);
        if (card == null) {
            System.out.println("  Card not found.");
            return;
        }
        System.out.println("  Current: Holder=" + card.getCardHolderId() +
                " | Owner=" + card.getPolicyOwnerId() +
                " | Expires=" + card.getExpirationDate().format(DATE_FMT));
        System.out.println("  (Leave blank to keep current value)");
        String newHolderId = input.promptOptionalString("New Holder ID");
        String newOwnerId = input.promptOptionalString("New Owner ID");
        String expStr = input.promptOptionalString("New Expiration Date (yyyy-MM-ddTHH:mm:ss)");
        LocalDateTime newExp = null;
        if (expStr != null && !expStr.isEmpty()) {
            try {
                newExp = LocalDateTime.parse(expStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (Exception e) {
                System.out.println("  Invalid date format.");
                return;
            }
        }
        String error = manager.updateCard(cardNumber, newHolderId, newOwnerId, newExp);
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            System.out.println("  Card updated successfully.");
        }
    }

    private void deleteCard() {
        System.out.println("\n--- Delete Card ---");
        String cardNumber = input.promptString("Card Number to delete: ");
        InsuranceCard card = manager.getCardByNumber(cardNumber);
        if (card == null) {
            System.out.println("  Card not found.");
            return;
        }
        if (!input.confirm("Are you sure you want to delete card " + cardNumber + "?")) {
            System.out.println("  Deletion cancelled.");
            return;
        }
        manager.deleteCard(cardNumber);
        System.out.println("  Card and associated claims deleted.");
    }

    // ==================== CLAIM MENU ====================

    private void manageClaims() {
        while (true) {
            System.out.println("\n--- Claims Management ---");
            System.out.println("  1. View All Claims");
            System.out.println("  2. View Claims Sorted (by Date or Amount)");
            System.out.println("  3. Add New Claim");
            System.out.println("  4. Update Claim Status");
            System.out.println("  5. Add Document to Claim");
            System.out.println("  6. View Claim Detail");
            System.out.println("  7. Delete Claim");
            System.out.println("  8. Back to Main Menu");
            int choice = input.promptInt("Choose an option: ", 1, 8);
            switch (choice) {
                case 1: viewAllClaims(); break;
                case 2: viewClaimsSorted(); break;
                case 3: addClaim(); break;
                case 4: updateClaimStatus(); break;
                case 5: addDocument(); break;
                case 6: viewClaimDetail(); break;
                case 7: deleteClaim(); break;
                case 8: return;
            }
        }
    }

    private void viewAllClaims() {
        ArrayList<Claim> list = manager.getClaims();
        if (list.isEmpty()) {
            System.out.println("  No claims in the system.");
            return;
        }
        System.out.println("\n--- All Claims (" + list.size() + ") ---");
        printClaimTable(list);
    }

    private void viewClaimsSorted() {
        System.out.println("  1. By Date (newest first)\n  2. By Amount (highest first)");
        int c = input.promptInt("Sort by: ", 1, 2);
        ArrayList<Claim> sorted = (c == 1) ? manager.getClaimsSortedByDate() : manager.getClaimsSortedByAmount();
        String label = (c == 1) ? "date (newest first)" : "amount (highest first)";
        System.out.println("\n--- Claims Sorted by " + label + " ---");
        printClaimTable(sorted);
    }

    private void addClaim() {
        System.out.println("\n--- Add New Claim ---");
        String id = input.promptClaimId("Claim ID");
        if (manager.getClaimById(id) != null) {
            System.out.println("  Error: Claim ID already exists.");
            return;
        }
        LocalDateTime claimDate = input.promptDateTime("Claim Date");
        String insuredId = input.promptString("Insured Person ID: ");
        if (manager.getCustomerById(insuredId) == null) {
            System.out.println("  Error: Insured person not found.");
            return;
        }
        String cardNumber = input.promptString("Card Number: ");
        InsuranceCard card = manager.getCardByNumber(cardNumber);
        if (card == null) {
            System.out.println("  Error: Card not found.");
            return;
        }
        LocalDateTime examDate = input.promptDateTime("Exam Date");
        if (examDate.isAfter(claimDate)) {
            System.out.println("  Error: Exam date must be before or on the claim date.");
            return;
        }
        if (examDate.isAfter(card.getExpirationDate())) {
            System.out.println("  Error: Exam date must be before card expiration (" +
                    card.getExpirationDate().format(DATE_FMT) + ").");
            return;
        }
        double amount = input.promptPositiveDouble("Claim Amount: $");
        String error = manager.addClaim(new Claim(id, claimDate, insuredId, cardNumber, examDate, amount, "New"));
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            System.out.println("  Claim added successfully with status: New.");
        }
    }

    private void updateClaimStatus() {
        System.out.println("\n--- Update Claim Status ---");
        String id = input.promptString("Claim ID: ");
        Claim claim = manager.getClaimById(id);
        if (claim == null) {
            System.out.println("  Claim not found.");
            return;
        }
        System.out.println("  Current status: " + claim.getStatusLabel());
        if (claim.getStatus() == ClaimStatus.DONE) {
            System.out.println("  This claim is already Done and cannot be changed.");
            return;
        }
        System.out.println("  Valid next status: " + claim.getNextStatus().getLabel());
        if (!input.confirm("Update status to " + claim.getNextStatus().getLabel() + "?")) {
            System.out.println("  Update cancelled.");
            return;
        }
        String error = manager.updateClaimStatus(id, claim.getNextStatus().getLabel());
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            System.out.println("  Claim status updated to " + claim.getStatusLabel() + ".");
        }
    }

    private void addDocument() {
        System.out.println("\n--- Add Document to Claim ---");
        String claimId = input.promptString("Claim ID: ");
        Claim claim = manager.getClaimById(claimId);
        if (claim == null) {
            System.out.println("  Claim not found.");
            return;
        }
        String expectedPrefix = claimId + "_" + claim.getCardNumber() + "_";
        System.out.println("  Document name must start with: " + expectedPrefix);
        System.out.println("  Document name must end with: .pdf");
        String docName = input.promptString("Document Name: ");
        String error = manager.addDocumentToClaim(claimId, docName);
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            System.out.println("  Document added. Total documents: " + claim.getDocuments().size());
        }
    }

    private void viewClaimDetail() {
        String id = input.promptString("Claim ID: ");
        Claim claim = manager.getClaimById(id);
        if (claim == null) {
            System.out.println("  Claim not found.");
            return;
        }
        Customer insured = manager.getCustomerById(claim.getInsuredPersonId());
        InsuranceCard card = manager.getCardByNumber(claim.getCardNumber());

        System.out.println("\n=============================================");
        System.out.println("  Claim Detail");
        System.out.println("=============================================");
        System.out.printf("  ID:              %s%n", claim.getId());
        System.out.printf("  Status:          %s%n", claim.getStatusLabel());
        System.out.printf("  Claim Date:      %s%n", claim.getClaimDate().format(DATE_FMT));
        System.out.printf("  Exam Date:       %s%n", claim.getExamDate().format(DATE_FMT));
        System.out.printf("  Insured Person:  %s (%s)%n",
                insured != null ? insured.getFullName() : "Unknown", claim.getInsuredPersonId());
        System.out.printf("  Card Number:     %s%n", claim.getCardNumber());
        if (card != null) {
            System.out.printf("  Card Expires:    %s%n", card.getExpirationDate().format(DATE_FMT));
        }
        System.out.printf("  Claim Amount:    $%.2f%n", claim.getClaimAmount());
        System.out.printf("  Documents:       %d%n", claim.getDocuments().size());
        for (int i = 0; i < claim.getDocuments().size(); i++) {
            System.out.printf("    [%d] %s%n", i + 1, claim.getDocuments().get(i));
        }
        System.out.println("=============================================");
    }

    private void deleteClaim() {
        System.out.println("\n--- Delete Claim ---");
        String id = input.promptString("Claim ID to delete: ");
        Claim claim = manager.getClaimById(id);
        if (claim == null) {
            System.out.println("  Claim not found.");
            return;
        }
        if (!input.confirm("Are you sure you want to delete claim " + id + "?")) {
            System.out.println("  Deletion cancelled.");
            return;
        }
        manager.deleteClaim(id);
        System.out.println("  Claim deleted.");
    }

    // ==================== SAVE & EXIT ====================

    private void saveAndExit() {
        System.out.println("\nSaving data to files...");
        fileManager.saveCustomers(dataDir + "/customers.txt", manager.getCustomers());
        fileManager.saveCards(dataDir + "/cards.txt", manager.getCards());
        fileManager.saveClaims(dataDir + "/claims.txt", manager.getClaims());
        System.out.println("  " + manager.getCustomers().size() + " customers saved.");
        System.out.println("  " + manager.getCards().size() + " cards saved.");
        System.out.println("  " + manager.getClaims().size() + " claims saved.");
        System.out.println("\nGoodbye!");
    }

    // ==================== DISPLAY HELPERS ====================

    private void printCustomerTable(ArrayList<Customer> list) {
        System.out.printf("  %-12s %-22s %-14s %-12s%n", "ID", "Name", "Type", "Parent PH");
        System.out.println("  " + "-".repeat(68));
        for (Customer c : list) {
            String parent = c.getParentPolicyHolderId() == null ? "N/A" : c.getParentPolicyHolderId();
            System.out.printf("  %-12s %-22s %-14s %-12s%n",
                    c.getId(), truncate(c.getFullName(), 21), c.getCustomerTypeLabel(), parent);
        }
    }

    private void printCardTable(ArrayList<InsuranceCard> cards) {
        System.out.printf("  %-12s %-14s %-14s %-20s%n", "Card No.", "Holder", "Owner", "Expires");
        System.out.println("  " + "-".repeat(65));
        for (InsuranceCard c : cards) {
            System.out.printf("  %-12s %-14s %-14s %-20s%n",
                    c.getCardNumber(), c.getCardHolderId(), c.getPolicyOwnerId(),
                    c.getExpirationDate().format(DATE_FMT));
        }
    }

    private void printClaimTable(ArrayList<Claim> list) {
        System.out.printf("  %-15s %-14s %-12s %-12s %-12s %s%n",
                "Claim ID", "Insured", "Card", "Amount", "Status", "Docs");
        System.out.println("  " + "-".repeat(80));
        for (Claim c : list) {
            System.out.printf("  %-15s %-14s %-12s $%9.2f  %-12s %d%n",
                    c.getId(), c.getInsuredPersonId(), c.getCardNumber(),
                    c.getClaimAmount(), c.getStatusLabel(), c.getDocuments().size());
        }
    }

    private String truncate(String s, int maxLen) {
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen - 1) + ".";
    }
}
