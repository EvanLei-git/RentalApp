package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.services.AdministratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/administrators")
public class AdministratorController {

    private final AdministratorService adminService;

    @Autowired
    public AdministratorController(AdministratorService adminService) {
        this.adminService = adminService;
    }

    // GET all administrators
    @GetMapping
    public List<Administrator> getAllAdministrators() {
        return adminService.getAllAdministrators();
    }

    // GET admin by ID
    @GetMapping("/{id}")
    public Administrator getAdministratorById(@PathVariable Long id) {
        return adminService.getAdministratorById(id);
    }

    // PUT: verify a tenant
    @PutMapping("/{adminId}/verify-tenant/{tenantId}")
    public ResponseEntity<String> verifyTenant(@PathVariable Long adminId, @PathVariable Long tenantId) {
        adminService.verifyTenant(adminId, tenantId);
        return ResponseEntity.ok("Tenant verified successfully!");
    }

    // PUT: approve or reject a property
    @PutMapping("/{adminId}/approve-property/{propertyId}")
    public ResponseEntity<String> approveProperty(@PathVariable Long adminId, @PathVariable Long propertyId) {
        adminService.approvePropertyListing(adminId, propertyId);
        return ResponseEntity.ok("Property approved successfully!");
    }

}
