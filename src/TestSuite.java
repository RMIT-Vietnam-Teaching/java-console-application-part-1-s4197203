import model.*;
import manager.*;
import service.*;
import exceptions.*;
import util.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class TestSuite {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  ClaimShield - Automated Test Suite");
        System.out.println("========================================\n");

        testValidator();
        testModels();
        testEnums();
        testFileManager();
        testClaimManager();
        testUserManager();
        testAuthentication();
        testActivityLogger();
        testReportService();
        testExceptionHandling();

        System.out.println("\n========================================");
        System.out.printf("  RESULTS: %d passed, %d failed%n", passed, failed);
        System.out.println("========================================");
        if (failed == 0) System.out.println("  ALL TESTS PASSED!");
        else System.out.println("  SOME TESTS FAILED!");
    }

    static void check(String name, boolean condition) {
        if (condition) { passed++; System.out.println("  PASS: " + name); }
        else { failed++; System.out.println("  FAIL: " + name); }
    }

    static void testValidator() {
        System.out.println("\n--- Validator Tests ---");
        check("isValidCustomerId valid", Validator.isValidCustomerId("c-1000001"));
        check("isValidCustomerId invalid null", !Validator.isValidCustomerId(null));
        check("isValidCustomerId invalid format", !Validator.isValidCustomerId("bad"));
        check("isValidClaimId valid", Validator.isValidClaimId("f-1234567890"));
        check("isValidClaimId invalid", !Validator.isValidClaimId("bad"));
        check("isValidCardNumber valid", Validator.isValidCardNumber("1000000001"));
        check("isValidCardNumber invalid", !Validator.isValidCardNumber("123"));
        check("isValidUserId valid", Validator.isValidUserId("u-1000001"));
        check("isValidUserId invalid", !Validator.isValidUserId("bad"));
        check("isPositiveAmount valid", Validator.isPositiveAmount(100));
        check("isPositiveAmount zero", !Validator.isPositiveAmount(0));
        check("isPositiveAmount negative", !Validator.isPositiveAmount(-1));
        check("isValidDocumentName valid", Validator.isValidDocumentName("f-1234567890_1000000001_Doc.pdf", "f-1234567890", "1000000001"));
        check("isValidDocumentName wrong prefix", !Validator.isValidDocumentName("wrong_1000000001_Doc.pdf", "f-1234567890", "1000000001"));
        check("isValidDocumentName no pdf", !Validator.isValidDocumentName("f-1234567890_1000000001_Doc.txt", "f-1234567890", "1000000001"));
        check("isValidUsername valid", Validator.isValidUsername("admin01"));
        check("isValidUsername too short", !Validator.isValidUsername("ab"));
        check("isValidPassword valid", Validator.isValidPassword("pass123"));
        check("isValidPassword too short", !Validator.isValidPassword("12345"));
        check("isValidEmail valid", Validator.isValidEmail("test@test.com"));
        check("isValidEmail no at", !Validator.isValidEmail("test"));
    }

    static void testModels() {
        System.out.println("\n--- Model Tests ---");
        Customer c1 = new Customer("c-1000001", "Test PH", CustomerType.POLICY_HOLDER, null);
        check("Customer ID format", c1.getId().equals("c-1000001"));
        check("Customer isPolicyHolder", c1.isPolicyHolder());
        check("Customer isDependent false", !c1.isDependent());
        check("Customer total starts at 0", c1.getTotalApprovedClaimAmount() == 0);
        check("Customer tier starts STANDARD", c1.getMembershipTier() == MembershipTier.STANDARD);
        c1.addApprovedClaimAmount(2500);
        check("Customer addApprovedClaimAmount", c1.getTotalApprovedClaimAmount() == 2500);
        check("Customer tier SILVER after 2500", c1.getMembershipTier() == MembershipTier.SILVER);
        Customer c2 = new Customer("c-2000001", "Test Dep", CustomerType.DEPENDENT, "c-1000001");
        check("Dependent has parent", c2.getParentPolicyHolderId().equals("c-1000001"));
        InsuranceCard card = new InsuranceCard("1000000001", "c-1000001", "c-1000001", LocalDateTime.of(2027, 12, 31, 23, 59));
        check("Card number", card.getCardNumber().equals("1000000001"));
        Claim claim = new Claim("f-1234567890", LocalDateTime.of(2026, 6, 15, 10, 30), "c-1000001", "1000000001", LocalDateTime.of(2026, 6, 14, 9, 0), 1500, ClaimStatus.DONE);
        check("Claim ID", claim.getId().equals("f-1234567890"));
        check("Claim processedBy null", claim.getProcessedBy() == null);
        Admin admin = new Admin("u-001", "admin01", "pass", "Admin", "a@t.com", UserStatus.ACTIVE);
        check("Admin role", admin.getRole() == UserRole.ADMIN);
        admin.setStatus(UserStatus.INACTIVE);
        check("Admin inactive", !admin.isActive());
        PolicyHolder ph = new PolicyHolder("u-003", "ph01", "pass", "PH", "p@t.com", UserStatus.ACTIVE, "c-1000001");
        check("PolicyHolder customerId", ph.getCustomerId().equals("c-1000001"));
        Dependent dep = new Dependent("u-004", "dep01", "pass", "Dep", "d@t.com", UserStatus.ACTIVE, "c-2000001", "c-1000001");
        ph.addDependent(dep);
        check("PolicyHolder addDependent", ph.getDependents().size() == 1);
        ph.removeDependent("c-2000001");
        check("PolicyHolder removeDependent", ph.getDependents().isEmpty());
        String adminFile = admin.toFileString();
        Admin parsed = Admin.fromFileString(adminFile);
        check("Admin fromFileString", parsed != null && parsed.getUserId().equals("u-001"));
        ClaimsOfficer off = new ClaimsOfficer("u-002", "off01", "pass", "Off", "o@t.com", UserStatus.ACTIVE);
        ClaimsOfficer parsedOff = ClaimsOfficer.fromFileString(off.toFileString());
        check("ClaimsOfficer fromFileString", parsedOff != null);
        PolicyHolder parsedPH = PolicyHolder.fromFileString(ph.toFileString());
        check("PolicyHolder fromFileString", parsedPH != null && parsedPH.getCustomerId().equals("c-1000001"));
        Dependent parsedDep = Dependent.fromFileString(dep.toFileString());
        check("Dependent fromFileString", parsedDep != null && parsedDep.getParentPolicyHolderId().equals("c-1000001"));
    }

    static boolean fromLabelThrows() {
        try { ClaimStatus.fromLabel("X"); return false; } catch (IllegalArgumentException e) { return true; }
    }

    static void testEnums() {
        System.out.println("\n--- Enum Tests ---");
        check("ClaimStatus NEW->PROCESSING valid", ClaimStatus.NEW.canTransitionTo(ClaimStatus.PROCESSING));
        check("ClaimStatus PROCESSING->DONE valid", ClaimStatus.PROCESSING.canTransitionTo(ClaimStatus.DONE));
        check("ClaimStatus NEW->DONE invalid", !ClaimStatus.NEW.canTransitionTo(ClaimStatus.DONE));
        check("ClaimStatus DONE->NEW invalid", !ClaimStatus.DONE.canTransitionTo(ClaimStatus.NEW));
        check("ClaimStatus fromLabel New", ClaimStatus.fromLabel("New") == ClaimStatus.NEW);
        check("ClaimStatus fromLabel invalid throws", fromLabelThrows());
        check("CustomerType fromLabel PolicyHolder", CustomerType.fromLabel("PolicyHolder") == CustomerType.POLICY_HOLDER);
        check("UserRole fromLabel Admin", UserRole.fromLabel("Admin") == UserRole.ADMIN);
        check("UserStatus fromLabel Active", UserStatus.fromLabel("Active") == UserStatus.ACTIVE);
        check("MembershipTier BASE_COPAY_RATE 0.30", MembershipTier.BASE_COPAY_RATE == 0.30);
        check("MembershipTier STANDARD rate 0.30", MembershipTier.STANDARD.getEffectiveCoPayRate() == 0.30);
        check("MembershipTier SILVER rate 0.285", MembershipTier.SILVER.getEffectiveCoPayRate() == 0.285);
        check("MembershipTier GOLD rate 0.27", MembershipTier.GOLD.getEffectiveCoPayRate() == 0.27);
        check("MembershipTier PLATINUM rate 0.255", MembershipTier.PLATINUM.getEffectiveCoPayRate() == 0.255);
        check("MembershipTier determineTier $0 -> STANDARD", MembershipTier.determineTier(0) == MembershipTier.STANDARD);
        check("MembershipTier determineTier $2000 -> SILVER", MembershipTier.determineTier(2000) == MembershipTier.SILVER);
        check("MembershipTier determineTier $5000 -> GOLD", MembershipTier.determineTier(5000) == MembershipTier.GOLD);
        check("MembershipTier determineTier $10000 -> PLATINUM", MembershipTier.determineTier(10000) == MembershipTier.PLATINUM);
    }

    static void testFileManager() {
        System.out.println("\n--- FileManager Tests ---");
        FileManager fm = new FileManager();
        ArrayList<Customer> customers = fm.loadCustomers("data/customers.txt");
        check("Load customers >= 20", customers.size() >= 20);
        ArrayList<InsuranceCard> cards = fm.loadCards("data/cards.txt");
        check("Load cards >= 23", cards.size() >= 23);
        ArrayList<Claim> claims = fm.loadClaims("data/claims.txt");
        check("Load claims >= 41", claims.size() >= 41);
        ArrayList<User> users = fm.loadUsers("data/users.txt");
        check("Load users >= 26", users.size() >= 26);
        fm.saveCustomers("data/test_customers.txt", customers);
        check("Save/reload customers", fm.loadCustomers("data/test_customers.txt").size() == customers.size());
        fm.saveCards("data/test_cards.txt", cards);
        check("Save/reload cards", fm.loadCards("data/test_cards.txt").size() == cards.size());
        fm.saveClaims("data/test_claims.txt", claims);
        check("Save/reload claims", fm.loadClaims("data/test_claims.txt").size() == claims.size());
        fm.saveUsers("data/test_users.txt", users);
        check("Save/reload users", fm.loadUsers("data/test_users.txt").size() == users.size());
        new java.io.File("data/test_customers.txt").delete();
        new java.io.File("data/test_cards.txt").delete();
        new java.io.File("data/test_claims.txt").delete();
        new java.io.File("data/test_users.txt").delete();
    }

    static void testClaimManager() {
        System.out.println("\n--- ClaimManager Tests ---");
        ClaimManager cm = new ClaimManager();
        FileManager fm = new FileManager();
        cm.setCustomers(fm.loadCustomers("data/customers.txt"));
        cm.setCards(fm.loadCards("data/cards.txt"));
        cm.setClaims(fm.loadClaims("data/claims.txt"));
        check("getCustomers not empty", !cm.getCustomers().isEmpty());
        check("getCardsByCustomerId returns cards", !cm.getCardsByCustomerId("c-1000001").isEmpty());
        check("addCustomer success", cm.addCustomer(new Customer("c-1000099", "T", CustomerType.POLICY_HOLDER, null)) == null);
        check("addCustomer duplicate rejected", cm.addCustomer(new Customer("c-1000099", "T", CustomerType.POLICY_HOLDER, null)) != null);
        check("deleteCustomer success", cm.deleteCustomer("c-1000099") == null);
        check("addCard success", cm.addCard(new InsuranceCard("1999999999", "c-1000001", "c-1000001", LocalDateTime.of(2029, 1, 1, 0, 0))) == null);
        check("addCard duplicate rejected", cm.addCard(new InsuranceCard("1999999999", "c-1000001", "c-1000001", LocalDateTime.of(2029, 1, 1, 0, 0))) != null);
        check("deleteCard success", cm.deleteCard("1999999999") == null);
        Claim newClaim = new Claim("f-9999999999", LocalDateTime.of(2026, 8, 1, 10, 0), "c-1000001", "1000000001", LocalDateTime.of(2026, 7, 31, 9, 0), 500, "New");
        check("addClaim success", cm.addClaim(newClaim) == null);
        check("addClaim duplicate rejected", cm.addClaim(new Claim("f-9999999999", LocalDateTime.of(2026, 8, 1, 10, 0), "c-1000001", "1000000001", LocalDateTime.of(2026, 7, 31, 9, 0), 500, "New")) != null);
        try {
            check("updateClaimStatus NEW->PROCESSING", cm.updateClaimStatus("f-9999999999", "Processing", "off01") == null);
            check("updateClaimStatus processedBy", cm.getClaimById("f-9999999999").getProcessedBy().equals("off01"));
            check("updateClaimStatus PROCESSING->DONE", cm.updateClaimStatus("f-9999999999", "Done", "off01") == null);
        } catch (InvalidStatusTransitionException e) { check("status update", false); }
        boolean threw = false;
        try { cm.updateClaimStatus("f-9999999999", "New", "off01"); } catch (InvalidStatusTransitionException e) { threw = true; }
        check("DONE->NEW throws", threw);
        check("DONE claim immutability - no docs", cm.addDocumentToClaim("f-1234567890", "f-1234567890_1000000001_New.pdf").contains("DONE"));
        Claim processingClaim = null;
        for (Claim c : cm.getClaims()) {
            if (c.getStatus() == ClaimStatus.PROCESSING) { processingClaim = c; break; }
        }
        if (processingClaim != null) {
            check("addDocument valid on processing claim", cm.addDocumentToClaim(processingClaim.getId(), processingClaim.getId() + "_" + processingClaim.getCardNumber() + "_TestDoc.pdf") == null);
        } else {
            check("addDocument valid (skipped, no processing claim)", true);
        }
        check("addDocument invalid name", cm.addDocumentToClaim("f-9999999999", "bad.pdf") != null);
        check("calculateCoPay returns value", cm.calculateCoPay(cm.getClaims().get(0)) > 0);
        check("calculateInsurancePayout returns value", cm.calculateInsurancePayout(cm.getClaims().get(0)) > 0);
        String stats = cm.getSystemStatistics();
        check("getSystemStatistics not empty", stats != null && !stats.isEmpty());
    }

    static void testUserManager() {
        System.out.println("\n--- UserManager Tests ---");
        UserManager um = new UserManager();
        FileManager fm = new FileManager();
        um.setUsers(fm.loadUsers("data/users.txt"));
        check("getUsers not empty", !um.getUsers().isEmpty());
        check("getUserById exists", um.getUserById("admin01") != null);
        check("getUsersByRole ADMIN", !um.getUsersByRole(UserRole.ADMIN).isEmpty());
        check("addUser success", um.addUser(new ClaimsOfficer("test-u1", "ta", "pass", "T", "t@t.com", UserStatus.ACTIVE)) == null);
        check("addUser duplicate rejected", um.addUser(new ClaimsOfficer("test-u1", "ta", "pass", "T", "t@t.com", UserStatus.ACTIVE)) != null);
        check("updateUser success", um.updateUser("test-u1", "New Name", null) == null);
        um.deleteUser("admin02");
        String adminDeleteResult = um.deleteUser("admin01");
        check("deleteUser last admin rejected", adminDeleteResult != null && adminDeleteResult.contains("last active admin"));
        um.deleteUser("test-u1");
        check("deleteUser success", um.getUserById("test-u1").getStatus() == UserStatus.INACTIVE);
    }

    static void testAuthentication() {
        System.out.println("\n--- Authentication Tests ---");
        FileManager fm = new FileManager();
        ArrayList<User> users = fm.loadUsers("data/users.txt");
        AuthenticationService auth = new AuthenticationService(users);
        boolean threw = false;
        try { auth.login("admin01", "admin123"); check("login valid", auth.getCurrentUser() != null); } catch (AuthenticationException e) { check("login valid", false); }
        threw = false;
        try { auth.login("admin01", "wrong"); } catch (AuthenticationException e) { threw = true; }
        check("login wrong password fails", threw);
        threw = false;
        try { auth.login("nonexistent", "pass"); } catch (AuthenticationException e) { threw = true; }
        check("login nonexistent fails", threw);
        auth.logout();
        check("logout clears", auth.getCurrentUser() == null);
    }

    static void testActivityLogger() {
        System.out.println("\n--- ActivityLogger Tests ---");
        ActivityLogger logger = new ActivityLogger("data/logs.txt");
        logger.log("test-user", "Test Action", "test-entity-123");
        ArrayList<String> logs = logger.getRecentLogs(5);
        check("log entry written", !logs.isEmpty());
        check("log has targetEntityId", logs.get(logs.size() - 1).contains("test-entity-123"));
    }

    static void testReportService() {
        System.out.println("\n--- ReportService Tests ---");
        FileManager fm = new FileManager();
        ArrayList<Customer> customers = fm.loadCustomers("data/customers.txt");
        ArrayList<InsuranceCard> cards = fm.loadCards("data/cards.txt");
        ArrayList<Claim> claims = fm.loadClaims("data/claims.txt");
        ReportService report = new ReportService(customers, cards, claims);
        check("generateFinancialReport", report.generateFinancialReport() != null && !report.generateFinancialReport().isEmpty());
        check("generateFinancialReportByDateRange", report.generateFinancialReportByDateRange(LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 12, 31, 23, 59)) != null);
        UserManager um = new UserManager();
        um.setUsers(fm.loadUsers("data/users.txt"));
        check("generateOfficerPerformanceReport", report.generateOfficerPerformanceReport(um.getUsersByRole(UserRole.CLAIMS_OFFICER)) != null);
        check("generateTierSummary", report.generateTierSummary() != null);
        check("generateFilteredClaimsReport", report.generateFilteredClaimsReport(ClaimStatus.DONE, null, null, null) != null);
    }

    static void testExceptionHandling() {
        System.out.println("\n--- Exception Handling Tests ---");
        check("InvalidStatusTransitionException msg", new InvalidStatusTransitionException("test").getMessage().equals("test"));
        check("InvalidClaimDateException msg", new InvalidClaimDateException("date").getMessage().equals("date"));
        check("AuthenticationException msg", new AuthenticationException("auth").getMessage().equals("auth"));
        ClaimManager cm = new ClaimManager();
        FileManager fm = new FileManager();
        cm.setCustomers(fm.loadCustomers("data/customers.txt"));
        cm.setCards(fm.loadCards("data/cards.txt"));
        cm.setClaims(fm.loadClaims("data/claims.txt"));
        check("examDate after claimDate rejected", cm.addClaim(new Claim("f-9999999997", LocalDateTime.of(2026, 8, 1, 10, 0), "c-1000001", "1000000001", LocalDateTime.of(2026, 8, 2, 9, 0), 500, "New")) != null);
        check("examDate after expiry rejected", cm.addClaim(new Claim("f-9999999996", LocalDateTime.of(2029, 1, 1, 10, 0), "c-1000001", "1000000001", LocalDateTime.of(2028, 12, 31, 9, 0), 500, "New")) != null);
    }
}
