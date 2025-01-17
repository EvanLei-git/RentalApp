package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.services.PropertyService;
import gr.hua.dit.rentalapp.services.PropertyVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This controller handles all the requests related to properties.
 */
@Controller
@RequestMapping("/property")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyVisitService propertyVisitService;

    @Autowired
    public PropertyController(PropertyService propertyService, PropertyVisitService propertyVisitService) {
        this.propertyService = propertyService;
        this.propertyVisitService = propertyVisitService;
    }

    // GET single property
    @GetMapping("/{propertyId}")
    @ResponseBody
    public ResponseEntity<Property> getPropertyById(@PathVariable Long propertyId) {
        try {
            Property property = propertyService.getPropertyById(propertyId);
            if (property != null) {
                return ResponseEntity.ok(property);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // POST: create a new property (Landlord usage)
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<String> createProperty(@RequestBody Property property, Principal principal) {
        // Get the currently logged in user
        User user = (User) ((Authentication) principal).getPrincipal();
        
        // Check if user is null
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not authenticated");
        }
        
        // Check if user has LANDLORD role
        boolean isLandlord = user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.LANDLORD);
                
        if (!isLandlord) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only landlords can create properties");
        }
        
        // Check if user is instance of Landlord
        if (!(user instanceof Landlord)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("User has landlord role but is not a Landlord instance");
        }
        
        // Set the owner before saving
        property.setOwner((Landlord) user);
        propertyService.createProperty(property);
        return ResponseEntity.ok("Property created successfully!");
    }

    // PUT: update property details
    @PutMapping("/{propertyId}")
    public ResponseEntity<String> updateProperty(@PathVariable Long propertyId, @RequestBody Property updatedProperty) {
        try {
            propertyService.updateProperty(propertyId, updatedProperty);
            return ResponseEntity.ok("Property updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update property: " + e.getMessage());
        }
    }

    // DELETE: remove property
    @DeleteMapping("/{propertyId}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long propertyId) {
        try {
            propertyService.deleteProperty(propertyId);
            return ResponseEntity.ok("Property deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete property: " + e.getMessage());
        }
    }

    // GET property data for home page
    @GetMapping("/homeData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHomePageData() {
        try {
            List<Property> properties = propertyService.getAllProperties().stream()
                .filter(property -> !property.isRented() && property.isApproved()) // Only show approved and unrented properties
                .collect(Collectors.toList());

            List<Map<String, Object>> propertyDTOs = properties.stream()
                .map(property -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", property.getPropertyId());
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
                    dto.put("landlordUsername", property.getOwner() != null ? property.getOwner().getUsername() : null);
                    return dto;
                })
                .collect(Collectors.toList());

            Set<String> cities = properties.stream()
                    .map(Property::getCity)
                    .collect(Collectors.toSet());
            Set<String> countries = properties.stream()
                    .map(Property::getCountry)
                    .collect(Collectors.toSet());
            
            Map<String, Object> data = new HashMap<>();
            data.put("properties", propertyDTOs);
            data.put("cities", cities);
            data.put("countries", countries);
            data.put("minPrice", properties.stream()
                    .mapToDouble(Property::getRentAmount)
                    .min()
                    .orElse(0));
            data.put("maxPrice", properties.stream()
                    .mapToDouble(Property::getRentAmount)
                    .max()
                    .orElse(0));
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // GET filter properties
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> filterProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean hasParking,
            @RequestParam(required = false) Boolean allowsPets,
            @RequestParam(required = false) Boolean hasGarden,
            @RequestParam(required = false) Boolean hasBalcony) {
        
        try {
            List<Property> filteredProperties = propertyService.getAllProperties().stream()
                .filter(p -> !p.isRented() && p.isApproved()) // Only show approved and unrented properties
                .filter(p -> city == null || p.getCity().equals(city))
                .filter(p -> country == null || p.getCountry().equals(country))
                .filter(p -> minPrice == null || p.getRentAmount() >= minPrice)
                .filter(p -> maxPrice == null || p.getRentAmount() <= maxPrice)
                .filter(p -> hasParking == null || p.isHasParking() == hasParking)
                .filter(p -> allowsPets == null || p.isAllowsPets() == allowsPets)
                .filter(p -> hasGarden == null || p.isHasGarden() == hasGarden)
                .filter(p -> hasBalcony == null || p.isHasBalcony() == hasBalcony)
                .collect(Collectors.toList());

            List<Map<String, Object>> propertyDTOs = filteredProperties.stream()
                .map(property -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("id", property.getPropertyId());
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
                    dto.put("landlordUsername", property.getOwner() != null ? property.getOwner().getUsername() : null);
                    return dto;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(propertyDTOs);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // GET single property page
    @GetMapping("/{propertyId}/details")
    public ModelAndView showPropertyDetails(@PathVariable Long propertyId, Principal principal) {
        Property property = propertyService.getPropertyById(propertyId);
        List<PropertyVisit> visits = propertyVisitService.getVisitsByProperty(propertyId);
        
        ModelAndView mav = new ModelAndView("/property-details/property-details");
        mav.addObject("property", property);
        mav.addObject("visits", visits);
        
        // Add authentication information
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TENANT"))) {
            mav.addObject("isTenant", true);
        } else {
            mav.addObject("isTenant", false);
        }
        
        // Check if current user has already scheduled a visit
        if (principal != null) {
            boolean hasExistingVisit = propertyVisitService.hasExistingVisit(propertyId, principal.getName());
            mav.addObject("hasExistingVisit", hasExistingVisit);
        }
        
        return mav;
    }
    
    // API Endpoints for property management
    @GetMapping("/api/landlords/properties/{propertyId}")
    public ResponseEntity<Property> getPropertyByIdApi(@PathVariable Long propertyId) {
        Property property = propertyService.getPropertyById(propertyId);
        if (property != null) {
            return ResponseEntity.ok(property);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/api/landlords/properties/{propertyId}")
    public ResponseEntity<String> updatePropertyApi(@PathVariable Long propertyId, @RequestBody Property property) {
        try {
            propertyService.updateProperty(propertyId, property);
            return ResponseEntity.ok("Property updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update property: " + e.getMessage());
        }
    }

    @DeleteMapping("/api/landlords/properties/{propertyId}")
    public ResponseEntity<String> deletePropertyApi(@PathVariable Long propertyId) {
        try {
            propertyService.deleteProperty(propertyId);
            return ResponseEntity.ok("Property deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete property: " + e.getMessage());
        }
    }

    @PostMapping("/api/landlords/properties")
    public ResponseEntity<Property> createPropertyApi(@RequestBody Property property) {
        try {
            Property newProperty = propertyService.createProperty(property);
            return ResponseEntity.ok(newProperty);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
