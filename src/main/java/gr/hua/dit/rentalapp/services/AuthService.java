package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        // Encrypt password
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Create a basic User
        User user = new User(username, email, encodedPassword){};

        // Find the role by its name
        RoleType roleType = RoleType.valueOf(roleString); // e.g., "ROLE_TENANT"
        Optional<Role> roleOptional = roleRepository.findByName(roleType);

        if (roleOptional.isEmpty()) {
            throw new RuntimeException("Role not found: " + roleString);
        }

        // Unwrap the role and add it to the user's roles
        Role role = roleOptional.get();
        user.getRoles().add(role);

        // Save the user
        userRepository.save(user);
    }

    /**
     * Login: Validate credentials and return a token (JWT or session).
     * For now, we'll just do a minimal password check and return a dummy token.
     */
    public String login(String username, String rawPassword) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new RuntimeException("Invalid username or password.");
        }
        // Check password
        if (!passwordEncoder.matches(rawPassword, user.get().getPassword())) {
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
