package model;

public class ClaimsOfficer extends User {
    private String officerName;

    public ClaimsOfficer(String userId, String password, String fullName, UserStatus status) {
        super(userId, password, fullName, UserRole.CLAIMS_OFFICER, status);
        this.officerName = fullName;
    }

    public String getOfficerName() { return officerName; }

    public static ClaimsOfficer fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length < 5) return null;
        return new ClaimsOfficer(
            parts[0].trim(), parts[1].trim(), parts[2].trim(),
            UserStatus.fromLabel(parts[4].trim())
        );
    }

    @Override
    public String toFileString() {
        return String.format("%s|%s|%s|%s|%s", userId, password, fullName, role.getLabel(), status.getLabel());
    }
}
