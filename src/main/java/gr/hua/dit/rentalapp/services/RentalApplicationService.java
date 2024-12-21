package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class RentalApplicationService {

    @Autowired
    private RentalApplicationRepository rentalApplicationRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Transactional
    public RentalApplication submitApplication(Tenant applicant, Long propertyId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.isApproved()) {
            throw new RuntimeException("Cannot apply for unapproved property");
        }

        RentalApplication application = new RentalApplication();
        application.setApplicant(applicant);
        application.setProperty(property);
        application.setApplicationDate(new Date());
        application.setStatus(ApplicationStatus.PENDING);

        return rentalApplicationRepository.save(application);
    }

    @Transactional
    public RentalApplication updateApplicationStatus(Long applicationId, ApplicationStatus newStatus) {
        RentalApplication application = rentalApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        application.setStatus(newStatus);
        return rentalApplicationRepository.save(application);
    }

    public List<RentalApplication> getApplicationsByTenant(Long tenantId) {
        return rentalApplicationRepository.findByApplicantId(tenantId);
    }

    public List<RentalApplication> getApplicationsByProperty(Long propertyId) {
        return rentalApplicationRepository.findByPropertyId(propertyId);
    }

    public List<RentalApplication> getPendingApplications() {
        return rentalApplicationRepository.findByStatus(ApplicationStatus.PENDING);
    }

    public List<RentalApplication> getTenantApplicationsByStatus(Long tenantId, ApplicationStatus status) {
        return rentalApplicationRepository.findByApplicantIdAndStatus(tenantId, status);
    }

    public List<RentalApplication> getPropertyApplicationsByStatus(Long propertyId, ApplicationStatus status) {
        return rentalApplicationRepository.findByPropertyIdAndStatus(propertyId, status);
    }

    public RentalApplication getApplication(Long applicationId) {
        return rentalApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
    }

    @Transactional
    public void deleteApplication(Long applicationId) {
        rentalApplicationRepository.deleteById(applicationId);
    }
}
