package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void updateUser(Long userId, User updatedUser) {
        User existing = userRepository.findById(userId).orElse(null);
        if (existing == null) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        // Update fields
        existing.setUsername(updatedUser.getUsername());
        existing.setEmail(updatedUser.getEmail());
        // existing.setPassword(...) passowrd changes? no?
        existing.setRoles(updatedUser.getRoles());
        userRepository.save(existing);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
