package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.*;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PostgresLargeObjectService largeObjectService;

    @Autowired
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
        return users.stream().map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getUserId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("firstName", user.getFirstName());
            userInfo.put("lastName", user.getLastName());
            userInfo.put("roles", user.getRoles().stream()
                    .map(role -> role.getName().name())
                    .collect(Collectors.toList()));
            return userInfo;
        }).collect(Collectors.toList());
    }

    @Transactional
    public User register(String username, String password, String email,
                        String firstName, String lastName, String role,
                        Double monthlyIncome, String employmentStatus,
                        MultipartFile idFrontImage, MultipartFile idBackImage) throws Exception {
        
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
            
            try {
                if (idFrontImage != null && !idFrontImage.isEmpty()) {
                    Long frontImageOid = largeObjectService.saveImage(idFrontImage.getBytes());
                    tenant.setIdFrontImageOid(frontImageOid);
                }
                
                if (idBackImage != null && !idBackImage.isEmpty()) {
                    Long backImageOid = largeObjectService.saveImage(idBackImage.getBytes());
                    tenant.setIdBackImageOid(backImageOid);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to save ID images: " + e.getMessage());
            }
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
            
            // Delete associated property visits first
            if (user instanceof Tenant) {
                entityManager.createQuery("DELETE FROM PropertyVisit pv WHERE pv.tenant.userId = :userId")
                        .setParameter("userId", userId)
                        .executeUpdate();
            } else if (user instanceof Landlord) {
                entityManager.createQuery("DELETE FROM PropertyVisit pv WHERE pv.landlord.userId = :userId")
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
            
            // Get ID photos
            if (tenant.getIdFrontImageOid() != null) {
                try {
                    byte[] frontPhotoData = largeObjectService.getImage(tenant.getIdFrontImageOid());
                    if (frontPhotoData != null) {
                        details.put("idPhotoFront", Base64.getEncoder().encodeToString(frontPhotoData));
                    }
                } catch (Exception e) {
                    // Log error but continue
                    System.err.println("Error fetching front ID photo: " + e.getMessage());
                }
            }
            
            if (tenant.getIdBackImageOid() != null) {
                try {
                    byte[] backPhotoData = largeObjectService.getImage(tenant.getIdBackImageOid());
                    if (backPhotoData != null) {
                        details.put("idPhotoBack", Base64.getEncoder().encodeToString(backPhotoData));
                    }
                } catch (Exception e) {
                    // Log error but continue
                    System.err.println("Error fetching back ID photo: " + e.getMessage());
                }
            }
        }
        
        // Get user roles
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());
        details.put("roles", roles);

        return details;
    }
}
