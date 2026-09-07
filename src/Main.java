import manager.ClaimManager;
import manager.FileManager;
import model.*;
import service.ActivityLogger;
import service.AuthenticationService;
import service.UserManager;
import ui.ConsoleUI;

import java.util.ArrayList;

/**
 * Entry point for the ClaimShield Health Insurance Management System.
 * Loads persistent data, authenticates the user, and launches
 * the role-appropriate console interface.
 *
 * @author Nguyen Khanh Nguyen - s4197203
 */
public class Main {
    public static void main(String[] args) {
        String dataDir = "data";
        FileManager fileManager = new FileManager();

        System.out.println("========================================");
        System.out.println("   ClaimShield - Insurance Management");
        System.out.println("========================================");
        System.out.println("Loading data...");

        ArrayList<Customer> customers = fileManager.loadCustomers(dataDir + "/customers.txt");
        ArrayList<InsuranceCard> cards = fileManager.loadCards(dataDir + "/cards.txt");
        ArrayList<Claim> claims = fileManager.loadClaims(dataDir + "/claims.txt");
        ArrayList<User> users = fileManager.loadUsers(dataDir + "/users.txt");

        ClaimManager claimManager = new ClaimManager();
        claimManager.setCustomers(customers);
        claimManager.setCards(cards);
        claimManager.setClaims(claims);

        UserManager userManager = new UserManager();
        userManager.setUsers(users);

        ActivityLogger logger = new ActivityLogger(dataDir + "/logs.txt");

        System.out.println("  " + customers.size() + " customers loaded.");
        System.out.println("  " + cards.size() + " cards loaded.");
        System.out.println("  " + claims.size() + " claims loaded.");
        System.out.println("  " + users.size() + " users loaded.");
        System.out.println("========================================\n");

        AuthenticationService authService = new AuthenticationService(users);

        ConsoleUI ui = new ConsoleUI(claimManager, userManager, authService, logger, dataDir);
        ui.run();
    }
}
