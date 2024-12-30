package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.services.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    @Autowired
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    // GET all tenants (likely for admin or landlord usage)
    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    // GET tenant by ID
    @GetMapping("/{id}")
    public Tenant getTenantById(@PathVariable Long id) {
        return tenantService.getTenantById(id);
    }

    // PUT: update tenant profile, e.g., employment status, monthly income
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTenant(@PathVariable Long id, @RequestBody Tenant tenantDetails) {
        tenantService.updateTenant(id, tenantDetails);
        return ResponseEntity.ok("Tenant updated successfully!");
    }

    // todo: GET /api/tenants/{id}/viewed-properties
    // todo: GET /api/tenants/{id}/applications
}
