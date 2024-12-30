package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.enums.VisitStatus;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PropertyVisitService {

    private final PropertyVisitRepository visitRepository;

    @Autowired
    public PropertyVisitService(PropertyVisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    public List<PropertyVisit> getAllVisits() {
        return visitRepository.findAll();
    }

    public PropertyVisit getVisitById(Long id) {
        return visitRepository.findById(id).orElse(null);
    }

    public PropertyVisit createVisit(PropertyVisit visit) {
        // Default the status to REQUESTED if needed
        if (visit.getVisitStatus() == null) {
            visit.setVisitStatus(VisitStatus.REQUESTED);
        }
        return visitRepository.save(visit);
    }

    public void scheduleVisit(Long visitId, Date scheduledDate) {
        PropertyVisit visit = visitRepository.findById(visitId).orElse(null);
        if (visit == null) {
            throw new RuntimeException("Visit not found: " + visitId);
        }
        visit.setVisitDate(scheduledDate);
        visit.setVisitStatus(VisitStatus.SCHEDULED);
        visitRepository.save(visit);
    }

    public void cancelVisit(Long visitId) {
        PropertyVisit visit = visitRepository.findById(visitId).orElse(null);
        if (visit == null) {
            throw new RuntimeException("Visit not found: " + visitId);
        }
        visit.setVisitStatus(VisitStatus.CANCELED);
        visitRepository.save(visit);
    }

    public void markVisitCompleted(Long visitId) {
        PropertyVisit visit = visitRepository.findById(visitId).orElse(null);
        if (visit == null) {
            throw new RuntimeException("Visit not found: " + visitId);
        }
        visit.setVisitStatus(VisitStatus.COMPLETED);
        visitRepository.save(visit);
    }

    public void deleteVisit(Long visitId) {
        visitRepository.deleteById(visitId);
    }
}
