package service;

import model.*;

import java.util.ArrayList;

public class CustomerService {
    private ArrayList<Customer> customers;

    public CustomerService() {
        this.customers = new ArrayList<>();
    }

    public void setCustomers(ArrayList<Customer> customers) {
        this.customers = customers;
    }

    public ArrayList<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    public Customer getCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public ArrayList<Customer> getCustomersByType(CustomerType type) {
        ArrayList<Customer> result = new ArrayList<>();
        for (Customer c : customers) {
            if (c.getCustomerType() == type) result.add(c);
        }
        return result;
    }
}
