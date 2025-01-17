package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import gr.hua.dit.rentalapp.services.RentalApplicationService;
import gr.hua.dit.rentalapp.services.PropertyVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@RestController
@RequestMapping("/api/rental-applications")
public class RentalApplicationController {

    private final RentalApplicationService applicationService;
    private final PropertyVisitService visitService;
    private final RentalApplicationRepository rentalApplicationRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyVisitRepository visitRepository;

    @Autowired
    public RentalApplicationController(RentalApplicationService applicationService, 
                                       PropertyVisitService visitService, 
                                       RentalApplicationRepository rentalApplicationRepository, 
                                       TenantRepository tenantRepository, 
                                       PropertyRepository propertyRepository,
                                       PropertyVisitRepository visitRepository) {
        this.applicationService = applicationService;
        this.visitService = visitService;
        this.rentalApplicationRepository = rentalApplicationRepository;
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
        this.visitRepository = visitRepository;
    }

    // GET all applications (probably restricted to Admin or Landlord)
    @GetMapping
    public List<RentalApplication> getAllApplications() {
        return applicationService.getAllApplications();
    }

    // GET a specific application
    @GetMapping("/{id}")
    public RentalApplication getApplicationById(@PathVariable Long id) {
        return applicationService.getApplicationById(id);
    }

    // POST: tenant creates a new application
    @PostMapping
    public ResponseEntity<String> createApplication(@RequestBody RentalApplication application) {
        applicationService.createApplication(application);
        return ResponseEntity.ok("Rental application submitted successfully!");
    }

    // PUT: update application status (approve / reject)
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateApplicationStatus(@PathVariable Long id, 
                                                          @RequestBody ApplicationStatus status) {
        applicationService.updateApplicationStatus(id, status);
        return ResponseEntity.ok("Application status updated to " + status);
    }

    // DELETE: maybe allow tenant to cancel an application if still pending
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.ok("Application deleted.");
    }

    @GetMapping("/tenant/dashboard")
    public ResponseEntity<?> getTenantDashboardData(
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) List<ApplicationStatus> applicationStatuses,
            @RequestParam(required = false) List<VisitStatus> visitStatuses,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<Map<String, Object>> combinedResults = new ArrayList<>();

        // Get tenant's applications
        if (filterType == null || filterType.equals("all") || filterType.equals("applications")) {
            List<RentalApplication> applications = applicationService.getApplicationsByTenant(username);

            // Filter by status if specified
            if (applicationStatuses != null && !applicationStatuses.isEmpty()) {
                applications = applications.stream()
                        .filter(app -> applicationStatuses.contains(app.getStatus()))
                        .collect(Collectors.toList());
            }

            // Filter by date range if specified
            if (dateFrom != null && dateTo != null) {
                applications = applications.stream()
                        .filter(app -> {
                            Date appDate = app.getApplicationDate();
                            return appDate != null && 
                                   !appDate.before(dateFrom) && 
                                   !appDate.after(dateTo);
                        })
                        .collect(Collectors.toList());
            }

            // Convert applications to simple maps
            for (RentalApplication app : applications) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "APPLICATION");
                item.put("status", app.getStatus().toString());
                item.put("date", app.getApplicationDate());

                // Create a simplified property map
                Map<String, Object> propertyMap = new HashMap<>();
                propertyMap.put("address", app.getProperty().getAddress());
                propertyMap.put("bedrooms", app.getProperty().getBedrooms());
                propertyMap.put("bathrooms", app.getProperty().getBathrooms());
                propertyMap.put("rentAmount", app.getProperty().getRentAmount());
                
                item.put("property", propertyMap);
                combinedResults.add(item);
            }
        }

        // Get tenant's visits
        if (filterType == null || filterType.equals("all") || filterType.equals("visits")) {
            List<PropertyVisit> visits = visitService.getVisitsByTenant(username);

            // Filter by status if specified
            if (visitStatuses != null && !visitStatuses.isEmpty()) {
                visits = visits.stream()
                        .filter(visit -> visitStatuses.contains(visit.getVisitStatus()))
                        .collect(Collectors.toList());
            }

            // Filter by date range if specified
            if (dateFrom != null && dateTo != null) {
                visits = visits.stream()
                        .filter(visit -> {
                            Date visitDate = visit.getVisitDate();
                            return visitDate != null && 
                                   !visitDate.before(dateFrom) && 
                                   !visitDate.after(dateTo);
                        })
                        .collect(Collectors.toList());
            }

            // Convert visits to simple maps
            for (PropertyVisit visit : visits) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "VISIT");
                item.put("status", visit.getVisitStatus().toString());
                item.put("date", visit.getVisitDate());

                // Create a simplified property map
                Map<String, Object> propertyMap = new HashMap<>();
                propertyMap.put("address", visit.getProperty().getAddress());
                propertyMap.put("bedrooms", visit.getProperty().getBedrooms());
                propertyMap.put("bathrooms", visit.getProperty().getBathrooms());
                propertyMap.put("rentAmount", visit.getProperty().getRentAmount());
                
                item.put("property", propertyMap);
                combinedResults.add(item);
            }
        }

        // Sort all results by date (most recent first)
        combinedResults.sort((a, b) -> {
            Date dateA = (Date) a.get("date");
            Date dateB = (Date) b.get("date");
            return dateB.compareTo(dateA);
        });

        return ResponseEntity.ok(combinedResults);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submitApplication(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String username = authentication.getName();
            Long propertyId = Long.parseLong(request.get("propertyId").toString());

            Optional<Tenant> tenantOpt = tenantRepository.findByUsername(username);
            Optional<Property> propertyOpt = propertyRepository.findById(propertyId);

            if (tenantOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tenant not found"));
            }

            if (propertyOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Property not found"));
            }

            Tenant tenant = tenantOpt.get();
            Property property = propertyOpt.get();

            // Check if application already exists
            List<RentalApplication> existingApplications = rentalApplicationRepository.findByPropertyPropertyId(propertyId);
            boolean hasExistingApplication = existingApplications.stream()
                .filter(app -> app.getApplicant().getUserId().equals(tenant.getUserId()))
                .anyMatch(app -> app.getStatus() == ApplicationStatus.PENDING);

            if (hasExistingApplication) {
                return ResponseEntity.badRequest().body(Map.of("message", "You already have a pending application for this property"));
            }

            // Create new application
            RentalApplication application = new RentalApplication();
            application.setApplicant(tenant);
            application.setProperty(property);
            application.setStatus(ApplicationStatus.PENDING);
            application.setApplicationDate(new Date());

            // Save application
            rentalApplicationRepository.save(application);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Application submitted successfully");
            response.put("applicationId", application.getApplicationId());
            response.put("status", "PENDING");
            response.put("isVerified", tenant.isVerified());
            response.put("hasEmploymentDetails", tenant.getEmploymentStatus() != null && !tenant.getEmploymentStatus().isEmpty());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Failed to submit application: " + e.getMessage()));
        }
    }
}
