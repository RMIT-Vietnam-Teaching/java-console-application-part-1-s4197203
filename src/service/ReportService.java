package service;

import model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Generates analytics and managerial reports for the ClaimShield system.
 * Includes financial summaries, officer performance, and tier-based breakdowns.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class ReportService {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final ArrayList<Customer> customers;
    private final ArrayList<InsuranceCard> cards;
    private final ArrayList<Claim> claims;

    public ReportService(ArrayList<Customer> customers, ArrayList<InsuranceCard> cards, ArrayList<Claim> claims) {
        this.customers = customers;
        this.cards = cards;
        this.claims = claims;
    }

    /**
     * Generates a comprehensive financial report with totals by status and tier breakdown.
     *
     * @return formatted financial report string
     */
    public String generateFinancialReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("         Financial Report\n");
        sb.append("========================================\n");

        double totalAmount = 0;
        int newCount = 0, processingCount = 0, doneCount = 0;
        double totalCoPay = 0, totalPayout = 0;

        for (Claim c : claims) {
            totalAmount += c.getClaimAmount();
            switch (c.getStatus()) {
                case NEW: newCount++; break;
                case PROCESSING: processingCount++; break;
                case DONE:
                    doneCount++;
                    Customer cust = getCustomerById(c.getInsuredPersonId());
                    if (cust != null) {
                        MembershipTier tier = cust.getMembershipTier();
                        totalCoPay += calculateCoPay(c.getClaimAmount(), tier);
                        totalPayout += calculatePayout(c.getClaimAmount(), tier);
                    }
                    break;
            }
        }

        sb.append(String.format("  Total Claims:         %d%n", claims.size()));
        sb.append(String.format("    New:                %d%n", newCount));
        sb.append(String.format("    Processing:         %d%n", processingCount));
        sb.append(String.format("    Done:               %d%n", doneCount));
        sb.append(String.format("  Total Claim Value:    $%,.2f%n", totalAmount));
        sb.append(String.format("  Average Claim:        $%,.2f%n", claims.isEmpty() ? 0 : totalAmount / claims.size()));
        sb.append(String.format("  Total Co-Pay (Done):  $%,.2f%n", totalCoPay));
        sb.append(String.format("  Total Payout (Done):  $%,.2f%n", totalPayout));

        sb.append("\n  Policy Holders by Tier:\n");
        HashMap<MembershipTier, Integer> tierCounts = new HashMap<>();
        for (Customer c : customers) {
            if (c.isPolicyHolder()) {
                MembershipTier tier = c.getMembershipTier();
                tierCounts.put(tier, tierCounts.getOrDefault(tier, 0) + 1);
            }
        }
        for (MembershipTier tier : MembershipTier.values()) {
            int count = tierCounts.getOrDefault(tier, 0);
            sb.append(String.format("    %-12s: %d holders (co-pay rate: %.1f%%)%n",
                    tier.getLabel(), count, tier.getEffectiveCoPayRate() * 100));
        }

        sb.append("========================================\n");
        return sb.toString();
    }

    /**
     * Generates a financial report filtered by date range.
     *
     * @param start the start of the date range (inclusive)
     * @param end   the end of the date range (inclusive)
     * @return formatted date-range financial report
     */
    public String generateFinancialReportByDateRange(LocalDateTime start, LocalDateTime end) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("    Financial Report (Date Range)\n");
        sb.append("  From: ").append(start.format(DATE_FMT)).append("\n");
        sb.append("  To:   ").append(end.format(DATE_FMT)).append("\n");
        sb.append("========================================\n");

        double totalAmount = 0, totalCoPay = 0, totalPayout = 0;
        int count = 0;

        for (Claim c : claims) {
            if (c.getStatus() == ClaimStatus.DONE &&
                !c.getClaimDate().isBefore(start) && !c.getClaimDate().isAfter(end)) {
                totalAmount += c.getClaimAmount();
                Customer cust = getCustomerById(c.getInsuredPersonId());
                if (cust != null) {
                    MembershipTier tier = cust.getMembershipTier();
                    totalCoPay += calculateCoPay(c.getClaimAmount(), tier);
                    totalPayout += calculatePayout(c.getClaimAmount(), tier);
                }
                count++;
            }
        }

        sb.append(String.format("  Approved Claims in Range: %d%n", count));
        sb.append(String.format("  Total Claim Value:        $%,.2f%n", totalAmount));
        sb.append(String.format("  Total Customer Co-Pay:    $%,.2f%n", totalCoPay));
        sb.append(String.format("  Total Insurance Payout:   $%,.2f%n", totalPayout));
        sb.append("========================================\n");
        return sb.toString();
    }

    /**
     * Generates a report showing total claim payouts and co-pays processed per Claims Officer.
     *
     * @param officers list of claims officers
     * @return formatted officer performance report
     */
    public String generateOfficerPerformanceReport(ArrayList<User> officers) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("       Officer Performance Report\n");
        sb.append("========================================\n");

        for (User u : officers) {
            if (u.getRole() == UserRole.CLAIMS_OFFICER) {
                int processedCount = 0;
                double totalCoPay = 0, totalPayout = 0;
                for (Claim c : claims) {
                    if (u.getUserId().equals(c.getProcessedBy()) && c.getStatus() == ClaimStatus.DONE) {
                        processedCount++;
                        Customer cust = getCustomerById(c.getInsuredPersonId());
                        if (cust != null) {
                            MembershipTier tier = cust.getMembershipTier();
                            totalCoPay += calculateCoPay(c.getClaimAmount(), tier);
                            totalPayout += calculatePayout(c.getClaimAmount(), tier);
                        }
                    }
                }
                sb.append(String.format("  %s (%s)%n", u.getFullName(), u.getUserId()));
                sb.append(String.format("    Status: %s | Email: %s%n", u.getStatus().getLabel(), u.getEmail()));
                sb.append(String.format("    Claims Processed (Done): %d%n", processedCount));
                sb.append(String.format("    Total Co-Pay Collected:  $%,.2f%n", totalCoPay));
                sb.append(String.format("    Total Payout Disbursed:  $%,.2f%n", totalPayout));
            }
        }
        sb.append("========================================\n");
        return sb.toString();
    }

    /**
     * Generates a tier-based financial summary showing total claims, insurance payouts,
     * and customer co-pay savings categorized by Membership Tier.
     *
     * @return formatted tier summary report
     */
    public String generateTierSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("       Membership Tier Summary\n");
        sb.append("========================================\n");

        HashMap<MembershipTier, double[]> tierStats = new HashMap<>();
        for (MembershipTier tier : MembershipTier.values()) {
            tierStats.put(tier, new double[]{0, 0, 0, 0});
        }

        for (Claim c : claims) {
            if (c.getStatus() == ClaimStatus.DONE) {
                Customer cust = getCustomerById(c.getInsuredPersonId());
                if (cust != null) {
                    MembershipTier tier = cust.getMembershipTier();
                    double[] stats = tierStats.get(tier);
                    stats[0] += c.getClaimAmount();
                    stats[1] += calculateCoPay(c.getClaimAmount(), tier);
                    stats[2] += calculatePayout(c.getClaimAmount(), tier);
                    stats[3]++;
                }
            }
        }

        int policyHolderCount;
        for (MembershipTier tier : MembershipTier.values()) {
            policyHolderCount = 0;
            for (Customer c : customers) {
                if (c.isPolicyHolder() && c.getMembershipTier() == tier) {
                    policyHolderCount++;
                }
            }
            double[] stats = tierStats.get(tier);
            sb.append(String.format("  %-12s | Coverage: %d%% | Co-Pay Rate: %.1f%% | Discount: %.0f%%%n",
                    tier.getLabel(), (int)((1.0 - tier.getEffectiveCoPayRate()) * 100),
                    tier.getEffectiveCoPayRate() * 100, tier.getTierDiscount() * 100));
            sb.append(String.format("    Policy Holders: %d | Approved Claims: %.0f%n",
                    policyHolderCount, stats[3]));
            sb.append(String.format("    Total Claims:   $%,.2f | Co-Pay: $%,.2f | Payout: $%,.2f%n",
                    stats[0], stats[1], stats[2]));
        }
        sb.append("========================================\n");
        return sb.toString();
    }

    /**
     * Lists all claims filtered by status, date range, or policyholder family group.
     *
     * @param statusFilter  optional status filter (null for all)
     * @param startDate     optional start date (null for no lower bound)
     * @param endDate       optional end date (null for no upper bound)
     * @param familyGroupId optional policyholder ID for family group filter (null for all)
     * @return formatted filtered claims report
     */
    public String generateFilteredClaimsReport(ClaimStatus statusFilter, LocalDateTime startDate,
                                                LocalDateTime endDate, String familyGroupId) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("       Filtered Claims Report\n");
        sb.append("========================================\n");

        ArrayList<Claim> filtered = new ArrayList<>();
        for (Claim c : claims) {
            boolean matches = true;
            if (statusFilter != null && c.getStatus() != statusFilter) matches = false;
            if (startDate != null && c.getClaimDate().isBefore(startDate)) matches = false;
            if (endDate != null && c.getClaimDate().isAfter(endDate)) matches = false;
            if (familyGroupId != null) {
                Customer ph = getCustomerById(familyGroupId);
                if (ph != null && ph.isPolicyHolder()) {
                    ArrayList<String> familyIds = new ArrayList<>();
                    familyIds.add(familyGroupId);
                    for (Customer dep : getDependentsOf(familyGroupId)) {
                        familyIds.add(dep.getId());
                    }
                    if (!familyIds.contains(c.getInsuredPersonId())) matches = false;
                } else {
                    matches = false;
                }
            }
            if (matches) filtered.add(c);
        }

        sb.append(String.format("  Filtered Claims: %d total%n", filtered.size()));
        if (filtered.isEmpty()) {
            sb.append("  No claims match the specified filters.\n");
        } else {
            double totalAmount = 0, totalCoPay = 0, totalPayout = 0;
            for (Claim c : filtered) {
                sb.append(String.format("  %-15s | %-12s | %-14s | $%9.2f | %s%n",
                        c.getId(), c.getStatusLabel(), c.getInsuredPersonId(),
                        c.getClaimAmount(), c.getClaimDate().format(DATE_FMT)));
                totalAmount += c.getClaimAmount();
                if (c.getStatus() == ClaimStatus.DONE) {
                    Customer cust = getCustomerById(c.getInsuredPersonId());
                    if (cust != null) {
                        MembershipTier tier = cust.getMembershipTier();
                        totalCoPay += calculateCoPay(c.getClaimAmount(), tier);
                        totalPayout += calculatePayout(c.getClaimAmount(), tier);
                    }
                }
            }
            sb.append(String.format("  Total Claim Value:  $%,.2f%n", totalAmount));
            sb.append(String.format("  Total Co-Pay:       $%,.2f%n", totalCoPay));
            sb.append(String.format("  Total Payout:       $%,.2f%n", totalPayout));
        }
        sb.append("========================================\n");
        return sb.toString();
    }

    // ==================== HELPER METHODS ====================

    private double calculateCoPay(double claimAmount, MembershipTier tier) {
        return Math.round(claimAmount * tier.getEffectiveCoPayRate() * 100.0) / 100.0;
    }

    private double calculatePayout(double claimAmount, MembershipTier tier) {
        return Math.round(claimAmount * (1.0 - tier.getEffectiveCoPayRate()) * 100.0) / 100.0;
    }

    private Customer getCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    private ArrayList<Customer> getDependentsOf(String policyHolderId) {
        ArrayList<Customer> results = new ArrayList<>();
        for (Customer c : customers) {
            if (c.isDependent() && policyHolderId.equals(c.getParentPolicyHolderId())) {
                results.add(c);
            }
        }
        return results;
    }
}
