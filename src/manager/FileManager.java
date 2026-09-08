package manager;

import model.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Handles file I/O operations for loading and saving system data.
 * Uses pipe-delimited text files with ISO-8601 date serialization.
 *
 * Updated formats:
 *   users.txt:    userId|username|password|fullName|email|role|status|customerId[|dependents]
 *   customers.txt: id|fullName|customerType|parentPolicyHolderId|cardReference|totalApprovedClaimAmount
 *   cards.txt:    cardNumber|cardHolderId|policyOwnerId|expirationDate
 *   claims.txt:   id|claimDate|insuredPersonId|cardNumber|examDate|documents|amount|status
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class FileManager {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String DELIMITER = "\\|";
    private static final String PIPE = "|";

    // ==================== CUSTOMERS ====================

    public ArrayList<Customer> loadCustomers(String filePath) {
        ArrayList<Customer> customers = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            createEmptyFile(file);
            return customers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    String[] parts = line.split(DELIMITER);
                    if (parts.length < 4) {
                        System.err.println("Skipping malformed customer record at line " + lineNum);
                        continue;
                    }
                    String parentId = parts[3].equals("null") ? null : parts[3];
                    CustomerType type = CustomerType.fromLabel(parts[2]);
                    String cardNum = parts.length > 4 && !parts[4].equals("null") ? parts[4] : null;
                    double totalApproved = parts.length > 5 ? Double.parseDouble(parts[5]) : 0.0;
                    Customer c = new Customer(parts[0], parts[1], type, parentId, null, totalApproved);
                    c.setCardNumberForLoading(cardNum);
                    customers.add(c);
                } catch (Exception e) {
                    System.err.println("Skipping invalid customer at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers from " + filePath + ": " + e.getMessage());
        }
        return customers;
    }

    public void saveCustomers(String filePath, ArrayList<Customer> customers) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Customer c : customers) {
                String parentId = c.getParentPolicyHolderId() == null ? "null" : c.getParentPolicyHolderId();
                String cardNum = c.getInsuranceCard() != null ? c.getInsuranceCard().getCardNumber() : "null";
                writer.println(c.getId() + PIPE + c.getFullName() + PIPE +
                        c.getCustomerTypeLabel() + PIPE + parentId + PIPE +
                        cardNum + PIPE + String.format("%.2f", c.getTotalApprovedClaimAmount()));
            }
        } catch (IOException e) {
            System.err.println("Error saving customers to " + filePath + ": " + e.getMessage());
        }
    }

    // ==================== INSURANCE CARDS ====================

    public ArrayList<InsuranceCard> loadCards(String filePath) {
        ArrayList<InsuranceCard> cards = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            createEmptyFile(file);
            return cards;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    String[] parts = line.split(DELIMITER);
                    if (parts.length < 4) {
                        System.err.println("Skipping malformed card record at line " + lineNum);
                        continue;
                    }
                    LocalDateTime expDate = LocalDateTime.parse(parts[3], DATE_FORMATTER);
                    cards.add(new InsuranceCard(parts[0], parts[1], parts[2], expDate));
                } catch (Exception e) {
                    System.err.println("Skipping invalid card at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading cards from " + filePath + ": " + e.getMessage());
        }
        return cards;
    }

    public void saveCards(String filePath, ArrayList<InsuranceCard> cards) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (InsuranceCard card : cards) {
                writer.println(card.getCardNumber() + PIPE + card.getCardHolderId() + PIPE +
                        card.getPolicyOwnerId() + PIPE + card.getExpirationDate().format(DATE_FORMATTER));
            }
        } catch (IOException e) {
            System.err.println("Error saving cards to " + filePath + ": " + e.getMessage());
        }
    }

    // ==================== CLAIMS ====================

    public ArrayList<Claim> loadClaims(String filePath) {
        ArrayList<Claim> claims = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            createEmptyFile(file);
            return claims;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    String[] parts = line.split(DELIMITER);
                    if (parts.length < 8) {
                        System.err.println("Skipping malformed claim record at line " + lineNum);
                        continue;
                    }
                    LocalDateTime claimDate = LocalDateTime.parse(parts[1], DATE_FORMATTER);
                    LocalDateTime examDate = LocalDateTime.parse(parts[4], DATE_FORMATTER);
                    double amount = Double.parseDouble(parts[6]);
                    ClaimStatus status = ClaimStatus.fromLabel(parts[7]);
                    Claim claim = new Claim(parts[0], claimDate, parts[2], parts[3], examDate, amount, status);
                    if (!parts[5].equals("null") && !parts[5].isEmpty()) {
                        String[] docs = parts[5].split(";");
                        claim.getDocuments().addAll(Arrays.asList(docs));
                    }
                    if (parts.length > 8 && !parts[8].trim().isEmpty() && !parts[8].trim().equals("null")) {
                        claim.setProcessedBy(parts[8].trim());
                    }
                    claims.add(claim);
                } catch (Exception e) {
                    System.err.println("Skipping invalid claim at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading claims from " + filePath + ": " + e.getMessage());
        }
        return claims;
    }

    public void saveClaims(String filePath, ArrayList<Claim> claims) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Claim claim : claims) {
                String docs = claim.getDocuments().isEmpty() ? "null" : String.join(";", claim.getDocuments());
                String processedBy = claim.getProcessedBy() == null ? "null" : claim.getProcessedBy();
                writer.println(claim.getId() + PIPE + claim.getClaimDate().format(DATE_FORMATTER) + PIPE +
                        claim.getInsuredPersonId() + PIPE + claim.getCardNumber() + PIPE +
                        claim.getExamDate().format(DATE_FORMATTER) + PIPE + docs + PIPE +
                        claim.getClaimAmount() + PIPE + claim.getStatusLabel() + PIPE + processedBy);
            }
        } catch (IOException e) {
            System.err.println("Error saving claims to " + filePath + ": " + e.getMessage());
        }
    }

    // ==================== USERS ====================

    public ArrayList<User> loadUsers(String filePath) {
        ArrayList<User> users = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            createEmptyFile(file);
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    String[] parts = line.split(DELIMITER);
                    if (parts.length < 7) {
                        System.err.println("Skipping malformed user record at line " + lineNum);
                        continue;
                    }
                    String role = parts[5].trim();
                    User user = null;
                    switch (role) {
                        case "Admin":
                            user = Admin.fromFileString(line);
                            break;
                        case "ClaimsOfficer":
                            user = ClaimsOfficer.fromFileString(line);
                            break;
                        case "Customer":
                            String customerId = parts.length > 7 ? parts[7].trim() : null;
                            user = new PolicyHolder(parts[0].trim(), parts[1].trim(), parts[2].trim(),
                                    parts[3].trim(), parts[4].trim(),
                                    UserStatus.fromLabel(parts[6].trim()), customerId);
                            break;
                        default:
                            System.err.println("Unknown role '" + role + "' at line " + lineNum);
                    }
                    if (user != null) {
                        users.add(user);
                    } else {
                        System.err.println("Skipping invalid user at line " + lineNum);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing user at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users from " + filePath + ": " + e.getMessage());
        }
        return users;
    }

    public void saveUsers(String filePath, ArrayList<User> users) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (User user : users) {
                writer.println(user.toFileString());
            }
        } catch (IOException e) {
            System.err.println("Error saving users to " + filePath + ": " + e.getMessage());
        }
    }

    // ==================== ACTIVITY LOGS ====================

    public ArrayList<String> loadLogs(String filePath) {
        ArrayList<String> logs = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            createEmptyFile(file);
            return logs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    logs.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading logs from " + filePath + ": " + e.getMessage());
        }
        return logs;
    }

    public void saveLogs(String filePath, ArrayList<String> logs) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (String log : logs) {
                writer.println(log);
            }
        } catch (IOException e) {
            System.err.println("Error saving logs to " + filePath + ": " + e.getMessage());
        }
    }

    // ==================== UTILITY ====================

    private void createEmptyFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Could not create data file: " + file.getPath());
        }
    }
}
