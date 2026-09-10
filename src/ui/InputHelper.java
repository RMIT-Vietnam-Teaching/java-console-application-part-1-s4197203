package ui;

import util.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Utility class for validated console input operations.
 * Provides methods to read and validate common data types from standard input.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class InputHelper {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompts the user for a non-empty string input.
     *
     * @param message the prompt to display
     * @return the trimmed user input
     */
    public String promptString(String message) {
        while (true) {
            System.out.print(message);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("  Input cannot be empty. Please try again.");
        }
    }

    /**
     * Prompts for a string input that may be empty (returns null for empty).
     *
     * @param message the prompt to display
     * @return the trimmed input or null if empty
     */
    public String promptOptionalString(String message) {
        System.out.print(message + " (press Enter to skip): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? null : input;
    }

    /**
     * Prompts for an integer within a specified range.
     */
    public int promptInt(String message, int min, int max) {
        while (true) {
            System.out.print(message);
            try {
                int value = Integer.parseInt(scanner.nextLine().trim());
                if (value >= min && value <= max) return value;
                System.out.println("  Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid number. Please try again.");
            }
        }
    }

    /**
     * Prompts for a positive double value.
     */
    public double promptPositiveDouble(String message) {
        while (true) {
            System.out.print(message);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value > 0) return value;
                System.out.println("  Amount must be positive. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("  Invalid number. Please try again.");
            }
        }
    }

    /**
     * Prompts for a LocalDateTime in ISO-8601 format.
     */
    public LocalDateTime promptDateTime(String message) {
        while (true) {
            System.out.print(message + " (yyyy-MM-ddTHH:mm:ss): ");
            try {
                return LocalDateTime.parse(scanner.nextLine().trim(), DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use: yyyy-MM-ddTHH:mm:ss (e.g., 2026-07-10T14:30:00)");
            }
        }
    }

    /**
     * Prompts for a customer ID with format validation.
     */
    public String promptCustomerId(String message) {
        while (true) {
            System.out.print(message + " (c-XXXXXXX): ");
            String input = scanner.nextLine().trim();
            if (Validator.isValidCustomerId(input)) return input;
            System.out.println("  Invalid format. Customer ID must be: c- followed by 7 digits (e.g., c-1234567).");
        }
    }

    /**
     * Prompts for a claim ID with format validation.
     */
    public String promptClaimId(String message) {
        while (true) {
            System.out.print(message + " (f-XXXXXXXXXX): ");
            String input = scanner.nextLine().trim();
            if (Validator.isValidClaimId(input)) return input;
            System.out.println("  Invalid format. Claim ID must be: f- followed by 10 digits (e.g., f-1234567890).");
        }
    }

    /**
     * Prompts for a 10-digit card number.
     */
    public String promptCardNumber(String message) {
        while (true) {
            System.out.print(message + " (10 digits): ");
            String input = scanner.nextLine().trim();
            if (input.matches("\\d{10}")) return input;
            System.out.println("  Card number must be exactly 10 digits.");
        }
    }

    /**
     * Prompts for a yes/no confirmation.
     *
     * @param message the confirmation prompt
     * @return true if user confirms (y/yes)
     */
    public boolean confirm(String message) {
        System.out.print(message + " (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");
    }
}
