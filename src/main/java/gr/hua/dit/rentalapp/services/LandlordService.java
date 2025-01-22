package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.entities.PropertyVisit;
import gr.hua.dit.rentalapp.entities.RentalApplication;
import gr.hua.dit.rentalapp.repositories.LandlordRepository;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import gr.hua.dit.rentalapp.repositories.PropertyVisitRepository;
import gr.hua.dit.rentalapp.repositories.RentalApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LandlordService {

    private final LandlordRepository landlordRepository;
    private final PropertyRepository propertyRepository;
    private final RentalApplicationRepository rentalApplicationRepository;
    private final PropertyVisitRepository propertyVisitRepository;

    @Autowired
    public LandlordService(LandlordRepository landlordRepository,
                          PropertyRepository propertyRepository,
                          RentalApplicationRepository rentalApplicationRepository,
                          PropertyVisitRepository propertyVisitRepository) {
        this.landlordRepository = landlordRepository;
        this.propertyRepository = propertyRepository;
        this.rentalApplicationRepository = rentalApplicationRepository;
        this.propertyVisitRepository = propertyVisitRepository;
    }

    public List<Landlord> getAllLandlords() {
        return landlordRepository.findAll();
    }

    public Landlord getLandlordById(Long id) {
        return landlordRepository.findById(id).orElse(null);
    }

    public Landlord getLandlordByUsername(String username) {
        return landlordRepository.findByUsername(username).orElse(null);
    }

    public Landlord createLandlord(Landlord landlord) {
        // Possibly encode password, set roles
        return landlordRepository.save(landlord);
    }

    public void updateLandlord(Long id, Landlord landlordDetails) {
        Landlord existing = getLandlordById(id);
        if (existing != null) {
            existing.setEmail(landlordDetails.getEmail());
            existing.setPhoneNumber(landlordDetails.getPhoneNumber());
            landlordRepository.save(existing);
        } else {
            throw new RuntimeException("Landlord not found: " + id);
        }
    }

    public void deleteLandlord(Long id) {
        landlordRepository.deleteById(id);
    }

    public List<Property> getPropertiesByLandlord(Long landlordId) {
        return propertyRepository.findByOwnerUserId(landlordId);
    }

    public List<RentalApplication> getRentalApplicationsForLandlord(Long landlordId) {
        // Get all properties owned by the landlord
        List<Property> properties = getPropertiesByLandlord(landlordId);
        if (properties.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all applications for these properties
        return properties.stream()
            .flatMap(property -> rentalApplicationRepository.findByPropertyPropertyId(property.getPropertyId()).stream())
            .collect(Collectors.toList());
    }

    public List<PropertyVisit> getPropertyVisitsForLandlord(Long landlordId) {
        // Get all properties owned by the landlord
        List<Property> properties = getPropertiesByLandlord(landlordId);
        if (properties.isEmpty()) {
            return new ArrayList<>();
        }

        // Get all visits for these properties
        return properties.stream()
            .flatMap(property -> propertyVisitRepository.findByProperty_PropertyId(property.getPropertyId()).stream())
            .collect(Collectors.toList());
    }
}
