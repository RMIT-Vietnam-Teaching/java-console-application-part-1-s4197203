package service;

import exceptions.AuthenticationException;
import model.User;

import java.util.ArrayList;

public class AuthenticationService {
    private final ArrayList<User> users;
    private User currentUser;

    public AuthenticationService(ArrayList<User> users) {
        this.users = users;
    }

    public User login(String username, String password) throws AuthenticationException {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                if (!user.isActive()) {
                    throw new AuthenticationException("Account is inactive. Contact administrator.");
                }
                if (user.getPassword().equals(password)) {
                    currentUser = user;
                    return user;
                }
                throw new AuthenticationException("Invalid username or password.");
            }
        }
        throw new AuthenticationException("Invalid username or password.");
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }
}
