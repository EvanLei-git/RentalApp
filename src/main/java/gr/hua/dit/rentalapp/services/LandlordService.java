package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.repositories.LandlordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LandlordService {

    private final LandlordRepository landlordRepository;

    @Autowired
    public LandlordService(LandlordRepository landlordRepository) {
        this.landlordRepository = landlordRepository;
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
        Landlord landlord = getLandlordById(landlordId);
        return landlord != null ? landlord.getProperties() : new ArrayList<>();
    }
}
