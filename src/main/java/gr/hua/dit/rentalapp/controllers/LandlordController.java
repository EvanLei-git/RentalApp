package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.services.LandlordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/landlords")
public class LandlordController {

    private final LandlordService landlordService;

    @Autowired
    public LandlordController(LandlordService landlordService) {
        this.landlordService = landlordService;
    }

    // GET all landlords
    @GetMapping
    public List<Landlord> getAllLandlords() {
        return landlordService.getAllLandlords();
    }

    // GET landlord by ID
    @GetMapping("/{id}")
    public Landlord getLandlordById(@PathVariable Long id) {
        return landlordService.getLandlordById(id);
    }

    // PUT: update landlord profile
    @PutMapping("/{id}")
    public void updateLandlord(@PathVariable Long id, @RequestBody Landlord updatedLandlord) {
        landlordService.updateLandlord(id, updatedLandlord);
    }

    // GET: properties belonging to a landlord
    @GetMapping("/{id}/properties")
    public List<Property> getLandlordProperties(@PathVariable Long id) {
        return landlordService.getPropertiesByLandlord(id);
    }

    // POST: Get dashboard data
    @PostMapping("/dashboard")
    public Map<String, Object> getDashboardData(@RequestBody Map<String, Object> filters) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        Map<String, Object> response = new HashMap<>();
        
        String viewType = (String) filters.get("viewType");
        if ("propertiesView".equals(viewType)) {
            // Get landlord's properties
            Landlord landlord = landlordService.getLandlordByUsername(username);
            if (landlord != null) {
                List<Property> properties = landlordService.getPropertiesByLandlord(landlord.getUserId());
                // Create a list to hold aggregated property data
                List<Map<String, Object>> aggregatedProperties = new ArrayList<>();
                
                for (Property property : properties) {
                    Map<String, Object> propertyData = new HashMap<>();
                    propertyData.put("propertyId", property.getPropertyId());
                    propertyData.put("address", property.getAddress());
                    propertyData.put("bedrooms", property.getBedrooms());
                    propertyData.put("bathrooms", property.getBathrooms());
                    propertyData.put("rentAmount", property.getRentAmount());
                    propertyData.put("type", property.getType());
                    propertyData.put("city", property.getCity());
                    propertyData.put("country", property.getCountry());
                    propertyData.put("description", property.getDescription());
                    propertyData.put("sizeInSquareMeters", property.getSizeInSquareMeters());
                    propertyData.put("hasParking", property.isHasParking());
                    propertyData.put("allowsPets", property.isAllowsPets());
                    propertyData.put("hasGarden", property.isHasGarden());
                    propertyData.put("hasBalcony", property.isHasBalcony());
                    propertyData.put("isRented", property.isRented());
                    
                    aggregatedProperties.add(propertyData);
                }
                response.put("properties", aggregatedProperties);
            }
        } else {
            // Handle alerts view logic here
        }
        
        return response;
    }

    // todo: landlord manage - properties or respond to rental applications....
}
