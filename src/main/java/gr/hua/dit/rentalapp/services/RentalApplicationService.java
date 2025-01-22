package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.enums.ApplicationStatus;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class RentalApplicationService {

    private final RentalApplicationRepository applicationRepository;
    private final TenantRepository tenantRepository;
    private final PropertyVisitRepository visitRepository;
    private final PropertyRepository propertyRepository;

    @Autowired
    public RentalApplicationService(RentalApplicationRepository applicationRepository, 
                                  TenantRepository tenantRepository,
                                  PropertyVisitRepository visitRepository,
                                  PropertyRepository propertyRepository) {
        this.applicationRepository = applicationRepository;
        this.tenantRepository = tenantRepository;
        this.visitRepository = visitRepository;
        this.propertyRepository = propertyRepository;
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
        
        // If the application is being approved
        if (status == ApplicationStatus.APPROVED) {
            Property property = existing.getProperty();
            
            // Mark the property as rented
            property.setRented(true);
            propertyRepository.save(property);
            
            // Reject all other applications for this property
            List<RentalApplication> otherApplications = applicationRepository.findByPropertyAndStatusAndApplicationIdNot(
                property, ApplicationStatus.PENDING, id);
            for (RentalApplication app : otherApplications) {
                app.setStatus(ApplicationStatus.REJECTED);
                applicationRepository.save(app);
            }
            
            // Cancel all pending visits for this property
            List<VisitStatus> activeStatuses = Arrays.asList(VisitStatus.REQUESTED, VisitStatus.SCHEDULED);
            List<PropertyVisit> activeVisits = visitRepository.findByPropertyAndVisitStatusIn(
                property, activeStatuses);
            for (PropertyVisit visit : activeVisits) {
                visit.setVisitStatus(VisitStatus.CANCELED);
                visitRepository.save(visit);
            }
        }
        
        // Update the status of the current application
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
