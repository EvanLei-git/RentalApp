package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.services.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;

import gr.hua.dit.rentalapp.repositories.TenantRepository;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.entities.PropertyVisit;

@Controller
public class UserController {

    private final UserAuthService userService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TenantRepository tenantRepository;
    private final RentalApplicationRepository rentalApplicationRepository;
    private final PropertyVisitRepository propertyVisitRepository;

    @Autowired
    public UserController(UserAuthService userService,
                          BCryptPasswordEncoder passwordEncoder,
                          TenantRepository tenantRepository,
                          RentalApplicationRepository rentalApplicationRepository,
                          PropertyVisitRepository propertyVisitRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.tenantRepository = tenantRepository;
        this.rentalApplicationRepository = rentalApplicationRepository;
        this.propertyVisitRepository = propertyVisitRepository;
    }

    // Web Interface Endpoints

    @GetMapping("/profile/edit")
    @Transactional
    public ModelAndView showEditProfileForm(Authentication authentication) {
        if (authentication == null) {
            return new ModelAndView("redirect:/login");
        }

        // Get the current authentication from SecurityContext as it might have been updated
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        String username;
        if (currentAuth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) currentAuth.getPrincipal()).getUsername();
        } else {
            username = currentAuth.getName();
        }

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        ModelAndView modelAndView = new ModelAndView("profile/edit");
        modelAndView.addObject("user", user);
        modelAndView.addObject("isTenant", user instanceof Tenant);
        if (user instanceof Tenant) {
            modelAndView.addObject("tenant", user);
        }

        return modelAndView;
    }

    @PostMapping("/profile/update")
    @Transactional
    public ModelAndView updateProfile(
            @ModelAttribute User updatedUser,
            @RequestParam(value = "monthlyIncome", required = false) Double monthlyIncome,
            @RequestParam(value = "employmentStatus", required = false) String employmentStatus,
            @RequestParam(value = "password", required = false) String password,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null) {
            return new ModelAndView("redirect:/login");
        }

        // Get the current authentication from SecurityContext
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername;
        if (currentAuth.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            currentUsername = ((org.springframework.security.core.userdetails.UserDetails) currentAuth.getPrincipal()).getUsername();
        } else {
            currentUsername = currentAuth.getName();
        }

        User currentUser = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));

        // Check if username is being changed and if it's available
        if (!currentUsername.equals(updatedUser.getUsername())) {
            if (userService.findByUsername(updatedUser.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username already taken!");
                return new ModelAndView("redirect:/profile/edit");
            }
        }

        // Update basic user information
        currentUser.setFirstName(updatedUser.getFirstName());
        currentUser.setLastName(updatedUser.getLastName());
        currentUser.setEmail(updatedUser.getEmail());
        currentUser.setUsername(updatedUser.getUsername());

        if (currentUser instanceof Tenant tenant) {
            // Handle tenant-specific updates
            if (monthlyIncome != null) {
                tenant.setMonthlyIncome(monthlyIncome);
            }
            if (employmentStatus != null && !employmentStatus.isEmpty()) {
                tenant.setEmploymentStatus(employmentStatus);
            }
        }

        // Update password if provided
        if (password != null && !password.trim().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(password);
            currentUser.setPassword(encodedPassword);
        }

        // Save the updated user
        currentUser = userService.getUserRepository().save(currentUser);

        // Update the authentication with the new username
        org.springframework.security.core.userdetails.UserDetails updatedUserDetails = userService.loadUserByUsername(currentUser.getUsername());
        Authentication newAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                updatedUserDetails,
                null, // Don't expose credentials in the Authentication object
                updatedUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");

        // Redirect to the profile edit page with the new username
        return new ModelAndView("redirect:/profile/edit");
    }

    @GetMapping("/verification-status")
    @ResponseBody
    public Map<String, Object> getVerificationStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> userOpt = userService.findByUsername(username);

        Map<String, Object> response = new HashMap<>();

        if (userOpt.isPresent() && userOpt.get() instanceof Tenant) {
            Tenant tenant = (Tenant) userOpt.get();

            response.put("isVerified", tenant.isVerified());
            response.put("hasEmploymentDetails", tenant.getEmploymentStatus() != null && !tenant.getEmploymentStatus().isEmpty());
            response.put("isPendingVerification", tenant.isPendingVerification());
        } else {
            response.put("isVerified", false);
            response.put("hasEmploymentDetails", false);
            response.put("isPendingVerification", false);
        }

        return response;
    }

    // REST API Endpoints

    @GetMapping("/api/users")
    @ResponseBody
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/api/users/{username}")
    @ResponseBody
    public Optional<User> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @PutMapping("/api/users/{userId}")
    @ResponseBody
    public ResponseEntity<String> updateUser(@PathVariable Long userId, @RequestBody User updatedUser) {
        userService.updateUser(userId, updatedUser);
        return ResponseEntity.ok("User updated successfully!");
    }

    @DeleteMapping("/api/users/{userId}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully!");
    }
}