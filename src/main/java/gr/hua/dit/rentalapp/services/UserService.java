package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long saveUser(User user, String roleName) {
        // Encrypt the user's password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign the specified role to the user
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        // Save the user and return their ID
        return userRepository.save(user).getUserId();
    }

    @Transactional
    public Long updateUser(User user) {
        // Save the updated user information
        return userRepository.save(user).getUserId();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find the user by username (email)
        Optional<User> opt = userRepository.findByUsername(username);

        if (opt.isEmpty())
            throw new UsernameNotFoundException("User with username: " + username + " not found!");
        else {
            User user = opt.get();
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getRoles()
                            .stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                            .collect(Collectors.toSet())
            );
        }
    }

    @Transactional
    public Iterable<User> getUsers() {
        // Retrieve all users
        return userRepository.findAll();
    }

    public Optional<User> getUser(Long userId) {
        // Retrieve a specific user by ID
        return userRepository.findById(userId);
    }

    @Transactional
    public void updateOrInsertRole(Role role) {
        // Update or insert a role
        roleRepository.save(role);
    }
}