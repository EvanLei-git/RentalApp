package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        // Encrypt password
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create a basic User or a specific subclass (Tenant, Landlord, Administrator)
        // For simplicity, let's just do a "User" here. 
        // If you have separate methods to create Tenant/Landlord/Administrator, call them instead.
        User user = new User(username, email, encodedPassword) {

        };

        // Find or create the role
        RoleType roleType = RoleType.valueOf(roleString); // e.g., "ROLE_TENANT"
        Role role = roleRepository.findByName(roleType);
        if (role == null) {
            throw new RuntimeException("Role not found: " + roleString);
        }
        
        user.getRoles().add(role);

        // Save user
        userRepository.save(user);
    }

    /**
     * Login: Validate credentials and return a token (JWT or session).
     * For now, we'll just do a minimal password check and return a dummy token.
     */
    public String login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("Invalid username or password.");
        }
        // Check password
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid username or password.");
        }
        // TODO: Generate a JWT or session
        return "jwt-dummy-token-for-" + username;
    }

    /**
     * Logout: If you have session-based or token invalidation logic, do it here.
     */
    public void logout() {
        // TODO: Invalidate session or JWT token if needed
    }
}
