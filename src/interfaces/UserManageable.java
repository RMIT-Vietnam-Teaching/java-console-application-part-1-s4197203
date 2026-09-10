package interfaces;

import model.User;
import java.util.ArrayList;

public interface UserManageable {
    String addUser(User user);
    String updateUser(String userId, String fullName, String statusLabel);
    String deleteUser(String userId);
    User getUserById(String userId);
    ArrayList<User> getUsers();
}
