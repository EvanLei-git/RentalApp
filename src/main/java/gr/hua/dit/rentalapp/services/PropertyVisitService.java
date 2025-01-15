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
import java.util.Arrays;
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

    public PropertyVisit scheduleVisit(Long id, Date visitDate) {
        PropertyVisit visit = propertyVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found!"));
        visit.setVisitDate(visitDate);
        visit.setVisitStatus(VisitStatus.SCHEDULED);
        return propertyVisitRepository.save(visit);
    }

    public PropertyVisit approveVisit(Long id) {
        PropertyVisit visit = propertyVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found!"));
        
        if (visit.getVisitStatus() != VisitStatus.REQUESTED) {
            throw new RuntimeException("Can only approve visits that are in REQUESTED status");
        }
        
        visit.setVisitStatus(VisitStatus.SCHEDULED);
        return propertyVisitRepository.save(visit);
    }

    public PropertyVisit cancelVisit(Long id) {
        PropertyVisit visit = getVisitById(id);
        
        // If the visit is in REQUESTED status, mark it as DECLINED instead of CANCELED
        VisitStatus newStatus = (visit.getVisitStatus() == VisitStatus.REQUESTED) ? 
                              VisitStatus.DECLINED : VisitStatus.CANCELED;
        
        if (newStatus == VisitStatus.DECLINED) {
            // Find and remove any previous declined visits for this property and user
            List<PropertyVisit> previousDeclines = propertyVisitRepository
                    .findByProperty_PropertyIdAndTenant_UsernameAndVisitStatus(
                        visit.getProperty().getPropertyId(),
                        visit.getTenant().getUsername(),
                        VisitStatus.DECLINED
                    );
            
            // Remove all previous declines except the current one
            previousDeclines.stream()
                          .filter(v -> !v.getVisitId().equals(visit.getVisitId()))
                          .forEach(propertyVisitRepository::delete);
        }

        visit.setVisitStatus(newStatus);
        return propertyVisitRepository.save(visit);
    }

    public PropertyVisit cancelVisit(Long propertyId, String username) {
        try {
            // Find the current active visit
            List<PropertyVisit> activeVisits = propertyVisitRepository
                    .findByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(
                        propertyId,
                        username,
                        Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
                    );

            if (activeVisits.isEmpty()) {
                throw new RuntimeException("No active visit found to cancel");
            }

            PropertyVisit visit = activeVisits.get(0);
            
            // If the visit is in REQUESTED status, mark it as DECLINED instead of CANCELED
            VisitStatus newStatus = (visit.getVisitStatus() == VisitStatus.REQUESTED) ? 
                                  VisitStatus.DECLINED : VisitStatus.CANCELED;
            
            if (newStatus == VisitStatus.DECLINED) {
                // Find and remove any previous declined visits for this property and user
                List<PropertyVisit> previousDeclines = propertyVisitRepository
                        .findByProperty_PropertyIdAndTenant_UsernameAndVisitStatus(
                            propertyId,
                            username,
                            VisitStatus.DECLINED
                        );
                
                if (!previousDeclines.isEmpty()) {
                    propertyVisitRepository.deleteAll(previousDeclines);
                }
            }

            visit.setVisitStatus(newStatus);
            return propertyVisitRepository.save(visit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to cancel visit: " + e.getMessage());
        }
    }

    public PropertyVisit completeVisit(Long id) {
        PropertyVisit visit = propertyVisitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visit not found!"));
        
        if (visit.getVisitStatus() != VisitStatus.SCHEDULED) {
            throw new RuntimeException("Can only complete visits that are SCHEDULED");
        }
        
        // Check if visit date has passed
        Date currentDate = new Date();
        if (currentDate.after(visit.getVisitDate())) {
            visit.setVisitStatus(VisitStatus.COMPLETED);
            return propertyVisitRepository.save(visit);
        }
        
        throw new RuntimeException("Cannot complete a visit before its scheduled date");
    }

    // Add a method to automatically complete past visits
    public void autoCompleteVisits() {
        Date currentDate = new Date();
        List<PropertyVisit> scheduledVisits = propertyVisitRepository.findByVisitStatus(VisitStatus.SCHEDULED);
        
        for (PropertyVisit visit : scheduledVisits) {
            if (currentDate.after(visit.getVisitDate())) {
                visit.setVisitStatus(VisitStatus.COMPLETED);
                propertyVisitRepository.save(visit);
            }
        }
    }

    public List<PropertyVisit> getVisitsByProperty(Long propertyId) {
        return propertyVisitRepository.findByProperty_PropertyId(propertyId);
    }

    public synchronized PropertyVisit createAndScheduleVisit(Long propertyId, Date requestedDate) {
        try {
            // Check if the requested time slot is available (only check SCHEDULED and REQUESTED visits)
            List<PropertyVisit> existingVisits = propertyVisitRepository
                    .findByProperty_PropertyIdAndVisitDateBetweenAndVisitStatusIn(
                        propertyId, 
                        requestedDate,
                        requestedDate,
                        Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
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

            PropertyVisit visit = new PropertyVisit();
            visit.setProperty(property);
            visit.setTenant(tenant);
            visit.setLandlord(property.getOwner());
            visit.setVisitDate(requestedDate);
            visit.setVisitStatus(VisitStatus.REQUESTED);

            return propertyVisitRepository.save(visit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to schedule visit: " + e.getMessage());
        }
    }

    public boolean hasExistingVisit(Long propertyId, String username) {
        return propertyVisitRepository.existsByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(
            propertyId, 
            username,
            Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
        );
    }

    public PropertyVisit getCurrentVisit(Long propertyId, String username) {
        try {
            List<PropertyVisit> activeVisits = propertyVisitRepository
                    .findByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(
                        propertyId,
                        username,
                        Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
                    );
            
            // Return the most recent active visit if any
            PropertyVisit visit = activeVisits.isEmpty() ? null : activeVisits.get(0);
            
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

    public PropertyVisit updateVisit(Long propertyId, Date requestedDate) {
        try {
            // Get the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Find the current active visit
            List<PropertyVisit> activeVisits = propertyVisitRepository
                    .findByProperty_PropertyIdAndTenant_UsernameAndVisitStatusIn(
                        propertyId,
                        username,
                        Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
                    );

            if (activeVisits.isEmpty()) {
                throw new RuntimeException("No active visit found to update");
            }

            PropertyVisit visit = activeVisits.get(0);

            // Check if there are other visits at the same time (excluding this visit)
            List<PropertyVisit> existingVisits = propertyVisitRepository
                    .findByProperty_PropertyIdAndVisitDateBetweenAndVisitStatusIn(
                        propertyId,
                        requestedDate,
                        requestedDate,
                        Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
                    ).stream()
                    .filter(v -> !v.getVisitId().equals(visit.getVisitId()))
                    .toList();

            if (!existingVisits.isEmpty()) {
                throw new RuntimeException("This time slot is already taken. Please choose a different time.");
            }

            visit.setVisitDate(requestedDate);
            return propertyVisitRepository.save(visit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update visit: " + e.getMessage());
        }
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

            // Get only active visits (SCHEDULED or REQUESTED)
            List<PropertyVisit> visits = propertyVisitRepository
                    .findByProperty_PropertyIdAndVisitDateBetweenAndVisitStatusIn(
                        propertyId,
                        startDate,
                        endDate,
                        Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
                    );

            // Extract the time slots that are taken
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            return visits.stream()
                    .map(visit -> timeFormat.format(visit.getVisitDate()))
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd", e);
        }
    }

    public List<PropertyVisit> getVisitsByTenant(String username) {
        // Only return active visits
        return propertyVisitRepository.findByTenant_UsernameAndVisitStatusIn(
            username,
            Arrays.asList(VisitStatus.SCHEDULED, VisitStatus.REQUESTED)
        );
    }
}
