package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.services.TenantService;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import gr.hua.dit.rentalapp.enums.VisitStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:8080", allowCredentials = "true")
@RequestMapping("/api/tenant")
public class TenantController {

    private final TenantService tenantService;
    private final RentalApplicationRepository rentalApplicationRepository;
    private final PropertyVisitRepository propertyVisitRepository;
    private final TenantRepository tenantRepository;

    @Autowired
    public TenantController(TenantService tenantService,
                            RentalApplicationRepository rentalApplicationRepository,
                            PropertyVisitRepository propertyVisitRepository,
                            TenantRepository tenantRepository) {
        this.tenantService = tenantService;
        this.rentalApplicationRepository = rentalApplicationRepository;
        this.propertyVisitRepository = propertyVisitRepository;
        this.tenantRepository = tenantRepository;
    }

    @GetMapping("/tenants")
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @GetMapping("/tenants/{id}")
    public Tenant getTenantById(@PathVariable Long id) {
        return tenantService.getTenantById(id);
    }

    @PutMapping("/tenants/{id}")
    public ResponseEntity<?> updateTenant(@PathVariable Long id, @RequestBody Tenant tenant) {
        try {
            tenantService.updateTenant(id, tenant);
            return ResponseEntity.ok("Tenant updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(
            @RequestParam(required = false) String applicationStatuses,
            @RequestParam(required = false) String visitStatuses) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<Tenant> tenantOpt = tenantRepository.findByUsername(username);

            if (tenantOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Tenant not found");
            }

            Tenant tenant = tenantOpt.get();

            List<String> applicationStatusList = applicationStatuses != null && !applicationStatuses.isEmpty()
                    ? Arrays.asList(applicationStatuses.split(","))
                    : Collections.emptyList();

            List<String> visitStatusList = visitStatuses != null && !visitStatuses.isEmpty()
                    ? Arrays.asList(visitStatuses.split(","))
                    : Collections.emptyList();

            List<RentalApplication> applications = rentalApplicationRepository.findByApplicantUserId(tenant.getUserId());
            List<PropertyVisit> visits = propertyVisitRepository.findByTenant_Username(username);

            // Filter applications
            applications = filterApplications(applications, applicationStatusList);
            // Filter visits
            visits = filterVisits(visits, visitStatusList);

            Map<String, Object> response = new HashMap<>();
            response.put("applications", applications);
            response.put("visits", visits);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard data: " + e.getMessage());
        }
    }

    private List<RentalApplication> filterApplications(List<RentalApplication> applications,
                                                       List<String> statuses) {
        return applications.stream()
                .filter(app -> statuses == null || statuses.isEmpty() ||
                        statuses.contains(app.getStatus().toString()))
                .collect(Collectors.toList());
    }

    private List<PropertyVisit> filterVisits(List<PropertyVisit> visits,
                                             List<String> statuses) {
        return visits.stream()
                .filter(visit -> statuses == null || statuses.isEmpty() ||
                        statuses.contains(visit.getVisitStatus().toString()))
                .collect(Collectors.toList());
    }

    @GetMapping("/applications")
    public ResponseEntity<?> getApplications(
            Authentication authentication,
            @RequestParam(required = false) String applicationStatuses) {
        try {
            String username = authentication.getName();
            Optional<Tenant> tenantOpt = tenantRepository.findByUsername(username);

            if (tenantOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tenant not found"));
            }

            Tenant tenant = tenantOpt.get();
            List<RentalApplication> applications = rentalApplicationRepository.findByApplicantUserId(tenant.getUserId());
            applications = filterApplications(applications,
                    applicationStatuses != null ? Arrays.asList(applicationStatuses.split(",")) : null);
            applications.sort((a1, a2) -> a2.getApplicationDate().compareTo(a1.getApplicationDate()));

            return ResponseEntity.ok(Map.of("applications", applications));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/visits")
    public ResponseEntity<?> getVisits(
            Authentication authentication,
            @RequestParam(required = false) String visitStatuses) {
        try {
            String username = authentication.getName();
            Optional<Tenant> tenantOpt = tenantRepository.findByUsername(username);

            if (tenantOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Tenant not found"));
            }

            Tenant tenant = tenantOpt.get();
            List<PropertyVisit> visits = propertyVisitRepository.findByTenant_Username(username);
            visits = filterVisits(visits,
                    visitStatuses != null ? Arrays.asList(visitStatuses.split(",")) : null);
            visits.sort((v1, v2) -> v2.getVisitDate().compareTo(v1.getVisitDate()));

            return ResponseEntity.ok(Map.of("visits", visits));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
