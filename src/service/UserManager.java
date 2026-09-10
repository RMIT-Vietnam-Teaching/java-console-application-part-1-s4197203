package service;

import interfaces.UserManageable;
import model.User;
import model.UserRole;
import model.UserStatus;
import util.Validator;

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
        if (!Validator.isValidUserId(user.getUserId())) {
            return "User ID must be 'u-' followed by exactly 7 digits.";
        }
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
    public String deleteUser(String userId) {
        User user = getUserById(userId);
        if (user == null) return "User not found.";
        if (user.getStatus() == UserStatus.INACTIVE) return "User is already inactive.";

        for (User u : users) {
            if (u.getUserId().equals(userId) && u.getRole() == UserRole.ADMIN) {
                int activeAdmins = 0;
                for (User u2 : users) {
                    if (u2.getRole() == UserRole.ADMIN && u2.getStatus() == UserStatus.ACTIVE
                            && !u2.getUserId().equals(userId)) {
                        activeAdmins++;
                    }
                }
                if (activeAdmins == 0) return "Cannot deactivate: this is the last active admin.";
                break;
            }
        }

        user.setStatus(UserStatus.INACTIVE);
        return null;
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
