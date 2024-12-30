package gr.hua.dit.rentalapp.controllers;

import jakarta.annotation.PostConstruct;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RoleRepository roleRepository;
    private final AuthService authService;

    @Autowired
    public AuthController(RoleRepository roleRepository, AuthService authService) {
        this.roleRepository = roleRepository;
        this.authService = authService;
    }


    @PostConstruct
    public void setupDefaultRoles() {
        // Administrator
        if (roleRepository.findByName(RoleType.ADMIN) == null) {
            roleRepository.save(new Role(RoleType.ADMIN));
        }
        // Landlord
        if (roleRepository.findByName(RoleType.LANDLORD) == null) {
            roleRepository.save(new Role(RoleType.LANDLORD));
        }
        // Tenant
        if (roleRepository.findByName(RoleType.TENANT) == null) {
            roleRepository.save(new Role(RoleType.TENANT));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> requestData) {
        // requestData might contain "username", "email", "password", "role"
        // Example usage:
        String username = requestData.get("username");
        String email = requestData.get("email");
        String password = requestData.get("password");
        String role = requestData.get("role");

        // Pass these to your AuthService method
        authService.register(username, email, password, role);

        return ResponseEntity.ok("User registered successfully!");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        // credentials might have "username" and "password"
        String username = credentials.get("username");
        String password = credentials.get("password");

        // AuthService logic
        String token = authService.login(username, password);

        // Return the token (for example) in the response
        return ResponseEntity.ok("Bearer " + token);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("User logged out successfully!");
    }
}
