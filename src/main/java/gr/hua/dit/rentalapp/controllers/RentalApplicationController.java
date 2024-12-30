package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import gr.hua.dit.rentalapp.services.RentalApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class RentalApplicationController {

    private final RentalApplicationService applicationService;

    @Autowired
    public RentalApplicationController(RentalApplicationService applicationService) {
        this.applicationService = applicationService;
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
}
