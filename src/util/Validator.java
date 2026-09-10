package util;

import java.util.regex.Pattern;

/**
 * Centralized validation utility for ClaimShield.
 * All ID formats, field constraints, and business rule checks are defined here.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public final class Validator {
    private Validator() {}

    public static final String CUSTOMER_ID_REGEX = "c-\\d{7}";
    public static final String CLAIM_ID_REGEX = "f-\\d{10}";
    public static final String CARD_NUMBER_REGEX = "\\d{10}";
    public static final String USER_ID_REGEX = "[a-z]{3,5}\\d{2}";

    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 50;
    public static final int PASSWORD_MIN = 6;
    public static final int PASSWORD_MAX = 100;

    private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile(CUSTOMER_ID_REGEX);
    private static final Pattern CLAIM_ID_PATTERN = Pattern.compile(CLAIM_ID_REGEX);
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile(CARD_NUMBER_REGEX);
    private static final Pattern USER_ID_PATTERN = Pattern.compile(USER_ID_REGEX);

    public static boolean isValidCustomerId(String id) {
        return id != null && CUSTOMER_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidClaimId(String id) {
        return id != null && CLAIM_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isValidCardNumber(String number) {
        return number != null && CARD_NUMBER_PATTERN.matcher(number).matches();
    }

    public static boolean isValidUserId(String id) {
        return id != null && USER_ID_PATTERN.matcher(id).matches();
    }

    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    public static boolean isValidDocumentName(String fileName, String claimId, String cardNumber) {
        if (fileName == null || claimId == null || cardNumber == null) return false;
        String expectedPrefix = claimId + "_" + cardNumber + "_";
        return fileName.startsWith(expectedPrefix) && fileName.endsWith(".pdf");
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.length() >= USERNAME_MIN && username.length() <= USERNAME_MAX;
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= PASSWORD_MIN && password.length() <= PASSWORD_MAX;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
