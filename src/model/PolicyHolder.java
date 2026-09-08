package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the primary account owner paying for insurance coverage.
 * Contains a list of linked dependents (family members).
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class PolicyHolder extends User {
    private String customerId;
    private List<Dependent> dependents;

    public PolicyHolder(String userId, String username, String password, String fullName, String email,
                        UserStatus status, String customerId) {
        super(userId, username, password, fullName, email, UserRole.CUSTOMER, status);
        this.customerId = customerId;
        this.dependents = new ArrayList<>();
    }

    public String getCustomerId() { return customerId; }

    public List<Dependent> getDependents() { return dependents; }

    public void addDependent(Dependent dependent) {
        for (Dependent d : dependents) {
            if (d.getCustomerId().equals(dependent.getCustomerId())) return;
        }
        dependents.add(dependent);
    }

    public void removeDependent(String customerId) {
        dependents.removeIf(d -> d.getCustomerId().equals(customerId));
    }

    public int getDependentCount() { return dependents.size(); }

    public List<String> getDependentCustomerIds() {
        List<String> ids = new ArrayList<>();
        for (Dependent d : dependents) {
            ids.add(d.getCustomerId());
        }
        return ids;
    }

    @Override
    public void displayDashboard() {
        System.out.println("\n=============================================");
        System.out.println("  CUSTOMER DASHBOARD - " + getFullName());
        System.out.println("=============================================");
        System.out.println("  Role:       Policy Holder");
        System.out.println("  User ID:    " + getUserId());
        System.out.println("  Customer ID:" + customerId);
        System.out.println("  Email:      " + getEmail());
        System.out.println("  Dependents: " + dependents.size());
        System.out.println("=============================================");
        System.out.println("  Capabilities:");
        System.out.println("    - View Profile, Cards, Claims");
        System.out.println("    - Submit New Claims");
        System.out.println("    - Track Membership Tier Status");
        System.out.println("=============================================");
    }

    public static PolicyHolder fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 8) return null;
        PolicyHolder ph = new PolicyHolder(
            parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(),
            parts[4].trim(), UserStatus.fromLabel(parts[6].trim()), parts[7] != null ? parts[7].trim() : null
        );
        return ph;
    }

    @Override
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s|%s|%s|%s",
                getUserId(), getUsername(), getPassword(), getFullName(), getEmail(),
                role.getLabel(), status.getLabel(), customerId);
    }
}
