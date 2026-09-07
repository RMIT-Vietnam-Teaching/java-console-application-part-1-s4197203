package ui;

import exceptions.AuthenticationException;
import manager.ClaimManager;
import manager.FileManager;
import model.*;
import service.ActivityLogger;
import service.AuthenticationService;
import service.ReportService;
import service.UserManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Interactive console UI with role-based access control.
 * Provides different menus for Admin, ClaimsOfficer, and Customer roles.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class ConsoleUI {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Scanner scanner;
    private InputHelper input;
    private ClaimManager claimManager;
    private UserManager userManager;
    private AuthenticationService authService;
    private ActivityLogger logger;
    private FileManager fileManager;
    private String dataDir;

    public ConsoleUI(ClaimManager claimManager, UserManager userManager,
                     AuthenticationService authService, ActivityLogger logger, String dataDir) {
        this.scanner = new Scanner(System.in);
        this.input = new InputHelper(scanner);
        this.claimManager = claimManager;
        this.userManager = userManager;
        this.authService = authService;
        this.logger = logger;
        this.fileManager = new FileManager();
        this.dataDir = dataDir;
    }

    // ==================== MAIN RUN LOOP ====================

    public void run() {
        while (true) {
            User loggedUser = loginScreen();
            if (loggedUser == null) return;
            switch (loggedUser.getRole()) {
                case ADMIN:
                    adminMenu(loggedUser);
                    break;
                case CLAIMS_OFFICER:
                    officerMenu(loggedUser);
                    break;
                case CUSTOMER:
                    customerMenu(loggedUser);
                    break;
            }
            authService.logout();
        }
    }

    // ==================== LOGIN ====================

    private User loginScreen() {
        System.out.println("\n========================================");
        System.out.println("        ClaimShield - Login");
        System.out.println("========================================");
        System.out.println("  1. Login");
        System.out.println("  2. Exit");
        int choice = input.promptInt("Choose: ", 1, 2);
        if (choice == 2) {
            System.out.println("Goodbye!");
            return null;
        }
        String userId = input.promptString("User ID: ");
        String password = input.promptString("Password: ");
        try {
            User user = authService.login(userId, password);
            logger.log(userId, "Login successful");
            System.out.println("\n  Welcome, " + user.getFullName() + " (" + user.getRole().getLabel() + ")");
            return user;
        } catch (AuthenticationException e) {
            System.out.println("  Login failed: " + e.getMessage());
            logger.log(userId, "Login failed: " + e.getMessage());
            return loginScreen();
        }
    }

    // ==================== ADMIN MENU ====================

    private void adminMenu(User admin) {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("       Admin Menu - " + admin.getFullName());
            System.out.println("========================================");
            System.out.println("  1.  Manage Users");
            System.out.println("  2.  Manage Customers");
            System.out.println("  3.  Manage Insurance Cards");
            System.out.println("  4.  Manage Claims");
            System.out.println("  5.  View System Statistics");
            System.out.println("  6.  Search & Filter Records");
            System.out.println("  7.  Financial Report");
            System.out.println("  8.  Officer Performance Report");
            System.out.println("  9.  Membership Tier Summary");
            System.out.println("  10. View Activity Logs");
            System.out.println("  11. Save and Logout");
            System.out.println("========================================");
            int choice = input.promptInt("Choose: ", 1, 11);
            switch (choice) {
                case 1: manageUsers(admin); break;
                case 2: manageCustomers(); break;
                case 3: manageCards(); break;
                case 4: manageClaims(); break;
                case 5: showStatistics(); break;
                case 6: searchRecords(); break;
                case 7: financialReport(); break;
                case 8: officerPerformanceReport(); break;
                case 9: tierSummary(); break;
                case 10: viewLogs(); break;
                case 11: saveAndLogout(admin); return;
            }
        }
    }

    // ==================== CLAIMS OFFICER MENU ====================

    private void officerMenu(User officer) {
        while (true) {
            System.out.println("\n========================================");
            System.out.println("    Claims Officer Menu - " + officer.getFullName());
            System.out.println("========================================");
            System.out.println("  1. View All Claims");
            System.out.println("  2. Process Claim (Update Status)");
            System.out.println("  3. Add Document to Claim");
            System.out.println("  4. View Claim Detail");
            System.out.println("  5. View All Customers");
            System.out.println("  6. View All Cards");
            System.out.println("  7. Search & Filter");
            System.out.println("  8. View System Statistics");
            System.out.println("  9. Save and Logout");
            System.out.println("========================================");
            int choice = input.promptInt("Choose: ", 1, 9);
            switch (choice) {
                case 1: viewAllClaims(); break;
                case 2: updateClaimStatus(); break;
                case 3: addDocument(); break;
                case 4: viewClaimDetail(); break;
                case 5: viewAllCustomers(); break;
                case 6: viewAllCards(); break;
                case 7: searchRecords(); break;
                case 8: showStatistics(); break;
                case 9: saveAndLogout(officer); return;
            }
        }
    }

    // ==================== CUSTOMER MENU ====================

    private void customerMenu(User customer) {
        String customerId = null;
        if (customer instanceof PolicyHolder) {
            customerId = ((PolicyHolder) customer).getCustomerId();
        } else if (customer instanceof Dependent) {
            customerId = ((Dependent) customer).getCustomerId();
        }

        while (true) {
            System.out.println("\n========================================");
            System.out.println("       Customer Menu - " + customer.getFullName());
            System.out.println("========================================");
            System.out.println("  1. View My Profile");
            System.out.println("  2. View My Cards");
            System.out.println("  3. View My Claims");
            System.out.println("  4. Submit New Claim");
            System.out.println("  5. View Claim Detail");
            System.out.println("  6. Save and Logout");
            System.out.println("========================================");
            int choice = input.promptInt("Choose: ", 1, 6);
            switch (choice) {
                case 1: viewMyProfile(customerId); break;
                case 2: viewMyCards(customerId); break;
                case 3: viewMyClaims(customerId); break;
                case 4: submitMyClaim(customerId); break;
                case 5: viewClaimDetail(); break;
                case 6: saveAndLogout(customer); return;
            }
        }
    }

    // ==================== ADMIN: USER MANAGEMENT ====================

    private void manageUsers(User admin) {
        while (true) {
            System.out.println("\n--- User Management ---");
            System.out.println("  1. View All Users");
            System.out.println("  2. Add New User");
            System.out.println("  3. Update User");
            System.out.println("  4. Delete User");
            System.out.println("  5. Back");
            int choice = input.promptInt("Choose: ", 1, 5);
            switch (choice) {
                case 1: viewAllUsers(); break;
                case 2: addUser(); break;
                case 3: updateUser(); break;
                case 4: deleteUser(); break;
                case 5: return;
            }
        }
    }

    private void viewAllUsers() {
        ArrayList<User> users = userManager.getUsers();
        System.out.println("\n--- All Users (" + users.size() + ") ---");
        System.out.printf("  %-10s %-12s %-22s %-16s %-10s%n", "ID", "Role", "Name", "Status", "Linked");
        System.out.println("  " + repeatChar('-', 75));
        for (User u : users) {
            String linked = "";
            if (u instanceof PolicyHolder) linked = ((PolicyHolder) u).getCustomerId();
            else if (u instanceof Dependent) linked = ((Dependent) u).getCustomerId();
            System.out.printf("  %-10s %-12s %-22s %-10s %-16s%n",
                    u.getUserId(), u.getRole().getLabel(), truncate(u.getFullName(), 21),
                    u.getStatus().getLabel(), linked);
        }
    }

    private void addUser() {
        System.out.println("\n--- Add New User ---");
        String userId = input.promptString("User ID: ");
        if (userManager.getUserById(userId) != null) {
            System.out.println("  Error: User ID already exists.");
            return;
        }
        String password = input.promptString("Password: ");
        String fullName = input.promptString("Full Name: ");
        System.out.println("  Role: 1 = Admin, 2 = ClaimsOfficer, 3 = Customer");
        int roleChoice = input.promptInt("Choose role: ", 1, 3);
        User user = null;
        switch (roleChoice) {
            case 1:
                user = new Admin(userId, password, fullName, UserStatus.ACTIVE);
                break;
            case 2:
                user = new ClaimsOfficer(userId, password, fullName, UserStatus.ACTIVE);
                break;
            case 3:
                String custId = input.promptString("Linked Customer ID: ");
                user = new PolicyHolder(userId, password, fullName, UserStatus.ACTIVE, custId);
                break;
        }
        String error = userManager.addUser(user);
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            logger.log(authService.getCurrentUser().getUserId(), "Added user " + userId);
            System.out.println("  User added successfully.");
        }
    }

    private void updateUser() {
        System.out.println("\n--- Update User ---");
        String userId = input.promptString("User ID: ");
        User user = userManager.getUserById(userId);
        if (user == null) {
            System.out.println("  User not found.");
            return;
        }
        System.out.println("  Current: " + user.getFullName() + " | " + user.getStatus().getLabel());
        System.out.println("  (Leave blank to keep current)");
        String name = input.promptOptionalString("New Full Name");
        String status = input.promptOptionalString("New Status (Active/Inactive)");
        String error = userManager.updateUser(userId, name, status);
        if (error != null) {
            System.out.println("  Error: " + error);
        } else {
            logger.log(authService.getCurrentUser().getUserId(), "Updated user " + userId);
            System.out.println("  User updated.");
        }
    }

    private void deleteUser() {
        System.out.println("\n--- Delete User ---");
        String userId = input.promptString("User ID: ");
        User user = userManager.getUserById(userId);
        if (user == null) {
            System.out.println("  User not found.");
            return;
        }
        if (!input.confirm("Delete " + user.getFullName() + "?")) {
            System.out.println("  Cancelled.");
            return;
        }
        userManager.deleteUser(userId);
        logger.log(authService.getCurrentUser().getUserId(), "Deleted user " + userId);
        System.out.println("  User deleted.");
    }

    // ==================== CUSTOMER MANAGEMENT ====================

    private void manageCustomers() {
        while (true) {
            System.out.println("\n--- Customer Directory ---");
            System.out.println("  1. View All Customers");
            System.out.println("  2. Add New Customer");
            System.out.println("  3. Update Customer");
            System.out.println("  4. Delete Customer");
            System.out.println("  5. View Customer Detail");
            System.out.println("  6. Back");
            int choice = input.promptInt("Choose: ", 1, 6);
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
        ArrayList<Customer> list = claimManager.getCustomers();
        System.out.println("\n--- All Customers (" + list.size() + ") ---");
        printCustomerTable(list);
    }

    private void addCustomer() {
        System.out.println("\n--- Add New Customer ---");
        String id = input.promptCustomerId("Customer ID");
        String fullName = input.promptString("Full Name: ");
        System.out.println("  Type: 1 = PolicyHolder, 2 = Dependent");
        int typeChoice = input.promptInt("Choose: ", 1, 2);
        String typeLabel = (typeChoice == 1) ? "PolicyHolder" : "Dependent";
        String parentId = null;
        if (typeChoice == 2) parentId = input.promptString("Parent Policy Holder ID: ");
        String error = claimManager.addCustomer(new Customer(id, fullName, typeLabel, parentId));
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(authService.getCurrentUser().getUserId(), "Added customer " + id);
            System.out.println("  Customer added.");
        }
    }

    private void updateCustomer() {
        System.out.println("\n--- Update Customer ---");
        String id = input.promptString("Customer ID: ");
        Customer existing = claimManager.getCustomerById(id);
        if (existing == null) { System.out.println("  Not found."); return; }
        System.out.println("  Current: " + existing.getFullName() + " | " + existing.getCustomerTypeLabel());
        System.out.println("  (Leave blank to keep)");
        String name = input.promptOptionalString("New Name");
        String type = input.promptOptionalString("New Type (PolicyHolder/Dependent)");
        String parent = input.promptOptionalString("New Parent ID");
        String error = claimManager.updateCustomer(id, name, type, parent);
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(authService.getCurrentUser().getUserId(), "Updated customer " + id);
            System.out.println("  Updated.");
        }
    }

    private void deleteCustomer() {
        System.out.println("\n--- Delete Customer ---");
        String id = input.promptString("Customer ID: ");
        Customer c = claimManager.getCustomerById(id);
        if (c == null) { System.out.println("  Not found."); return; }
        if (!input.confirm("Delete " + c.getFullName() + " and all associated records?")) {
            System.out.println("  Cancelled."); return;
        }
        claimManager.deleteCustomer(id);
        logger.log(authService.getCurrentUser().getUserId(), "Deleted customer " + id);
        System.out.println("  Deleted.");
    }

    private void viewCustomerDetail() {
        String id = input.promptString("Customer ID: ");
        Customer c = claimManager.getCustomerById(id);
        if (c == null) { System.out.println("  Not found."); return; }
        printCustomerDetail(c);
    }

    // ==================== CARD MANAGEMENT ====================

    private void manageCards() {
        while (true) {
            System.out.println("\n--- Insurance Cards ---");
            System.out.println("  1. View All Cards");
            System.out.println("  2. Add New Card");
            System.out.println("  3. Update Card");
            System.out.println("  4. Delete Card");
            System.out.println("  5. Back");
            int choice = input.promptInt("Choose: ", 1, 5);
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
        ArrayList<InsuranceCard> list = claimManager.getCards();
        System.out.println("\n--- All Cards (" + list.size() + ") ---");
        printCardTable(list);
    }

    private void addCard() {
        System.out.println("\n--- Add New Card ---");
        String cardNumber = input.promptCardNumber("Card Number");
        String holderId = input.promptString("Holder ID: ");
        String ownerId = input.promptString("Owner ID: ");
        LocalDateTime expDate = input.promptDateTime("Expiration Date");
        System.out.println("  Tier: 1=BASIC, 2=SILVER, 3=GOLD, 4=PLATINUM");
        int tierChoice = input.promptInt("Choose tier: ", 1, 4);
        MembershipTier tier = MembershipTier.values()[tierChoice - 1];
        InsuranceCard card = new InsuranceCard(cardNumber, holderId, ownerId, expDate, tier);
        String error = claimManager.addCard(card);
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(authService.getCurrentUser().getUserId(), "Added card " + cardNumber);
            System.out.println("  Card added.");
        }
    }

    private void updateCard() {
        System.out.println("\n--- Update Card ---");
        String cardNumber = input.promptString("Card Number: ");
        InsuranceCard card = claimManager.getCardByNumber(cardNumber);
        if (card == null) { System.out.println("  Not found."); return; }
        System.out.println("  Current: Holder=" + card.getCardHolderId() + " | Owner=" + card.getPolicyOwnerId());
        System.out.println("  (Leave blank to keep)");
        String holder = input.promptOptionalString("New Holder ID");
        String owner = input.promptOptionalString("New Owner ID");
        String expStr = input.promptOptionalString("New Expiration (yyyy-MM-ddTHH:mm:ss)");
        LocalDateTime exp = null;
        if (expStr != null && !expStr.isEmpty()) {
            try { exp = LocalDateTime.parse(expStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); }
            catch (Exception e) { System.out.println("  Invalid date."); return; }
        }
        String error = claimManager.updateCard(cardNumber, holder, owner, exp);
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(authService.getCurrentUser().getUserId(), "Updated card " + cardNumber);
            System.out.println("  Updated.");
        }
    }

    private void deleteCard() {
        System.out.println("\n--- Delete Card ---");
        String cardNumber = input.promptString("Card Number: ");
        InsuranceCard card = claimManager.getCardByNumber(cardNumber);
        if (card == null) { System.out.println("  Not found."); return; }
        if (!input.confirm("Delete card " + cardNumber + "?")) { System.out.println("  Cancelled."); return; }
        claimManager.deleteCard(cardNumber);
        logger.log(authService.getCurrentUser().getUserId(), "Deleted card " + cardNumber);
        System.out.println("  Deleted.");
    }

    // ==================== CLAIM MANAGEMENT ====================

    private void manageClaims() {
        while (true) {
            System.out.println("\n--- Claims Management ---");
            System.out.println("  1. View All Claims");
            System.out.println("  2. View Claims Sorted");
            System.out.println("  3. Add New Claim");
            System.out.println("  4. Update Claim Status");
            System.out.println("  5. Add Document to Claim");
            System.out.println("  6. View Claim Detail");
            System.out.println("  7. Calculate Co-Pay");
            System.out.println("  8. Delete Claim");
            System.out.println("  9. Back");
            int choice = input.promptInt("Choose: ", 1, 9);
            switch (choice) {
                case 1: viewAllClaims(); break;
                case 2: viewClaimsSorted(); break;
                case 3: addClaim(); break;
                case 4: updateClaimStatus(); break;
                case 5: addDocument(); break;
                case 6: viewClaimDetail(); break;
                case 7: calculateCoPay(); break;
                case 8: deleteClaim(); break;
                case 9: return;
            }
        }
    }

    private void viewAllClaims() {
        ArrayList<Claim> list = claimManager.getClaims();
        System.out.println("\n--- All Claims (" + list.size() + ") ---");
        printClaimTable(list);
    }

    private void viewClaimsSorted() {
        System.out.println("  1. By Date (newest first)\n  2. By Amount (highest first)");
        int c = input.promptInt("Sort by: ", 1, 2);
        ArrayList<Claim> sorted = (c == 1) ? claimManager.getClaimsSortedByDate() : claimManager.getClaimsSortedByAmount();
        printClaimTable(sorted);
    }

    private void addClaim() {
        System.out.println("\n--- Add New Claim ---");
        String id = input.promptClaimId("Claim ID");
        LocalDateTime claimDate = input.promptDateTime("Claim Date");
        String insuredId = input.promptString("Insured Person ID: ");
        String cardNumber = input.promptString("Card Number: ");
        LocalDateTime examDate = input.promptDateTime("Exam Date");
        double amount = input.promptPositiveDouble("Claim Amount: $");
        Claim claim = new Claim(id, claimDate, insuredId, cardNumber, examDate, amount, "New");
        String error = claimManager.addClaim(claim);
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(authService.getCurrentUser().getUserId(), "Added claim " + id);
            System.out.println("  Claim added.");
        }
    }

    private void updateClaimStatus() {
        System.out.println("\n--- Update Claim Status ---");
        String id = input.promptString("Claim ID: ");
        Claim claim = claimManager.getClaimById(id);
        if (claim == null) { System.out.println("  Not found."); return; }
        System.out.println("  Current: " + claim.getStatusLabel());
        if (claim.getStatus() == ClaimStatus.DONE) {
            System.out.println("  Already Done."); return;
        }
        System.out.println("  Next: " + claim.getNextStatus().getLabel());
        if (!input.confirm("Update?")) { System.out.println("  Cancelled."); return; }
        String error = claimManager.updateClaimStatus(id, claim.getNextStatus().getLabel());
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(authService.getCurrentUser().getUserId(), "Updated claim " + id + " to " + claim.getStatusLabel());
            System.out.println("  Updated.");
        }
    }

    private void addDocument() {
        System.out.println("\n--- Add Document ---");
        String claimId = input.promptString("Claim ID: ");
        Claim claim = claimManager.getClaimById(claimId);
        if (claim == null) { System.out.println("  Not found."); return; }
        System.out.println("  Must start with: " + claimId + "_" + claim.getCardNumber() + "_");
        String docName = input.promptString("Document Name: ");
        String error = claimManager.addDocumentToClaim(claimId, docName);
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(authService.getCurrentUser().getUserId(), "Added doc to claim " + claimId);
            System.out.println("  Document added.");
        }
    }

    private void viewClaimDetail() {
        String id = input.promptString("Claim ID: ");
        Claim claim = claimManager.getClaimById(id);
        if (claim == null) { System.out.println("  Not found."); return; }
        Customer insured = claimManager.getCustomerById(claim.getInsuredPersonId());
        InsuranceCard card = claimManager.getCardByNumber(claim.getCardNumber());

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
            System.out.printf("  Tier:            %s (Coverage: %d%%)%n",
                    card.getMembershipTier().getLabel(),
                    (int)(card.getMembershipTier().getCoverageRate() * 100));
        }
        System.out.printf("  Claim Amount:    $%,.2f%n", claim.getClaimAmount());
        if (card != null) {
            double coPay = claimManager.calculateCoPay(claim);
            double covered = claimManager.calculateCoverageAmount(claim);
            System.out.printf("  Coverage (%%):    $%,.2f%n", covered);
            System.out.printf("  Co-Pay:          $%,.2f%n", coPay);
        }
        System.out.printf("  Documents:       %d%n", claim.getDocuments().size());
        for (int i = 0; i < claim.getDocuments().size(); i++) {
            System.out.printf("    [%d] %s%n", i + 1, claim.getDocuments().get(i));
        }
        System.out.println("=============================================");
    }

    private void calculateCoPay() {
        System.out.println("\n--- Co-Pay Calculator ---");
        String id = input.promptString("Claim ID: ");
        Claim claim = claimManager.getClaimById(id);
        if (claim == null) { System.out.println("  Not found."); return; }
        InsuranceCard card = claimManager.getCardByNumber(claim.getCardNumber());
        if (card == null) { System.out.println("  Card not found."); return; }
        double coPay = claimManager.calculateCoPay(claim);
        double covered = claimManager.calculateCoverageAmount(claim);
        System.out.printf("  Claim Amount:    $%,.2f%n", claim.getClaimAmount());
        System.out.printf("  Tier:            %s (%d%% coverage)%n",
                card.getMembershipTier().getLabel(),
                (int)(card.getMembershipTier().getCoverageRate() * 100));
        System.out.printf("  Insurance Pays:  $%,.2f%n", covered);
        System.out.printf("  Customer Co-Pay: $%,.2f%n", coPay);
    }

    private void deleteClaim() {
        System.out.println("\n--- Delete Claim ---");
        String id = input.promptString("Claim ID: ");
        Claim claim = claimManager.getClaimById(id);
        if (claim == null) { System.out.println("  Not found."); return; }
        if (!input.confirm("Delete claim " + id + "?")) { System.out.println("  Cancelled."); return; }
        claimManager.deleteClaim(id);
        logger.log(authService.getCurrentUser().getUserId(), "Deleted claim " + id);
        System.out.println("  Deleted.");
    }

    // ==================== STATISTICS & REPORTS ====================

    private void showStatistics() {
        System.out.println(claimManager.getSystemStatistics());
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void financialReport() {
        ReportService report = new ReportService(
                claimManager.getCustomers(), claimManager.getCards(), claimManager.getClaims());
        System.out.println(report.generateFinancialReport());
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void officerPerformanceReport() {
        ReportService report = new ReportService(
                claimManager.getCustomers(), claimManager.getCards(), claimManager.getClaims());
        ArrayList<User> officers = userManager.getUsersByRole(UserRole.CLAIMS_OFFICER);
        System.out.println(report.generateOfficerPerformanceReport(officers));
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void tierSummary() {
        ReportService report = new ReportService(
                claimManager.getCustomers(), claimManager.getCards(), claimManager.getClaims());
        System.out.println(report.generateTierSummary());
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    private void viewLogs() {
        ArrayList<String> logs = logger.getRecentLogs(20);
        System.out.println("\n--- Recent Activity Logs (last 20) ---");
        for (String log : logs) {
            System.out.println("  " + log);
        }
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ==================== SEARCH ====================

    private void searchRecords() {
        while (true) {
            System.out.println("\n--- Search & Filter ---");
            System.out.println("  1. Search Customers by Name");
            System.out.println("  2. Filter Customers by Type");
            System.out.println("  3. Find Customer by ID");
            System.out.println("  4. Filter Claims by Status");
            System.out.println("  5. View Claims for Customer");
            System.out.println("  6. View Cards for Customer");
            System.out.println("  7. Back");
            int choice = input.promptInt("Choose: ", 1, 7);
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
        String keyword = input.promptString("Name keyword: ");
        ArrayList<Customer> results = claimManager.searchCustomersByName(keyword);
        if (results.isEmpty()) System.out.println("  No matches.");
        else printCustomerTable(results);
    }

    private void filterCustomersByType() {
        System.out.println("  1. PolicyHolder\n  2. Dependent");
        int c = input.promptInt("Choose: ", 1, 2);
        CustomerType type = (c == 1) ? CustomerType.POLICY_HOLDER : CustomerType.DEPENDENT;
        printCustomerTable(claimManager.getCustomersByType(type));
    }

    private void findCustomerById() {
        String id = input.promptCustomerId("Customer ID");
        Customer c = claimManager.getCustomerById(id);
        if (c == null) System.out.println("  Not found.");
        else printCustomerDetail(c);
    }

    private void filterClaimsByStatus() {
        System.out.println("  1. New\n  2. Processing\n  3. Done");
        int c = input.promptInt("Choose: ", 1, 3);
        ClaimStatus status;
        switch (c) {
            case 1: status = ClaimStatus.NEW; break;
            case 2: status = ClaimStatus.PROCESSING; break;
            default: status = ClaimStatus.DONE; break;
        }
        printClaimTable(claimManager.getClaimsByStatus(status));
    }

    private void viewClaimsForCustomer() {
        String id = input.promptString("Customer ID: ");
        Customer cust = claimManager.getCustomerById(id);
        if (cust == null) { System.out.println("  Not found."); return; }
        printClaimTable(claimManager.getClaimsByCustomerId(id));
    }

    private void viewCardsForCustomer() {
        String id = input.promptString("Customer ID: ");
        Customer cust = claimManager.getCustomerById(id);
        if (cust == null) { System.out.println("  Not found."); return; }
        printCardTable(claimManager.getCardsByCustomerId(id));
    }

    // ==================== CUSTOMER SELF-SERVICE ====================

    private void viewMyProfile(String customerId) {
        Customer c = claimManager.getCustomerById(customerId);
        if (c == null) { System.out.println("  Profile not found."); return; }
        printCustomerDetail(c);
    }

    private void viewMyCards(String customerId) {
        ArrayList<InsuranceCard> cards = claimManager.getCardsByCustomerId(customerId);
        System.out.println("\n--- My Cards (" + cards.size() + ") ---");
        printCardTable(cards);
    }

    private void viewMyClaims(String customerId) {
        ArrayList<Claim> claims = claimManager.getClaimsByCustomerId(customerId);
        System.out.println("\n--- My Claims (" + claims.size() + ") ---");
        printClaimTable(claims);
    }

    private void submitMyClaim(String customerId) {
        System.out.println("\n--- Submit New Claim ---");
        String id = input.promptClaimId("Claim ID");
        LocalDateTime claimDate = input.promptDateTime("Claim Date");
        String cardNumber = input.promptString("Card Number: ");
        InsuranceCard card = claimManager.getCardByNumber(cardNumber);
        if (card == null) { System.out.println("  Card not found."); return; }
        LocalDateTime examDate = input.promptDateTime("Exam Date");
        double amount = input.promptPositiveDouble("Claim Amount: $");
        Claim claim = new Claim(id, claimDate, customerId, cardNumber, examDate, amount, "New");
        String error = claimManager.addClaim(claim);
        if (error != null) System.out.println("  Error: " + error);
        else {
            logger.log(customerId, "Submitted claim " + id);
            System.out.println("  Claim submitted.");
        }
    }

    // ==================== SAVE & EXIT ====================

    private void saveAndLogout(User user) {
        System.out.println("\nSaving data...");
        fileManager.saveCustomers(dataDir + "/customers.txt", claimManager.getCustomers());
        fileManager.saveCards(dataDir + "/cards.txt", claimManager.getCards());
        fileManager.saveClaims(dataDir + "/claims.txt", claimManager.getClaims());
        fileManager.saveUsers(dataDir + "/users.txt", userManager.getUsers());
        logger.log(user.getUserId(), "Logout");
        System.out.println("  Data saved. Goodbye!");
    }

    // ==================== DISPLAY HELPERS ====================

    private void printCustomerTable(ArrayList<Customer> list) {
        System.out.printf("  %-12s %-22s %-14s %-12s%n", "ID", "Name", "Type", "Parent PH");
        System.out.println("  " + repeatChar('-', 68));
        for (Customer c : list) {
            String parent = c.getParentPolicyHolderId() == null ? "N/A" : c.getParentPolicyHolderId();
            System.out.printf("  %-12s %-22s %-14s %-12s%n",
                    c.getId(), truncate(c.getFullName(), 21), c.getCustomerTypeLabel(), parent);
        }
    }

    private void printCardTable(ArrayList<InsuranceCard> cards) {
        System.out.printf("  %-12s %-14s %-14s %-20s %-10s%n", "Card No.", "Holder", "Owner", "Expires", "Tier");
        System.out.println("  " + repeatChar('-', 75));
        for (InsuranceCard c : cards) {
            System.out.printf("  %-12s %-14s %-14s %-20s %-10s%n",
                    c.getCardNumber(), c.getCardHolderId(), c.getPolicyOwnerId(),
                    c.getExpirationDate().format(DATE_FMT), c.getMembershipTier().getLabel());
        }
    }

    private void printClaimTable(ArrayList<Claim> list) {
        System.out.printf("  %-15s %-14s %-12s %-12s %-12s %s%n",
                "Claim ID", "Insured", "Card", "Amount", "Status", "Docs");
        System.out.println("  " + repeatChar('-', 80));
        for (Claim c : list) {
            System.out.printf("  %-15s %-14s %-12s $%9.2f  %-12s %d%n",
                    c.getId(), c.getInsuredPersonId(), c.getCardNumber(),
                    c.getClaimAmount(), c.getStatusLabel(), c.getDocuments().size());
        }
    }

    private void printCustomerDetail(Customer c) {
        System.out.println("\n=============================================");
        System.out.println("  Customer Detail");
        System.out.println("=============================================");
        System.out.printf("  ID:          %s%n", c.getId());
        System.out.printf("  Name:        %s%n", c.getFullName());
        System.out.printf("  Type:        %s%n", c.getCustomerTypeLabel());
        System.out.printf("  Parent PH:   %s%n", c.getParentPolicyHolderId() == null ? "N/A" : c.getParentPolicyHolderId());
        if (c.isPolicyHolder()) {
            ArrayList<Customer> deps = claimManager.getDependentsOf(c.getId());
            System.out.printf("  Dependents:  %d%n", deps.size());
            for (Customer dep : deps) {
                System.out.printf("    - %s (%s)%n", dep.getFullName(), dep.getId());
            }
        }
        ArrayList<InsuranceCard> cards = claimManager.getCardsByCustomerId(c.getId());
        System.out.printf("  Cards:       %d%n", cards.size());
        for (InsuranceCard card : cards) {
            System.out.printf("    - %s | Tier: %s | Expires: %s%n", card.getCardNumber(),
                    card.getMembershipTier().getLabel(), card.getExpirationDate().format(DATE_FMT));
        }
        ArrayList<Claim> claims = claimManager.getClaimsByCustomerId(c.getId());
        System.out.printf("  Claims:      %d%n", claims.size());
        for (Claim claim : claims) {
            System.out.printf("    - %s | $%,.2f | %s%n", claim.getId(),
                    claim.getClaimAmount(), claim.getStatusLabel());
        }
        System.out.println("=============================================");
    }

    private String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + ".";
    }

    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) sb.append(c);
        return sb.toString();
    }
}
