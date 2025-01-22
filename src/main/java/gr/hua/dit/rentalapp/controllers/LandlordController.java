package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.services.LandlordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/landlords")
public class LandlordController {

    private static final Logger logger = LoggerFactory.getLogger(LandlordController.class);
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
    public ResponseEntity<?> getDashboardData(@RequestBody Map<String, Object> filters) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            logger.debug("Processing dashboard request for user: {}", username);
            
            Map<String, Object> response = new HashMap<>();
            
            String viewType = (String) filters.get("viewType");
            logger.debug("View type: {}", viewType);
            
            Landlord landlord = landlordService.getLandlordByUsername(username);
            if (landlord == null) {
                logger.error("Landlord not found for username: {}", username);
                return ResponseEntity.badRequest().body("Landlord not found");
            }

            if ("properties".equals(viewType)) {
                // Get landlord's properties
                List<Property> properties = landlordService.getPropertiesByLandlord(landlord.getUserId());
                logger.debug("Found {} properties", properties != null ? properties.size() : 0);
                
                // Create a list to hold aggregated property data
                List<Map<String, Object>> aggregatedProperties = new ArrayList<>();
                
                if (properties != null) {
                    for (Property property : properties) {
                        Map<String, Object> propertyData = new HashMap<>();
                        propertyData.put("id", property.getPropertyId());
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
                        propertyData.put("status", property.isRented() ? "RENTED" : "UNRENTED");
                        
                        aggregatedProperties.add(propertyData);
                    }
                }
                response.put("properties", aggregatedProperties);
            } else if ("alerts".equals(viewType)) {
                // Handle alerts view
                List<Map<String, Object>> alerts = new ArrayList<>();
                
                // Get applications for landlord's properties
                List<RentalApplication> applications = landlordService.getRentalApplicationsForLandlord(landlord.getUserId());
                logger.debug("Found {} applications", applications != null ? applications.size() : 0);
                
                if (applications != null) {
                    for (RentalApplication app : applications) {
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("id", app.getApplicationId());
                        alert.put("type", "application");
                        alert.put("propertyAddress", app.getProperty().getAddress());
                        alert.put("message", " New rental application from " + app.getApplicant().getFirstName() + " " + app.getApplicant().getLastName());
                        alert.put("status", app.getStatus().toString());
                        alert.put("timestamp", app.getApplicationDate());
                        alert.put("tenantName", app.getApplicant().getFirstName() + " " + app.getApplicant().getLastName());
                        alert.put("isVerified", app.getApplicant().isVerified());
                        alert.put("monthlyIncome", app.getApplicant().getMonthlyIncome());
                        alerts.add(alert);
                    }
                }
                
                // Get visits for landlord's properties
                List<PropertyVisit> visits = landlordService.getPropertyVisitsForLandlord(landlord.getUserId());
                logger.debug("Found {} visits", visits != null ? visits.size() : 0);
                
                if (visits != null) {
                    for (PropertyVisit visit : visits) {
                        Map<String, Object> alert = new HashMap<>();
                        alert.put("id", visit.getVisitId());
                        alert.put("type", "visit");
                        alert.put("propertyAddress", visit.getProperty().getAddress());
                        alert.put("message", " Visit request for " +
                                new SimpleDateFormat("dd-MM-yyyy HH:mm").format(visit.getVisitDate()));
                        alert.put("status", visit.getVisitStatus().toString());
                        alert.put("timestamp", visit.getStatusCreatedAt());
                        alert.put("tenantName", visit.getTenant().getFirstName() + " " + visit.getTenant().getLastName());
                        alert.put("isVerified", visit.getTenant().isVerified());
                        alerts.add(alert);
                    }
                }
                
                // Apply filters
                @SuppressWarnings("unchecked")
                List<String> applicationStatus = (List<String>) filters.get("applicationStatus");
                @SuppressWarnings("unchecked")
                List<String> visitStatus = (List<String>) filters.get("visitStatus");
                // Filter alerts based on criteria
                if (applicationStatus != null || visitStatus != null) {
                    alerts = alerts.stream()
                        .filter(alert -> {
                            boolean matchesStatus = true;
                            if ("application".equals(alert.get("type")) && applicationStatus != null) {
                                matchesStatus = applicationStatus.contains(alert.get("status"));
                            } else if ("visit".equals(alert.get("type")) && visitStatus != null) {
                                matchesStatus = visitStatus.contains(alert.get("status"));
                            }
                            
                            boolean matchesDate = true;
                            
                            return matchesStatus && matchesDate;
                        })
                        .collect(Collectors.toList());
                }
                
                response.put("alerts", alerts);
            } else {
                logger.error("Invalid view type: {}", viewType);
                return ResponseEntity.badRequest().body("Invalid view type: " + viewType);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing dashboard request", e);
            return ResponseEntity.internalServerError().body("Error processing dashboard request: " + e.getMessage());
        }
    }
}
