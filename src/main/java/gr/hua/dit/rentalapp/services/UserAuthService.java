package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.*;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.stream.Stream;

@Service
@Transactional
public class UserAuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
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

    public Map<String, String> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        Map<String, String> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRoles().iterator().next().getName().toString());
        return response;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Map<String, Object>> getAllUsersInfo() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertUserToMap).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getFilteredUsers(String usernameFilter, String roleFilter, Boolean verifiedFilter) {
        List<User> users = userRepository.findAll();

        Stream<User> filteredStream = users.stream();

        // Filter by username if provided
        if (usernameFilter != null && !usernameFilter.isEmpty()) {
            filteredStream = filteredStream.filter(user ->
                    user.getUsername().toLowerCase().startsWith(usernameFilter.toLowerCase()));
        }

        // Filter by role if provided
        if (roleFilter != null && !roleFilter.isEmpty()) {
            filteredStream = filteredStream.filter(user ->
                    user.getRoles().stream()
                            .anyMatch(role -> role.getName().name().equalsIgnoreCase(roleFilter)));
        }

        // Filter by verification status if provided
        if (verifiedFilter != null) {
            filteredStream = filteredStream.filter(user -> {
                if (user instanceof Landlord) {
                    return ((Landlord) user).getVerifiedBy() != null == verifiedFilter;
                } else if (user instanceof Tenant) {
                    return ((Tenant) user).getVerifiedBy() != null == verifiedFilter;
                }
                return true; // Administrators are always considered verified
            });
        }

        return filteredStream.map(this::convertUserToMap).collect(Collectors.toList());
    }

    private Map<String, Object> convertUserToMap(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getUserId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList()));

        // Add verification status
        if (user instanceof Landlord) {
            userInfo.put("verified", ((Landlord) user).getVerifiedBy() != null);
            userInfo.put("userType", "LANDLORD");
        } else if (user instanceof Tenant) {
            userInfo.put("verified", ((Tenant) user).getVerifiedBy() != null);
            userInfo.put("userType", "TENANT");
        } else if (user instanceof Administrator) {
            userInfo.put("verified", true);
            userInfo.put("userType", "ADMINISTRATOR");
        }

        return userInfo;
    }

    @Transactional
    public User register(String username, String password, String email,
                         String firstName, String lastName, String role,
                         Double monthlyIncome, String employmentStatus) throws Exception {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        User user;
        RoleType roleType;

        if ("tenant".equalsIgnoreCase(role)) {
            roleType = RoleType.TENANT;
            Tenant tenant = new Tenant();
            tenant.setMonthlyIncome(monthlyIncome);
            tenant.setEmploymentStatus(employmentStatus);
            user = tenant;
        } else if ("landlord".equalsIgnoreCase(role)) {
            roleType = RoleType.LANDLORD;
            user = new Landlord();
        } else {
            throw new IllegalArgumentException("Invalid role specified: " + role);
        }

        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        Role userRole = roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRoles(Collections.singleton(userRole));

        return userRepository.save(user);
    }

    public User updateUser(Long userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Check if user is not an administrator
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(role -> role.getName().name().equals("ADMINISTRATOR"));
            if (isAdmin) {
                throw new RuntimeException("Cannot delete administrator accounts");
            }

            // Delete all reports created by the user
            entityManager.createQuery("DELETE FROM Report r WHERE r.user.userId = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();

            // If the user is a tenant, delete their rental applications first
            if (user instanceof Tenant) {
                entityManager.createQuery("DELETE FROM RentalApplication ra WHERE ra.applicant.userId = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();

                // Delete tenant's visits
                entityManager.createQuery("DELETE FROM PropertyVisit pv WHERE pv.tenant.userId = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();
            }

            // If the user is a landlord, handle their properties
            if (user instanceof Landlord) {
                // Get all properties owned by this landlord
                List<Long> propertyIds = entityManager.createQuery(
                                "SELECT p.propertyId FROM Property p WHERE p.owner.userId = :userId", Long.class)
                        .setParameter("userId", userId)
                        .getResultList();

                // Delete all rental applications for these properties
                if (!propertyIds.isEmpty()) {
                    entityManager.createQuery(
                                    "DELETE FROM RentalApplication ra WHERE ra.property.propertyId IN :propertyIds")
                            .setParameter("propertyIds", propertyIds)
                            .executeUpdate();

                    // Delete all visits for these properties
                    entityManager.createQuery(
                                    "DELETE FROM PropertyVisit pv WHERE pv.property.propertyId IN :propertyIds")
                            .setParameter("propertyIds", propertyIds)
                            .executeUpdate();
                }

                // Now delete the properties
                entityManager.createQuery("DELETE FROM Property p WHERE p.owner.userId = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();
            }

            // Clear the user's roles
            user.getRoles().clear();
            userRepository.save(user);

            // Flush changes before final deletion
            entityManager.flush();
            entityManager.clear();

            // Now delete the user
            userRepository.delete(user);

        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage());
        }
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public Map<String, Object> getUserDetailsById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> details = new HashMap<>();
        details.put("id", user.getUserId());
        details.put("username", user.getUsername());
        details.put("email", user.getEmail());
        details.put("firstName", user.getFirstName());
        details.put("lastName", user.getLastName());

        if (user instanceof Tenant) {
            Tenant tenant = (Tenant) user;
            details.put("monthlyIncome", tenant.getMonthlyIncome());
            details.put("employmentStatus", tenant.getEmploymentStatus());
        }

        // Get user roles
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        details.put("roles", roles);

        return details;
    }

    @Transactional
    public void verifyUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Get the current admin user
        String adminUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Check if the current user has admin role
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMINISTRATOR"));

        if (!isAdmin) {
            throw new RuntimeException("Only administrators can verify users");
        }

        // Cast to Administrator if it's the correct type
        Administrator admin;
        if (currentUser instanceof Administrator) {
            admin = (Administrator) currentUser;
        } else {
            // Create a new Administrator instance if the user has admin role but isn't an Administrator instance
            admin = new Administrator();
            admin.setUserId(currentUser.getUserId());
            admin.setUsername(currentUser.getUsername());
            admin.setEmail(currentUser.getEmail());
            admin.setFirstName(currentUser.getFirstName());
            admin.setLastName(currentUser.getLastName());
            admin.setPassword(currentUser.getPassword());
            admin.setRoles(currentUser.getRoles());
            admin = (Administrator) userRepository.save(admin);
        }

        if (user instanceof Landlord) {
            ((Landlord) user).setVerifiedBy(admin);
            ((Landlord) user).setVerified(true);
        } else if (user instanceof Tenant) {
            ((Tenant) user).setVerifiedBy(admin);
            ((Tenant) user).setVerified(true);
        } else {
            throw new RuntimeException("Only Landlords and Tenants can be verified");
        }

        userRepository.save(user);
    }
}
