package manager;

import model.Claim;
import model.ClaimStatus;
import model.Customer;
import model.CustomerType;
import model.InsuranceCard;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Handles file I/O operations for loading and saving system data.
 * Uses pipe-delimited text files with ISO-8601 date serialization.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class FileManager {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String DELIMITER = "\\|";
    private static final String PIPE = "|";

    /**
     * Loads customers from a pipe-delimited text file.
     * Format: id|fullName|customerType|parentPolicyHolderId
     *
     * @param filePath path to the customers data file
     * @return list of parsed Customer objects
     */
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
                    customers.add(new Customer(parts[0], parts[1], type, parentId));
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping invalid customer at line " + lineNum + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers from " + filePath + ": " + e.getMessage());
        }
        return customers;
    }

    /**
     * Loads insurance cards from a pipe-delimited text file.
     * Format: cardNumber|cardHolderId|policyOwnerId|expirationDate
     *
     * @param filePath path to the cards data file
     * @return list of parsed InsuranceCard objects
     */
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

    /**
     * Loads claims from a pipe-delimited text file.
     * Format: id|claimDate|insuredPersonId|cardNumber|examDate|doc1;doc2|amount|status
     *
     * @param filePath path to the claims data file
     * @return list of parsed Claim objects
     */
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

    /**
     * Saves all customers to a pipe-delimited text file.
     */
    public void saveCustomers(String filePath, ArrayList<Customer> customers) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Customer c : customers) {
                String parentId = c.getParentPolicyHolderId() == null ? "null" : c.getParentPolicyHolderId();
                writer.println(c.getId() + PIPE + c.getFullName() + PIPE +
                        c.getCustomerTypeLabel() + PIPE + parentId);
            }
        } catch (IOException e) {
            System.err.println("Error saving customers to " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Saves all insurance cards to a pipe-delimited text file.
     */
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

    /**
     * Saves all claims to a pipe-delimited text file.
     */
    public void saveClaims(String filePath, ArrayList<Claim> claims) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Claim claim : claims) {
                String docs = claim.getDocuments().isEmpty() ? "null" : String.join(";", claim.getDocuments());
                writer.println(claim.getId() + PIPE + claim.getClaimDate().format(DATE_FORMATTER) + PIPE +
                        claim.getInsuredPersonId() + PIPE + claim.getCardNumber() + PIPE +
                        claim.getExamDate().format(DATE_FORMATTER) + PIPE + docs + PIPE +
                        claim.getClaimAmount() + PIPE + claim.getStatusLabel());
            }
        } catch (IOException e) {
            System.err.println("Error saving claims to " + filePath + ": " + e.getMessage());
        }
    }

    private void createEmptyFile(File file) {
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Could not create data file: " + file.getPath());
        }
    }
}
