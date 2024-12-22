package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Calendar;


@Service
public class PropertyVisitService {

    @Autowired
    private PropertyVisitRepository propertyVisitRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Transactional
    public PropertyVisit requestVisit(Long propertyId, Tenant tenant, Date visitDate) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.isApproved()) {
            throw new RuntimeException("Cannot request visit for unapproved property");
        }

        // Check if the property already has a visit scheduled at this time
        List<PropertyVisit> existingVisits = propertyVisitRepository
                .findByPropertyIdAndVisitDateBetween(propertyId, getStartOfDay(visitDate), getEndOfDay(visitDate));
        
        if (!existingVisits.isEmpty()) {
            throw new RuntimeException("Property already has a visit scheduled for this date");
        }

        PropertyVisit visit = new PropertyVisit();
        visit.setProperty(property);
        visit.setTenant(tenant);
        visit.setLandlord(property.getOwner());
        visit.setVisitDate(visitDate);
        visit.setVisitStatus(VisitStatus.REQUESTED);

        return propertyVisitRepository.save(visit);
    }

    @Transactional
    public PropertyVisit confirmVisit(Long visitId) {
        PropertyVisit visit = propertyVisitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found"));
        
        if (visit.getVisitStatus() != VisitStatus.REQUESTED) {
            throw new RuntimeException("Can only confirm requested visits");
        }
        
        visit.setVisitStatus(VisitStatus.SCHEDULED);
        return propertyVisitRepository.save(visit);
    }

    @Transactional
    public PropertyVisit completeVisit(Long visitId) {
        PropertyVisit visit = propertyVisitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found"));
        
        if (visit.getVisitStatus() != VisitStatus.SCHEDULED) {
            throw new RuntimeException("Can only complete scheduled visits");
        }
        
        visit.setVisitStatus(VisitStatus.COMPLETED);
        return propertyVisitRepository.save(visit);
    }

    public List<PropertyVisit> getPropertyVisits(Long propertyId) {
        return propertyVisitRepository.findByPropertyId(propertyId);
    }

    public List<PropertyVisit> getTenantVisits(Long tenantId) {
        return propertyVisitRepository.findByTenantId(tenantId);
    }

    public List<PropertyVisit> getLandlordVisits(Long landlordId) {
        return propertyVisitRepository.findByLandlordId(landlordId);
    }

    public List<PropertyVisit> getRequestedVisits() {
        return propertyVisitRepository.findByVisitStatus(VisitStatus.REQUESTED);
    }

    public List<PropertyVisit> getScheduledVisits() {
        return propertyVisitRepository.findByVisitStatus(VisitStatus.SCHEDULED);
    }

    public List<PropertyVisit> getVisitsByDateRange(Date startDate, Date endDate) {
        return propertyVisitRepository.findByVisitDateBetween(startDate, endDate);
    }

    public List<PropertyVisit> getPropertyVisitsByStatus(Long propertyId, VisitStatus status) {
        return propertyVisitRepository.findByPropertyIdAndVisitStatus(propertyId, status);
    }

    public List<PropertyVisit> getTenantVisitsByStatus(Long tenantId, VisitStatus status) {
        return propertyVisitRepository.findByTenantIdAndVisitStatus(tenantId, status);
    }

    public List<PropertyVisit> getLandlordVisitsByStatus(Long landlordId, VisitStatus status) {
        return propertyVisitRepository.findByLandlordIdAndVisitStatus(landlordId, status);
    }

    @Transactional
    public void cancelVisit(Long visitId) {
        PropertyVisit visit = propertyVisitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found"));
        
        if (visit.getVisitStatus() == VisitStatus.COMPLETED) {
            throw new RuntimeException("Cannot cancel a completed visit");
        }
        
        visit.setVisitStatus(VisitStatus.CANCELLED);
        propertyVisitRepository.save(visit);
    }

    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }
}
