package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with the given credentials and role name.

     */
    public void register(String username, String email, String rawPassword, String roleString) {
        System.out.println("Attempting to register user: " + username + " with role: " + roleString);
        
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (roleString == null || roleString.trim().isEmpty()) {
            throw new RuntimeException("Role is required");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            System.out.println("Username already exists: " + username);
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            System.out.println("Email already exists: " + email);
            throw new RuntimeException("Email already exists");
        }

        try {
            // Encrypt password
            String encodedPassword = passwordEncoder.encode(rawPassword);

            // Create a new User instance
            User user = new User(username, email, encodedPassword);

            // Find the role by its name
            RoleType roleType = RoleType.valueOf(roleString.toUpperCase());
            Optional<Role> roleOptional = roleRepository.findByName(roleType);

            if (roleOptional.isEmpty()) {
                System.out.println("Role not found: " + roleString);
                throw new RuntimeException("Role not found: " + roleString);
            }

            // Unwrap the role and add it to the user's roles
            Role role = roleOptional.get();
            user.getRoles().add(role);

            // Save the user
            userRepository.save(user);
            System.out.println("Successfully registered user: " + username);
            
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid role type: " + roleString);
            throw new RuntimeException("Invalid role type: " + roleString);
        } catch (Exception e) {
            System.out.println("Error registering user: " + e.getMessage());
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }

    /**
     * Login: Validate credentials and return a token (JWT or session).
     * For now, we'll just do a minimal password check and return a dummy token.
     */
    public Map<String, String> login(String username, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username or password.");
        }
        
        User user = userOpt.get();
        // Check password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid username or password.");
        }

        Map<String, String> response = new HashMap<>();
        response.put("token", "jwt-dummy-token-for-" + username);
        
        // Get the user's role
        String userRole = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().toString())
                .orElse("TENANT"); // Default role
                
        response.put("role", userRole);
        
        return response;
    }

    /**
     * Logout: If you have session-based or token invalidation logic, do it here.
     */
    public void logout() {
        // TODO: Invalidate session or JWT token if needed
    }
}
