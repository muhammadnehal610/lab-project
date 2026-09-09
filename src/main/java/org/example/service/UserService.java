package org.example.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.util.SessionManager;
import org.example.util.UserSession;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    private final UserRepository userRepository = new UserRepository();

    public boolean authenticateUser(String email, String rawPassword) {
        if (email == null || email.trim().isEmpty() || rawPassword == null || rawPassword.trim().isEmpty()) {
            return false;
        }

        try {
            String storedPassword = userRepository.findPasswordByEmail(email.trim());

            if (storedPassword == null) {
                return false;
            }

            BCrypt.Result result = BCrypt.verifyer().verify(rawPassword.toCharArray(), storedPassword);

            if (result.verified) {
                User user = userRepository.getUserByEmail(email.trim());
                UserSession.getInstance().login(user);
                SessionManager.saveSession(user.getEmail());
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean registerUser(String firstName , String lastName , String email , String rawPassword){
        if (firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                rawPassword == null || rawPassword.trim().isEmpty()) {
            return false;
        }

        try {
            if (userRepository.emailExist(email.trim())) {
                return false;
            }

            String hashPassword = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());

            User newUser = new User(firstName, lastName, email, hashPassword, "USER");
            userRepository.createUser(newUser);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}