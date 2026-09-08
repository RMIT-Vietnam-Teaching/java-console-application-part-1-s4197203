package interfaces;

import model.Customer;
import model.CustomerType;
import java.util.ArrayList;

public interface CustomerManageable {
    String addCustomer(Customer customer);
    String updateCustomer(String id, String fullName, String typeLabel, String parentId);
    String deleteCustomer(String id);
    Customer getCustomerById(String id);
    ArrayList<Customer> getCustomers();
    ArrayList<Customer> searchCustomersByName(String keyword);
    ArrayList<Customer> getCustomersByType(CustomerType type);
}
