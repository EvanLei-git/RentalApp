package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    @Autowired
    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id).orElse(null);
    }

    public Tenant createTenant(Tenant tenant) {
        // Possibly encode password or assign default roles here
        return tenantRepository.save(tenant);
    }

    public void updateTenant(Long id, Tenant tenantDetails) {
        Tenant existing = tenantRepository.findById(id).orElseThrow(() -> new RuntimeException("Tenant not found: " + id));

        // Update fields if provided (null-safe updates)
        if (tenantDetails.getEmail() != null) {
            existing.setEmail(tenantDetails.getEmail());
        }
        if (tenantDetails.getEmploymentStatus() != null) {
            existing.setEmploymentStatus(tenantDetails.getEmploymentStatus());
        }
        if (tenantDetails.getMonthlyIncome() != null) {
            existing.setMonthlyIncome(tenantDetails.getMonthlyIncome());
        }
        if (tenantDetails.isBackgroundCheckCleared() != existing.isBackgroundCheckCleared()) {
            existing.setBackgroundCheckCleared(tenantDetails.isBackgroundCheckCleared());
        }
        // Save updated tenant
        tenantRepository.save(existing);
    }


    public void deleteTenant(Long id) {
        tenantRepository.deleteById(id);
    }
}
