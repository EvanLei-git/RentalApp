package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.*;
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
    public void register(String username, String email, String rawPassword, String firstName, String lastName, String roleString) {
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
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }
        if (roleString == null || roleString.trim().isEmpty()) {
            throw new RuntimeException("Role is required");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            System.out.println("Username already exists: " + username);
            throw new RuntimeException("Username already exists");
        }

        // Create appropriate user type based on role
        User user;
        switch (roleString.toUpperCase()) {
            case "TENANT":
                Tenant tenant = new Tenant();
                tenant.setUsername(username);
                tenant.setEmail(email);
                tenant.setPassword(passwordEncoder.encode(rawPassword));
                tenant.setEmploymentStatus("Not Specified"); // Default value
                tenant.setMonthlyIncome(0.0); // Default value
                user = tenant;
                break;
            case "LANDLORD":
                Landlord landlord = new Landlord();
                landlord.setUsername(username);
                landlord.setEmail(email);
                landlord.setPassword(passwordEncoder.encode(rawPassword));
                user = landlord;
                break;
            case "ADMINISTRATOR":
                Administrator admin = new Administrator();
                admin.setUsername(username);
                admin.setEmail(email);
                admin.setPassword(passwordEncoder.encode(rawPassword));
                user = admin;
                break;
            default:
                throw new RuntimeException("Invalid role: " + roleString);
        }

        // Set first and last name
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Find and set role
        Role role = roleRepository.findByName(RoleType.valueOf(roleString.toUpperCase()))
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleString));
        user.getRoles().add(role);

        // Save user
        userRepository.save(user);
        System.out.println("Successfully registered user: " + username);
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
