package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user, String roleStr) {
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set role
        RoleType roleType = RoleType.valueOf(roleStr.toUpperCase());
        Role role = roleRepository.findByName(roleType);
        if (role == null) {
            // Create role if it doesn't exist
            role = new Role(roleType);
            role = roleRepository.save(role);
        }
        
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }

    public void updateUser(Long userId, User updatedUser) {
        User existing = userRepository.findById(userId).orElse(null);
        if (existing == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        existing.setUsername(updatedUser.getUsername());
        existing.setEmail(updatedUser.getEmail());
        existing.setRoles(updatedUser.getRoles());
        userRepository.save(existing);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
