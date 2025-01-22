package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.services.AdministratorService;
import gr.hua.dit.rentalapp.services.PropertyService;
import gr.hua.dit.rentalapp.services.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/administrators")
@Secured("ROLE_ADMINISTRATOR")
public class AdministratorController {

    private final AdministratorService adminService;
    private final UserAuthService userAuthService;
    private final PropertyService propertyService;

    @Autowired
    public AdministratorController(AdministratorService adminService, UserAuthService userAuthService, PropertyService propertyService) {
        this.adminService = adminService;
        this.userAuthService = userAuthService;
        this.propertyService = propertyService;
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

    // GET all users with optional filters
    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean verified) {
        try {
            // If no filters are provided, return all users
            if (username == null && role == null && verified == null) {
                return ResponseEntity.ok(userAuthService.getAllUsersInfo());
            }
            
            // Otherwise, return filtered users
            return ResponseEntity.ok(userAuthService.getFilteredUsers(username, role, verified));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching users: " + e.getMessage());
        }
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
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userAuthService.deleteUser(userId);
            return ResponseEntity.ok()
                .body(Map.of("success", true, "message", "User deleted successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // POST: verify a user
    @PostMapping("/users/{userId}/verify")
    public ResponseEntity<?> verifyUser(@PathVariable Long userId) {
        try {
            userAuthService.verifyUser(userId);
            return ResponseEntity.ok("User verified successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying user: " + e.getMessage());
        }
    }

    // GET all properties for admin dashboard with filters
    @GetMapping("/properties")
    public ResponseEntity<List<Map<String, Object>>> getPropertiesForAdmin(
            @RequestParam(required = false) String ownerUsername,
            @RequestParam(required = false) Boolean isRented) {
        try {
            List<Property> properties = propertyService.getAllProperties();
            
            // Filter properties based on parameters
            if (ownerUsername != null && !ownerUsername.isEmpty() || isRented != null) {
                properties = properties.stream()
                    .filter(property -> {
                        boolean matchesOwner = true;
                        boolean matchesRentedStatus = true;
                        
                        // Filter by owner username if provided
                        if (ownerUsername != null && !ownerUsername.isEmpty()) {
                            matchesOwner = property.getOwner().getUsername()
                                .toLowerCase()
                                .contains(ownerUsername.toLowerCase());
                        }
                        
                        // Filter by rented status if provided
                        if (isRented != null) {
                            matchesRentedStatus = property.isRented() == isRented;
                        }
                        
                        return matchesOwner && matchesRentedStatus;
                    })
                    .collect(Collectors.toList());
            }
            
            List<Map<String, Object>> propertyDTOs = properties.stream()
                .map(property -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("propertyId", property.getPropertyId());
                    dto.put("address", property.getAddress());
                    dto.put("city", property.getCity());
                    dto.put("country", property.getCountry());
                    dto.put("rentAmount", property.getRentAmount());
                    dto.put("sizeInSquareMeters", property.getSizeInSquareMeters());
                    dto.put("bedrooms", property.getBedrooms());
                    dto.put("bathrooms", property.getBathrooms());
                    dto.put("hasParking", property.isHasParking());
                    dto.put("allowsPets", property.isAllowsPets());
                    dto.put("hasGarden", property.isHasGarden());
                    dto.put("hasBalcony", property.isHasBalcony());
                    dto.put("type", property.getType());
                    dto.put("description", property.getDescription());
                    dto.put("creationDate", property.getCreationDate());
                    dto.put("rented", property.isRented());
                    dto.put("approved", property.isApproved());
                    
                    // Add owner information
                    if (property.getOwner() != null) {
                        Map<String, Object> ownerInfo = new HashMap<>();
                        ownerInfo.put("id", property.getOwner().getUserId());
                        ownerInfo.put("username", property.getOwner().getUsername());
                        ownerInfo.put("firstName", property.getOwner().getFirstName());
                        ownerInfo.put("lastName", property.getOwner().getLastName());
                        dto.put("owner", ownerInfo);
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(propertyDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // POST approve property
    @PostMapping("/properties/{propertyId}/approve")
    public ResponseEntity<String> approveProperty(@PathVariable Long propertyId) {
        try {
            Property property = propertyService.getPropertyById(propertyId);
            if (property == null) {
                return ResponseEntity.notFound().build();
            }
            
            property.setApproved(true);
            propertyService.updateProperty(propertyId, property);
            return ResponseEntity.ok("Property approved successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to approve property");
        }
    }

    // DELETE property
    @DeleteMapping("/properties/{propertyId}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long propertyId) {
        try {
            Property property = propertyService.getPropertyById(propertyId);
            if (property == null) {
                return ResponseEntity.notFound().build();
            }
            
            propertyService.deleteProperty(propertyId);
            return ResponseEntity.ok("Property deleted successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete property");
        }
    }

    // PUT: verify a tenant
    @PutMapping("/{adminId}/verify-tenant/{tenantId}")
    public ResponseEntity<String> verifyTenant(@PathVariable Long adminId, @PathVariable Long tenantId) {
        adminService.verifyTenant(adminId, tenantId);
        return ResponseEntity.ok("Tenant verified successfully!");
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
