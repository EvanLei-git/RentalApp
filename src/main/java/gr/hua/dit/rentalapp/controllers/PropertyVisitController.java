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
    public ResponseEntity<String> requestVisit(@RequestBody PropertyVisit visit) {
        visit.setVisitStatus(VisitStatus.REQUESTED);
        visitService.createVisit(visit);
        return ResponseEntity.ok("Visit requested successfully!");
    }

    // PUT: schedule a visit (Landlord action)
    @PutMapping("/{id}/schedule")
    public ResponseEntity<String> scheduleVisit(@PathVariable Long id, @RequestBody PropertyVisit updatedVisit) {
        visitService.scheduleVisit(id, updatedVisit.getVisitDate());
        return ResponseEntity.ok("Visit scheduled successfully!");
    }

    // PUT: cancel a visit (Tenant or Landlord)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<String> cancelVisit(@PathVariable Long id) {
        visitService.cancelVisit(id);
        return ResponseEntity.ok("Visit canceled.");
    }

    // PUT: mark as completed
    @PutMapping("/{id}/complete")
    public ResponseEntity<String> markVisitCompleted(@PathVariable Long id) {
        visitService.markVisitCompleted(id);
        return ResponseEntity.ok("Visit marked as completed.");
    }

    // DELETE: remove the record entirely if needed
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteVisit(@PathVariable Long id) {
        visitService.deleteVisit(id);
        return ResponseEntity.ok("Visit deleted.");
    }
}
