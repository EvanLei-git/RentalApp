package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.services.PropertyVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<PropertyVisit> scheduleVisit(@RequestBody VisitRequest request) {
        PropertyVisit visit = visitService.createAndScheduleVisit(request.getPropertyId(), request.getVisitDate());
        return ResponseEntity.ok(visit);
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

    // PUT: cancel a visit (Tenant or Landlord)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<PropertyVisit> cancelVisit(@PathVariable Long id) {
        PropertyVisit canceledVisit = visitService.cancelVisit(id);
        return ResponseEntity.ok(canceledVisit);
    }

    // PUT: update status
    @PutMapping("/{id}/update-status")
    public ResponseEntity<PropertyVisit> updateVisitStatus(@PathVariable Long id, @RequestBody VisitStatus status) {
        PropertyVisit updatedVisit = visitService.updateVisitStatus(id, status);
        return ResponseEntity.ok(updatedVisit);
    }

    // DELETE: remove the record entirely if needed
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.ok("Visit deleted.");
    }
}
