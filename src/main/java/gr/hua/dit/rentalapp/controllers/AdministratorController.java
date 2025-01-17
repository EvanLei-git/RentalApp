package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.services.AdministratorService;
import gr.hua.dit.rentalapp.services.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/administrators")
public class AdministratorController {

    private final AdministratorService adminService;
    private final UserAuthService userAuthService;

    @Autowired
    public AdministratorController(AdministratorService adminService, UserAuthService userAuthService) {
        this.adminService = adminService;
        this.userAuthService = userAuthService;
    }

    // GET all administrators
    @GetMapping
    public List<Administrator> getAllAdministrators() {
        return adminService.getAllAdministrators();
    }

    // GET admin by ID
    @GetMapping("/{id}")
    public Administrator getAdministratorById(@PathVariable Long id) {
        return adminService.getAdministratorById(id);
    }

    // GET all users
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return userAuthService.getAllUsersInfo();
    }

    // GET user details
    @GetMapping("/users/{userId}/details")
    public ResponseEntity<?> getUserDetails(@PathVariable Long userId) {
        try {
            Map<String, Object> userDetails = userAuthService.getUserDetailsById(userId);
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching user details: " + e.getMessage());
        }
    }

    // DELETE: delete a user
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userAuthService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully!");
    }

    // PUT: verify a tenant
    @PutMapping("/{adminId}/verify-tenant/{tenantId}")
    public ResponseEntity<String> verifyTenant(@PathVariable Long adminId, @PathVariable Long tenantId) {
        adminService.verifyTenant(adminId, tenantId);
        return ResponseEntity.ok("Tenant verified successfully!");
    }

    // PUT: approve or reject a property
    @PutMapping("/{adminId}/approve-property/{propertyId}")
    public ResponseEntity<String> approveProperty(@PathVariable Long adminId, @PathVariable Long propertyId) {
        adminService.approvePropertyListing(adminId, propertyId);
        return ResponseEntity.ok("Property approved successfully!");
    }

    // GET all rental applications
    @GetMapping("/rentals")
    public ResponseEntity<?> getAllRentals() {
        try {
            List<Map<String, Object>> rentals = adminService.getAllRentals();
            return ResponseEntity.ok(rentals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching rentals: " + e.getMessage());
        }
    }

    @GetMapping("/rentals/{rentalId}")
    public ResponseEntity<?> getRentalDetails(@PathVariable Long rentalId) {
        try {
            RentalApplication rental = adminService.getRentalApplicationById(rentalId);
            return ResponseEntity.ok(rental);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching rental details: " + e.getMessage());
        }
    }

    @PostMapping("/{adminId}/rentals/{rentalId}/approve")
    public ResponseEntity<?> approveRental(@PathVariable Long adminId, @PathVariable Long rentalId) {
        try {
            adminService.approveRentalApplication(adminId, rentalId);
            return ResponseEntity.ok("Rental application approved successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error approving rental: " + e.getMessage());
        }
    }

    @PostMapping("/{adminId}/rentals/{rentalId}/reject")
    public ResponseEntity<?> rejectRental(@PathVariable Long adminId, @PathVariable Long rentalId) {
        try {
            adminService.rejectRentalApplication(adminId, rentalId);
            return ResponseEntity.ok("Rental application rejected successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error rejecting rental: " + e.getMessage());
        }
    }
}
