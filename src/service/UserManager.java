package service;

import interfaces.UserManageable;
import model.User;
import model.UserRole;
import model.UserStatus;

import java.util.ArrayList;

public class UserManager implements UserManageable {
    private ArrayList<User> users;

    public UserManager() {
        this.users = new ArrayList<>();
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    @Override
    public String addUser(User user) {
        if (getUserById(user.getUserId()) != null) return "User ID already exists.";
        users.add(user);
        return null;
    }

    @Override
    public String updateUser(String userId, String fullName, String statusLabel) {
        User user = getUserById(userId);
        if (user == null) return "User not found.";
        if (fullName != null && !fullName.isEmpty()) user.setFullName(fullName);
        if (statusLabel != null && !statusLabel.isEmpty()) user.setStatus(UserStatus.fromLabel(statusLabel));
        return null;
    }

    @Override
    public void deleteUser(String userId) {
        users.removeIf(u -> u.getUserId().equals(userId));
    }

    @Override
    public User getUserById(String userId) {
        for (User u : users) {
            if (u.getUserId().equals(userId)) return u;
        }
        return null;
    }

    @Override
    public ArrayList<User> getUsers() {
        return new ArrayList<>(users);
    }

    public ArrayList<User> getUsersByRole(UserRole role) {
        ArrayList<User> result = new ArrayList<>();
        for (User u : users) {
            if (u.getRole() == role) result.add(u);
        }
        return result;
    }
}
