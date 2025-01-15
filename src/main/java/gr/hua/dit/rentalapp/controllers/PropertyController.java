package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.services.PropertyService;
import gr.hua.dit.rentalapp.services.PropertyVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Collections;
import java.security.Principal;

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

    // GET all properties
    @GetMapping
    public List<Property> getAllProperties() {
        return propertyService.getAllProperties();
    }

    // GET single property
    @GetMapping("/{propertyId}")
    public Property getPropertyById(@PathVariable Long propertyId) {
        return propertyService.getPropertyById(propertyId);
    }

    // POST: create a new property (Landlord usage)
    @PostMapping
    public ResponseEntity<String> createProperty(@RequestBody Property property) {
        propertyService.createProperty(property);
        return ResponseEntity.ok("Property created successfully!");
    }

    // PUT: update property details
    @PutMapping("/{propertyId}")
    public ResponseEntity<String> updateProperty(@PathVariable Long propertyId, @RequestBody Property updatedProperty) {
        propertyService.updateProperty(propertyId, updatedProperty);
        return ResponseEntity.ok("Property updated successfully!");
    }

    // DELETE: remove property
    @DeleteMapping("/{propertyId}")
    public ResponseEntity<String> deleteProperty(@PathVariable Long propertyId) {
        propertyService.deleteProperty(propertyId);
        return ResponseEntity.ok("Property deleted successfully!");
    }

    // GET property data for home page
    @GetMapping("/homeData")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getHomePageData() {
        try {
            List<Property> properties = propertyService.getAllProperties();
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
                    dto.put("landlordUsername", property.getOwner() != null ? property.getOwner().getUsername() : null);
                    return dto;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(propertyDTOs);
        } catch (Exception e) {
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
}
