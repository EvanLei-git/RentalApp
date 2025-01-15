package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.*;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserAuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserAuthService(UserRepository userRepository,
                         RoleRepository roleRepository,
                         BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // User Management Methods
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public void updateUser(Long userId, User updatedUser) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Update basic information
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            existing.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existing.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null && !updatedUser.getFirstName().isEmpty()) {
            existing.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null && !updatedUser.getLastName().isEmpty()) {
            existing.setLastName(updatedUser.getLastName());
        }

        // Update password if provided
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // Update roles if provided
        if (updatedUser.getRoles() != null && !updatedUser.getRoles().isEmpty()) {
            existing.setRoles(updatedUser.getRoles());
        }

        userRepository.save(existing);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }

    // Authentication Methods
    /**
     * Register a new user with the given credentials and role name.
     */
    @Transactional
    public void register(String username, String email, String rawPassword, String firstName, String lastName, String roleString, Double monthlyIncome, String employmentStatus) {
        System.out.println("Attempting to register user: " + username + " with role: " + roleString);

        // Validate inputs
        validateRegistrationInput(username, email, rawPassword, firstName, lastName, roleString);

        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            System.out.println("Username already exists: " + username);
            throw new RuntimeException("Username already exists");
        }

        // Create appropriate user type based on role
        User user = createUserByRole(username, email, rawPassword, roleString, monthlyIncome, employmentStatus);

        // Set first and last name
        user.setFirstName(firstName);
        user.setLastName(lastName);

        // Find and set role
        Role role = roleRepository.findByName(RoleType.valueOf(roleString.toUpperCase()))
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleString));
        user.getRoles().add(role);

        // Save the user
        userRepository.save(user);
        System.out.println("Successfully registered user: " + username);
    }


    /**
     * Authenticate user and return login response.
     */
    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // In a real application, you would generate a JWT token here
        Map<String, String> response = new HashMap<>();
        response.put("token", "dummy-token-" + username); // Replace with actual JWT token
        response.put("role", user.getRoles().iterator().next().getName().toString());
        return response;
    }

    /**
     * Save a user with encoded password and default role.
     */
    @Transactional
    public Long saveUser(User user) {
        // Encode password
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Set default role if none exists
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Role defaultRole = roleRepository.findByName(RoleType.TENANT)
                    .orElseThrow(() -> new RuntimeException("Error: Default role TENANT not found."));
            Set<Role> roles = new HashSet<>();
            roles.add(defaultRole);
            user.setRoles(roles);
        }

        user = userRepository.save(user);
        return user.getUserId();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }

    // Helper Methods
    private void validateRegistrationInput(String username, String email, String rawPassword, 
                                         String firstName, String lastName, String roleString) {
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
    }

    private User createUserByRole(String username, String email, String rawPassword, String roleString, Double monthlyIncome, String employmentStatus) {
        User user;
        switch (roleString.toUpperCase()) {
            case "TENANT":
                Tenant tenant = new Tenant();
                tenant.setUsername(username);
                tenant.setEmail(email);
                tenant.setPassword(passwordEncoder.encode(rawPassword));
                tenant.setEmploymentStatus(employmentStatus != null ? employmentStatus : "Not Specified"); // Default value
                tenant.setMonthlyIncome(monthlyIncome != null ? monthlyIncome : 0.0); // Default value
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
        return user;
    }


    public void logout() {
        // TODO: Invalidate session
    }
}
