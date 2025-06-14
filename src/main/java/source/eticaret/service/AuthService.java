package source.eticaret.service;

import source.eticaret.model.User;
import source.eticaret.repository.UserRepository;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.time.LocalDateTime;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AuthService {
    private static AuthService instance;
    private final UserRepository userRepository;

    private User currentUser;
    private LocalDateTime loginTime;


    private static final long SESSION_TIMEOUT_MINUTES = 30;

    private AuthService(DatabaseService databaseService) {
        this.userRepository = new UserRepository(databaseService);
    }

    public static AuthService getInstance(DatabaseService databaseService) {
        if (instance == null) {
            instance = new AuthService(databaseService);
        }
        return instance;
    }

    public boolean login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (verifyPassword(password, user.getPasswordHash())) {
                currentUser = user;
                loginTime = LocalDateTime.now();
                return true;
            }
        }
        return false;
    }

    public boolean register(String username, String email, String password, String roleName) {
        if (userRepository.findByUsername(username).isPresent()) {
            return false;
        }
        User newUser  = new User();
        newUser .setUsername(username);
        newUser .setEmail(email);
        newUser .setPasswordHash(hashPassword(password));
        newUser .setRoleName(roleName);
        return userRepository.save(newUser );
    }

    public void logout() {
        currentUser = null;
        loginTime = null;
    }

    public boolean isUserLoggedIn() {
        if (currentUser == null) return false;
        if (loginTime == null) return false;

        LocalDateTime now = LocalDateTime.now();
        return !loginTime.plusMinutes(SESSION_TIMEOUT_MINUTES).isBefore(now);
    }

    public User getCurrentUser() {
        if (!isUserLoggedIn()) {
            logout();
            return null;
        }
        return currentUser;
    }


    private String hashPassword(String password) {
        try {

            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);
            

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            

            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);
            

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }
    

    private boolean verifyPassword(String password, String storedHash) {
        try {

            byte[] combined = Base64.getDecoder().decode(storedHash);
            

            byte[] salt = new byte[16];
            byte[] hash = new byte[combined.length - salt.length];
            System.arraycopy(combined, 0, salt, 0, salt.length);
            System.arraycopy(combined, salt.length, hash, 0, hash.length);
            

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 10000, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] testHash = factory.generateSecret(spec).getEncoded();
            

            return Arrays.equals(hash, testHash);
        } catch (Exception e) {
            return false;
        }
    }
}