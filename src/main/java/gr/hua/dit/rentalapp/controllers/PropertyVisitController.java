package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.services.PropertyVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class PropertyVisitController {

    private final PropertyVisitService visitService;

    @Autowired
    public PropertyVisitController(PropertyVisitService visitService) {
        this.visitService = visitService;
    }

    // GET: all visits (may restrict by landlord, tenant, etc.)
    @GetMapping
    public List<PropertyVisit> getAllVisits() {
        return visitService.getAllVisits();
    }

    // GET: a specific visit
    @GetMapping("/{id}")
    public PropertyVisit getVisitById(@PathVariable Long id) {
        return visitService.getVisitById(id);
    }

    // POST: request a visit (Tenant action)
    @PostMapping
    public ResponseEntity<PropertyVisit> requestVisit(@RequestParam Long tenantId, @RequestParam Long propertyId) {
        PropertyVisit visit = visitService.createVisit(tenantId, propertyId);
        return ResponseEntity.ok(visit);
    }

    // POST: schedule a visit
    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleVisit(@RequestBody VisitRequest request) {
        try {
            // Check if visit exists
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            boolean hasVisit = visitService.hasExistingVisit(request.getPropertyId(), username);
            if (hasVisit) {
                // Update existing visit
                PropertyVisit updatedVisit = visitService.updateVisit(request.getPropertyId(), request.getVisitDate());
                return ResponseEntity.ok(updatedVisit);
            } else {
                // Create new visit
                PropertyVisit visit = visitService.createAndScheduleVisit(request.getPropertyId(), request.getVisitDate());
                return ResponseEntity.ok(visit);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to schedule visit: " + e.getMessage());
        }
    }

    static class VisitRequest {
        private Long propertyId;
        private String visitDate;

        public Long getPropertyId() {
            return propertyId;
        }

        public void setPropertyId(Long propertyId) {
            this.propertyId = propertyId;
        }

        public String getVisitDate() {
            return visitDate;
        }

        public void setVisitDate(String visitDate) {
            this.visitDate = visitDate;
        }
    }

    // PUT: schedule a visit (Landlord action)
    @PutMapping("/{id}/schedule")
    public ResponseEntity<PropertyVisit> scheduleVisit(@PathVariable Long id, @RequestBody String visitDate) {
        PropertyVisit updatedVisit = visitService.scheduleVisit(id, visitDate);
        return ResponseEntity.ok(updatedVisit);
    }


    // PUT: update status
    @PutMapping("/{id}/update-status")
    public ResponseEntity<PropertyVisit> updateVisitStatus(@PathVariable Long id, @RequestBody VisitStatus status) {
        PropertyVisit updatedVisit = visitService.updateVisitStatus(id, status);
        return ResponseEntity.ok(updatedVisit);
    }

    // GET: get taken time slots for a property on a specific date
    @GetMapping("/taken-slots/{propertyId}")
    public ResponseEntity<List<String>> getTakenTimeSlots(
            @PathVariable Long propertyId,
            @RequestParam String date) {
        List<String> takenSlots = visitService.getTakenTimeSlots(propertyId, date);
        return ResponseEntity.ok(takenSlots);
    }

    // GET: check if user has existing visit for a property
    @GetMapping("/check-existing/{propertyId}")
    public ResponseEntity<Map<String, Boolean>> checkExistingVisit(
            @PathVariable Long propertyId,
            @RequestParam String username) {
        boolean hasVisit = visitService.hasExistingVisit(propertyId, username);
        Map<String, Boolean> response = new HashMap<>();
        response.put("hasVisit", hasVisit);
        return ResponseEntity.ok(response);
    }

    // GET: get user's current visit for a property
    @GetMapping("/current-visit/{propertyId}")
    public ResponseEntity<?> getCurrentVisit(
            @PathVariable Long propertyId,
            @RequestParam String username) {
        try {
            PropertyVisit visit = visitService.getCurrentVisit(propertyId, username);
            if (visit == null) {
                Map<String, Object> emptyResponse = new HashMap<>();
                emptyResponse.put("visitId", null);
                emptyResponse.put("visitDate", null);
                emptyResponse.put("status", null);
                return ResponseEntity.ok(emptyResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("visitId", visit.getVisitId());
            response.put("visitDate", visit.getVisitDate());
            response.put("status", visit.getVisitStatus());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to load visit: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // PUT: update existing visit
    @PutMapping("/update/{propertyId}")
    public ResponseEntity<?> updateVisit(
            @PathVariable Long propertyId,
            @RequestBody VisitRequest request) {
        try {
            PropertyVisit updatedVisit = visitService.updateVisit(propertyId, request.getVisitDate());
            return ResponseEntity.ok(updatedVisit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE: remove the record entirely if needed
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.ok("Visit deleted.");
    }

    @DeleteMapping("/cancel/{propertyId}")
    public ResponseEntity<?> cancelVisit(@PathVariable Long propertyId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            visitService.cancelVisit(propertyId, username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to cancel visit: " + e.getMessage());
        }
    }
}
