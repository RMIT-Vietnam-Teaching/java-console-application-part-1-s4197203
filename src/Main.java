import manager.ClaimManager;
import manager.FileManager;
import model.Claim;
import model.Customer;
import model.InsuranceCard;
import ui.ConsoleUI;

import java.util.ArrayList;

/**
 * Entry point for the ClaimShield Health Insurance Management System.
 * Loads persistent data from text files, initializes the data manager,
 * and launches the interactive console interface.
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

        ClaimManager manager = new ClaimManager();
        manager.setCustomers(customers);
        manager.setCards(cards);
        manager.setClaims(claims);

        System.out.println("  " + customers.size() + " customers loaded.");
        System.out.println("  " + cards.size() + " cards loaded.");
        System.out.println("  " + claims.size() + " claims loaded.");
        System.out.println("========================================\n");

        ConsoleUI ui = new ConsoleUI(manager, dataDir);
        ui.run();
    }
}
