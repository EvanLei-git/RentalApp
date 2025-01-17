package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.repositories.AdministratorRepository;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.of;

@Service
public class AdministratorService {

    private final AdministratorRepository adminRepository;
    private final TenantRepository tenantRepository;
    private final PropertyRepository propertyRepository;
    private final RentalApplicationRepository rentalApplicationRepository;

    @Autowired
    public AdministratorService(AdministratorRepository adminRepository,
                                TenantRepository tenantRepository,
                                PropertyRepository propertyRepository,
                                RentalApplicationRepository rentalApplicationRepository) {
        this.adminRepository = adminRepository;
        this.tenantRepository = tenantRepository;
        this.propertyRepository = propertyRepository;
        this.rentalApplicationRepository = rentalApplicationRepository;
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

    public List<Map<String, Object>> getAllRentals() {
        List<RentalApplication> rentals = rentalApplicationRepository.findAll();
        return rentals.stream().map(rental -> {
            Map<String, Object> rentalMap = new HashMap<>();
            rentalMap.put("id", rental.getId());
            rentalMap.put("property", Map.of(
                "id", rental.getProperty().getPropertyId(),
                "title", rental.getProperty().getAddress()
            ));
            rentalMap.put("tenant", Map.of(
                "id", rental.getTenant().getUserId(),
                "firstName", rental.getTenant().getFirstName(),
                "lastName", rental.getTenant().getLastName()
            ));
            rentalMap.put("startDate", rental.getStartDate());
            rentalMap.put("endDate", rental.getEndDate());
            rentalMap.put("status", rental.getStatus());
            return rentalMap;
        }).collect(Collectors.toList());
    }

    public void approveRentalApplication(Long adminId, Long rentalId) {
        Administrator admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrator not found: " + adminId));
        
        RentalApplication rental = rentalApplicationRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental application not found: " + rentalId));

        if (rental.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Cannot approve rental application that is not in PENDING state");
        }

        rental.setStatus(ApplicationStatus.APPROVED);
        rentalApplicationRepository.save(rental);
    }

    public void rejectRentalApplication(Long adminId, Long rentalId) {
        Administrator admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Administrator not found: " + adminId));
        
        RentalApplication rental = rentalApplicationRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental application not found: " + rentalId));

        if (rental.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Cannot reject rental application that is not in PENDING state");
        }

        rental.setStatus(ApplicationStatus.REJECTED);
        rentalApplicationRepository.save(rental);
    }

    public RentalApplication getRentalApplicationById(Long rentalId) {
        return rentalApplicationRepository.findById(rentalId)
                .orElseThrow(() -> new RuntimeException("Rental application not found: " + rentalId));
    }
}
