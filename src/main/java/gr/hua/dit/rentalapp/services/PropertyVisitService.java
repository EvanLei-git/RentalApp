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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Service
public class PropertyVisitService {

    private final PropertyVisitRepository propertyVisitRepository;
    private final PropertyRepository propertyRepository;
    private final TenantRepository tenantRepository;

    private static final Object SCHEDULE_LOCK = new Object();

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
        synchronized(SCHEDULE_LOCK) {
            try {
                // Parse the requested date first
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                Date requestedDate = dateFormat.parse(visitDateStr);

                // Check if the exact time slot is already taken
                List<PropertyVisit> existingVisits = propertyVisitRepository
                    .findByProperty_PropertyIdAndVisitDateBetween(
                        propertyId, 
                        requestedDate,
                        requestedDate
                    );

                if (!existingVisits.isEmpty()) {
                    throw new RuntimeException("This time slot is already taken. Please choose a different time.");
                }

                Property property = propertyRepository.findById(propertyId)
                        .orElseThrow(() -> new RuntimeException("Property not found!"));

                // Get the current authenticated user as tenant
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                Tenant tenant = tenantRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Tenant not found!"));

                // Check for existing visits and delete them
                PropertyVisit existingVisit = propertyVisitRepository.findByProperty_PropertyIdAndTenant_Username(propertyId, username);
                if (existingVisit != null) {
                    propertyVisitRepository.delete(existingVisit);
                }

                PropertyVisit visit = new PropertyVisit();
                visit.setTenant(tenant);
                visit.setProperty(property);
                visit.setLandlord(property.getOwner());
                visit.setVisitStatus(VisitStatus.SCHEDULED);
                visit.setVisitDate(requestedDate);

                return propertyVisitRepository.save(visit);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid date format");
            }
        }
    }

    public boolean hasExistingVisit(Long propertyId, String username) {
        return propertyVisitRepository.existsByProperty_PropertyIdAndTenant_Username(propertyId, username);
    }

    public PropertyVisit getCurrentVisit(Long propertyId, String username) {
        try {
            PropertyVisit visit = propertyVisitRepository.findByProperty_PropertyIdAndTenant_Username(propertyId, username);
            if (visit != null && visit.getVisitDate() != null) {
                // Ensure the date is properly formatted
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                String formattedDate = dateFormat.format(visit.getVisitDate());
                visit.setVisitDate(dateFormat.parse(formattedDate));
            }
            return visit;
        } catch (Exception e) {
            // Log the error but don't throw it
            System.err.println("Error retrieving visit: " + e.getMessage());
            return null;
        }
    }

    public PropertyVisit updateVisit(Long propertyId, String visitDateStr) {
        synchronized(SCHEDULE_LOCK) {
            try {
                // Parse the requested date first
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                Date requestedDate = dateFormat.parse(visitDateStr);

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                
                PropertyVisit currentVisit = propertyVisitRepository.findByProperty_PropertyIdAndTenant_Username(propertyId, username);
                if (currentVisit == null) {
                    throw new RuntimeException("No existing visit found");
                }

                // Check if the exact time slot is already taken by someone else
                List<PropertyVisit> existingVisits = propertyVisitRepository
                    .findByProperty_PropertyIdAndVisitDateBetween(
                        propertyId, 
                        requestedDate,
                        requestedDate
                    )
                    .stream()
                    .filter(visit -> !visit.getVisitId().equals(currentVisit.getVisitId()))
                    .toList();

                if (!existingVisits.isEmpty()) {
                    throw new RuntimeException("This time slot is already taken. Please choose a different time.");
                }

                currentVisit.setVisitDate(requestedDate);
                return propertyVisitRepository.save(currentVisit);
            } catch (ParseException e) {
                throw new RuntimeException("Invalid date format");
            }
        }
    }

    public void cancelVisit(Long propertyId, String username) {
        PropertyVisit visit = propertyVisitRepository.findByProperty_PropertyIdAndTenant_Username(propertyId, username);
        if (visit == null) {
            throw new RuntimeException("No visit found to cancel");
        }
        propertyVisitRepository.delete(visit);
    }

    public List<String> getTakenTimeSlots(Long propertyId, String dateStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormat.parse(dateStr);
            
            // Set time to start of day
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startDate = calendar.getTime();
            
            // Set time to end of day
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            Date endDate = calendar.getTime();

            // Get all visits for this property on this date
            List<PropertyVisit> visits = propertyVisitRepository.findByProperty_PropertyIdAndVisitDateBetween(
                    propertyId, startDate, endDate);

            // Extract the time slots that are taken
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            return visits.stream()
                    .map(visit -> timeFormat.format(visit.getVisitDate()))
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd", e);
        }
    }
}
