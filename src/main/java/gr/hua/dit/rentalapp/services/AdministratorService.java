package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.repositories.AdministratorRepository;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdministratorService {

    private final AdministratorRepository adminRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;

    @Autowired
    public AdministratorService(AdministratorRepository adminRepository,
                                TenantRepository tenantRepository,
                                PropertyRepository propertyRepository) {
        this.adminRepository = adminRepository;
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
    }

    public List<Administrator> getAllAdministrators() {
        return adminRepository.findAll();
    }

    public Administrator getAdministratorById(Long id) {
        return adminRepository.findById(id).orElse(null);
    }

    public void createAdministrator(Administrator administrator) {
        // Possibly encode password, set role to ROLE_ADMIN
        adminRepository.save(administrator);
    }

    public void verifyTenant(Long adminId, Long tenantId) {
        Administrator admin = adminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            throw new RuntimeException("Administrator not found: " + adminId);
        }
        Tenant tenant = tenantRepository.findById(tenantId).orElse(null);
        if (tenant == null) {
            throw new RuntimeException("Tenant not found: " + tenantId);
        }
        // Perform verification
        tenant.setBackgroundCheckCleared(true);
        tenant.setVerifiedBy(admin);
        tenantRepository.save(tenant);
    }

    public void approvePropertyListing(Long adminId, Long propertyId) {
        Administrator admin = adminRepository.findById(adminId).orElse(null);
        if (admin == null) {
            throw new RuntimeException("Administrator not found: " + adminId);
        }
        Property property = propertyRepository.findById(propertyId).orElse(null);
        if (property == null) {
            throw new RuntimeException("Property not found: " + propertyId);
        }
        property.setApproved(true);
        property.setVerifiedBy(admin);
        propertyRepository.save(property);
    }
}
