package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.*;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.RoleRepository;
import gr.hua.dit.rentalapp.services.UserAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final RoleRepository roleRepository;
    private final UserAuthService userAuthService;

    @Autowired
    public AuthController(RoleRepository roleRepository, UserAuthService userAuthService) {
        this.roleRepository = roleRepository;
        this.userAuthService = userAuthService;
    }

    // View endpoints for login
    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("/auth/login");
    }

    @GetMapping("/dashboard")
    public ModelAndView dashboard() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getAuthorities().stream().allMatch(a -> a.getAuthority().equals(" "))) {
            return new ModelAndView("redirect:/login");
        }
        return new ModelAndView("dashboard/dashboard");
    }

    @GetMapping("/")
    public ModelAndView root() {
        return new ModelAndView("redirect:/home");
    }

    @GetMapping("/home")
    public ModelAndView home() {
        return new ModelAndView("/home-page/home");
    }

    // View endpoints for registration
    @GetMapping("/register")
    public ModelAndView showRegistrationForm() {
        return new ModelAndView("/auth/register");
    }

    @GetMapping("/uploadIdentity")
    public ModelAndView showUploadIdentityForm() {
        return new ModelAndView("/auth/uploadIdentity");
    }

    @PostMapping("/register")
    public ModelAndView registerUser(@RequestParam String email,
                                     @RequestParam String username,
                                     @RequestParam String password,
                                     @RequestParam String firstName,
                                     @RequestParam String lastName,
                                     @RequestParam String role,
                                     @RequestParam(required = false) Double monthlyIncome,
                                     @RequestParam(required = false) String employmentStatus,
                                     @RequestParam(required = false) MultipartFile idFront,
                                     @RequestParam(required = false) MultipartFile idBack,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Check if username already exists
            if (userAuthService.findByUsername(username).isPresent()) {
                ModelAndView mav = new ModelAndView("/auth/register");
                mav.addObject("error", "Username already exists!");
                return mav;
            }

            // Register the user
            userAuthService.register(username, email, password, firstName, lastName, role, monthlyIncome, employmentStatus);

            // Handle ID uploads if provided
            if (idFront != null && !idFront.isEmpty() && idBack != null && !idBack.isEmpty()) {
                try {
                    String uploadDir = "uploads/identity/" + username + "/";
                    Path uploadPath = Paths.get(uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // Save front ID
                    Path frontPath = uploadPath.resolve("id_front" + getFileExtension(idFront.getOriginalFilename()));
                    Files.copy(idFront.getInputStream(), frontPath, StandardCopyOption.REPLACE_EXISTING);

                    // Save back ID
                    Path backPath = uploadPath.resolve("id_back" + getFileExtension(idBack.getOriginalFilename()));
                    Files.copy(idBack.getInputStream(), backPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    // Log the error but continue with registration
                    logger.error("Failed to save ID documents for user: " + username, e);
                }
            }

            // Redirect to login page with success message
            ModelAndView mav = new ModelAndView("redirect:/login");
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return mav;

        } catch (Exception e) {
            ModelAndView mav = new ModelAndView("/auth/register");
            mav.addObject("error", "Registration failed: " + e.getMessage());
            return mav;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        userAuthService.logout();
        return ResponseEntity.ok("User logged out successfully!");
    }

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> registerUserApi(@RequestBody Map<String, String> requestData) {
        try {
            String username = requestData.get("username");
            String email = requestData.get("email");
            String password = requestData.get("password");
            String firstName = requestData.get("firstName");
            String lastName = requestData.get("lastName");
            String role = requestData.get("role");
            Double monthlyIncome = requestData.get("monthlyIncome") != null ? Double.valueOf(requestData.get("monthlyIncome")) : null;
            String employmentStatus = requestData.get("employmentStatus");

            // Validate required fields
            if (username == null || email == null || password == null || role == null || firstName == null || lastName == null) {
                return ResponseEntity.badRequest().body("All fields are required");
            }

            // Register the user
            userAuthService.register(username, email, password, firstName, lastName, role, monthlyIncome, employmentStatus);
            return ResponseEntity.ok("User registered successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            // Validate required fields
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body("Username and password are required");
            }

            // AuthService logic
            Map<String, String> authResponse = userAuthService.login(username, password);

            // Create response with token and redirect URL
            Map<String, String> response = new HashMap<>();
            response.put("token", authResponse.get("token"));
            response.put("role", authResponse.get("role"));
            response.put("redirect", "/home");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<String> logoutApi() {
        // No need to call userAuthService.logout() since we're handling it client-side
        return ResponseEntity.ok("User logged out successfully!");
    }

    @PostConstruct
    public void setupDefaultRoles() {
        try {
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
        } catch (Exception e) {
            logger.error("Error setting up default roles", e);
        }
    }
}
