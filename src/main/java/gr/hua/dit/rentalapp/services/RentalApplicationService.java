package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RentalApplicationService {

    private final RentalApplicationRepository applicationRepository;
    private final TenantRepository tenantRepository;

    @Autowired
    public RentalApplicationService(RentalApplicationRepository applicationRepository, TenantRepository tenantRepository) {
        this.applicationRepository = applicationRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<RentalApplication> getAllApplications() {
        return applicationRepository.findAll();
    }

    public RentalApplication getApplicationById(Long id) {
        return applicationRepository.findById(id).orElse(null);
    }

    public RentalApplication createApplication(RentalApplication application) {
        // set status to PENDING, set date, etc.
        // application.setStatus(ApplicationStatus.PENDING); // todo: might have to move it to the constructor
        return applicationRepository.save(application);
    }

    public void updateApplicationStatus(Long id, ApplicationStatus status) {
        RentalApplication existing = applicationRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("Application not found: " + id);
        }
        existing.setStatus(status);
        applicationRepository.save(existing);
    }

    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }

    public List<RentalApplication> getApplicationsByTenant(String username) {
        Tenant tenant = tenantRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return applicationRepository.findByApplicantUserId(tenant.getUserId());
    }
}
