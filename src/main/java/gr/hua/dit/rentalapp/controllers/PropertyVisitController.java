package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.services.PropertyVisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class PropertyVisitController {

    private final PropertyVisitService visitService;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    @Autowired
    public PropertyVisitController(PropertyVisitService visitService) {
        this.visitService = visitService;
    }

    private Date parseVisitDate(String dateStr) throws ParseException {
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new ParseException("Invalid date format. Expected format: yyyy-MM-dd'T'HH:mm", e.getErrorOffset());
        }
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
    public ResponseEntity<?> scheduleVisit(@RequestBody(required = false) Map<String, Object> requestBody,
                                         @RequestParam(required = false) Long propertyId,
                                         @RequestParam(required = false) String visitDate) {
        try {
            // Extract values from either request body or parameters
            Long finalPropertyId = propertyId;
            String finalVisitDate = visitDate;
            
            if (requestBody != null) {
                finalPropertyId = Long.valueOf(requestBody.get("propertyId").toString());
                finalVisitDate = (String) requestBody.get("visitDate");
            }
            
            if (finalPropertyId == null || finalVisitDate == null) {
                throw new IllegalArgumentException("Property ID and visit date are required");
            }

            Date parsedDate = parseVisitDate(finalVisitDate);
            
            // Check if visit exists
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            boolean hasVisit = visitService.hasExistingVisit(finalPropertyId, username);
            if (hasVisit) {
                // Update existing visit
                PropertyVisit updatedVisit = visitService.updateVisit(finalPropertyId, parsedDate);
                return ResponseEntity.ok(updatedVisit);
            } else {
                // Create new visit
                PropertyVisit visit = visitService.createAndScheduleVisit(finalPropertyId, parsedDate);
                return ResponseEntity.ok(visit);
            }
        } catch (ParseException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid date format: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // PUT: schedule a visit (Landlord action)
    @PutMapping("/{id}/schedule")
    public ResponseEntity<?> scheduleLandlordVisit(@PathVariable Long id, @RequestBody String visitDate) {
        try {
            Date parsedDate = parseVisitDate(visitDate);
            PropertyVisit updatedVisit = visitService.scheduleVisit(id, parsedDate);
            return ResponseEntity.ok(updatedVisit);
        } catch (ParseException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid date format: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // PUT: update status
    @PutMapping("/{id}/update-status")
    public ResponseEntity<?> updateVisitStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String action = request.get("action");
            PropertyVisit updatedVisit;
            
            switch (action.toUpperCase()) {
                case "APPROVE":
                    updatedVisit = visitService.approveVisit(id);
                    break;
                case "COMPLETE":
                    updatedVisit = visitService.completeVisit(id);
                    break;
                case "CANCEL":
                    updatedVisit = visitService.cancelVisit(id);
                    break;
                default:
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid action. Must be one of: APPROVE, COMPLETE, CANCEL");
                    return ResponseEntity.badRequest().body(errorResponse);
            }
            
            return ResponseEntity.ok(updatedVisit);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
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
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load visit: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // PUT: update existing visit
    @PutMapping("/update/{propertyId}")
    public ResponseEntity<?> updateVisit(
            @PathVariable Long propertyId,
            @RequestBody(required = false) Map<String, Object> requestBody,
            @RequestParam(required = false) String visitDate) {
        try {
            String finalVisitDate = visitDate;
            if (requestBody != null && requestBody.containsKey("visitDate")) {
                finalVisitDate = (String) requestBody.get("visitDate");
            }
            
            if (finalVisitDate == null) {
                throw new IllegalArgumentException("Visit date is required");
            }

            Date parsedDate = parseVisitDate(finalVisitDate);
            PropertyVisit updatedVisit = visitService.updateVisit(propertyId, parsedDate);
            return ResponseEntity.ok(updatedVisit);
        } catch (ParseException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid date format: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/cancel/{propertyId}")
    public ResponseEntity<?> cancelVisitByPropertyId(@PathVariable Long propertyId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            PropertyVisit visit = visitService.cancelVisit(propertyId, username);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Visit cancelled successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to cancel visit: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // DELETE: remove the record entirely if needed
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVisit(@PathVariable Long id) {
        try {
            PropertyVisit visit = visitService.cancelVisit(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Visit deleted successfully.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete visit: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
