package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PropertyVisitService {

    private final PropertyVisitRepository propertyVisitRepository;
    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;

    @Autowired
    public PropertyVisitService(PropertyVisitRepository propertyVisitRepository, PropertyRepository propertyRepository, TenantRepository tenantRepository) {
        this.propertyVisitRepository = propertyVisitRepository;
        this.propertyRepository = propertyRepository;
        this.tenantRepository = tenantRepository;
    }

    public List<PropertyVisit> getAllVisits() {
        return propertyVisitRepository.findAll();
    }

    public PropertyVisit getVisitById(Long id) {
        return propertyVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found!"));
    }

    public PropertyVisit createVisit(Long tenantId, Long propertyId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found!"));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found!"));

        PropertyVisit visit = new PropertyVisit();
        visit.setTenant(tenant);
        visit.setProperty(property);
        visit.setLandlord(property.getOwner());
        visit.setVisitStatus(VisitStatus.REQUESTED);
        visit.setVisitDate(new Date()); // Set default visit date for now

        return propertyVisitRepository.save(visit);
    }

    public PropertyVisit scheduleVisit(Long id, String visitDate) {
        PropertyVisit visit = propertyVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found!"));
        try {
            Date parsedDate = new Date(Long.parseLong(visitDate)); // Parse the date from string
            visit.setVisitDate(parsedDate);
            visit.setVisitStatus(VisitStatus.SCHEDULED);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format");
        }

        return propertyVisitRepository.save(visit);
    }

    public PropertyVisit cancelVisit(Long id) {
        PropertyVisit visit = propertyVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found!"));
        visit.setVisitStatus(VisitStatus.CANCELED);

        return propertyVisitRepository.save(visit);
    }

    public PropertyVisit updateVisitStatus(Long id, VisitStatus visitStatus) {
        PropertyVisit visit = propertyVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found!"));
        visit.setVisitStatus(visitStatus);

        return propertyVisitRepository.save(visit);
    }

    public void deleteVisit(Long id) {
        propertyVisitRepository.deleteById(id);
    }

    public List<PropertyVisit> getVisitsByProperty(Long propertyId) {
        return propertyVisitRepository.findByProperty_PropertyId(propertyId);
    }

    public PropertyVisit createAndScheduleVisit(Long propertyId, String visitDateStr) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found!"));

        // Get the current authenticated user as tenant
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Tenant tenant = tenantRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tenant not found!"));

        PropertyVisit visit = new PropertyVisit();
        visit.setTenant(tenant);
        visit.setProperty(property);
        visit.setLandlord(property.getOwner());
        visit.setVisitStatus(VisitStatus.SCHEDULED);
        
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            Date visitDate = dateFormat.parse(visitDateStr);
            visit.setVisitDate(visitDate);
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format");
        }

        return propertyVisitRepository.save(visit);
    }

    public boolean hasExistingVisit(Long propertyId, String username) {
        return propertyVisitRepository.existsByPropertyPropertyIdAndTenantUsername(propertyId, username);
    }
}
