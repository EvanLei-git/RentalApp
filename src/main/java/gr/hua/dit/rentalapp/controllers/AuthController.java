package gr.hua.dit.rentalapp.controllers;

import jakarta.annotation.PostConstruct;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        if (roleRepository.findByName(RoleType.ADMINISTRATOR).isEmpty()) {
            roleRepository.save(new Role(RoleType.ADMINISTRATOR));
        }
        // Landlord
        if (roleRepository.findByName(RoleType.LANDLORD).isEmpty()) {
            roleRepository.save(new Role(RoleType.LANDLORD));
        }
        // Tenant
        if (roleRepository.findByName(RoleType.TENANT).isEmpty()) {
            roleRepository.save(new Role(RoleType.TENANT));
        }
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> requestData) {
        try {
            String username = requestData.get("username");
            String email = requestData.get("email");
            String password = requestData.get("password");
            String role = requestData.get("role");

            // Validate required fields
            if (username == null || email == null || password == null || role == null) {
                return ResponseEntity.badRequest().body("All fields are required");
            }

            // Register the user
            authService.register(username, email, password, role);
            return ResponseEntity.ok("User registered successfully!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            // Validate required fields
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body("Username and password are required");
            }

            // AuthService logic
            Map<String, String> authResponse = authService.login(username, password);

            // Create response with token and redirect URL
            Map<String, String> response = new HashMap<>();
            response.put("token", authResponse.get("token"));
            response.put("role", authResponse.get("role"));
            response.put("redirect", "/home.html");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("User logged out successfully!");
    }
}
