package service;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ReportService {
    private final ArrayList<Customer> customers;
    private final ArrayList<InsuranceCard> cards;
    private final ArrayList<Claim> claims;

    public ReportService(ArrayList<Customer> customers, ArrayList<InsuranceCard> cards, ArrayList<Claim> claims) {
        this.customers = customers;
        this.cards = cards;
        this.claims = claims;
    }

    public String generateFinancialReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("         Financial Report\n");
        sb.append("========================================\n");

        double totalAmount = 0;
        int newCount = 0, processingCount = 0, doneCount = 0;

        for (Claim c : claims) {
            totalAmount += c.getClaimAmount();
            switch (c.getStatus()) {
                case NEW: newCount++; break;
                case PROCESSING: processingCount++; break;
                case DONE: doneCount++; break;
            }
        }

        sb.append(String.format("  Total Claims:        %d%n", claims.size()));
        sb.append(String.format("  New:                 %d%n", newCount));
        sb.append(String.format("  Processing:          %d%n", processingCount));
        sb.append(String.format("  Done:                %d%n", doneCount));
        sb.append(String.format("  Total Amount:        $%,.2f%n", totalAmount));
        sb.append(String.format("  Average Claim:       $%,.2f%n", claims.isEmpty() ? 0 : totalAmount / claims.size()));

        HashMap<MembershipTier, Integer> tierCounts = new HashMap<>();
        for (Customer c : customers) {
            if (c.isPolicyHolder()) {
                MembershipTier tier = getTierForCustomer(c.getId());
                tierCounts.put(tier, tierCounts.getOrDefault(tier, 0) + 1);
            }
        }
        sb.append("\n  Policy Holders by Tier:\n");
        for (MembershipTier tier : MembershipTier.values()) {
            int count = tierCounts.getOrDefault(tier, 0);
            sb.append(String.format("    %-10s: %d holders%n", tier.getLabel(), count));
        }

        sb.append("========================================\n");
        return sb.toString();
    }

    public String generateOfficerPerformanceReport(ArrayList<User> officers) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("       Officer Performance Report\n");
        sb.append("========================================\n");

        for (User u : officers) {
            if (u.getRole() == UserRole.CLAIMS_OFFICER) {
                sb.append(String.format("  %s (%s)%n", u.getFullName(), u.getUserId()));
                sb.append(String.format("    Status: %s%n", u.getStatus().getLabel()));
            }
        }
        sb.append("========================================\n");
        return sb.toString();
    }

    public String generateTierSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("       Membership Tier Summary\n");
        sb.append("========================================\n");

        for (MembershipTier tier : MembershipTier.values()) {
            int policyHolderCount = 0;
            for (Customer c : customers) {
                if (c.isPolicyHolder() && getTierForCustomer(c.getId()) == tier) {
                    policyHolderCount++;
                }
            }
            sb.append(String.format("  %-10s | Coverage: %d%% | Policy Holders: %d%n",
                    tier.getLabel(), (int)(tier.getCoverageRate() * 100), policyHolderCount));
        }
        sb.append("========================================\n");
        return sb.toString();
    }

    private MembershipTier getTierForCustomer(String customerId) {
        for (InsuranceCard card : cards) {
            if (card.getPolicyOwnerId().equals(customerId)) {
                return card.getMembershipTier();
            }
        }
        return MembershipTier.BASIC;
    }
}
