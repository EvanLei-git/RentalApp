package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.enums.PropertyType;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PropertyService {

    @Autowired
    private PropertyRepository propertyRepository;

    @Transactional
    public Property createProperty(Property property) {
        property.setApproved(false); // New properties need admin approval
        return propertyRepository.save(property);
    }

    @Transactional
    public Property updateProperty(Property property) {
        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteProperty(Long propertyId) {
        propertyRepository.deleteById(propertyId);
    }

    public Property getProperty(Long propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public List<Property> getApprovedProperties() {
        return propertyRepository.findByIsApproved(true);
    }

    public List<Property> getPendingProperties() {
        return propertyRepository.findByIsApproved(false);
    }

    public List<Property> getLandlordProperties(Landlord landlord) {
        return propertyRepository.findByOwner(landlord);
    }

    public List<Property> searchProperties(String address, PropertyType type, 
                                         Double minRent, Double maxRent,
                                         Integer minBedrooms, Integer minBathrooms) {
        List<Property> properties = propertyRepository.findByIsApproved(true);

        if (address != null) {
            properties.removeIf(p -> !p.getAddress().toLowerCase().contains(address.toLowerCase()));
        }
        if (type != null) {
            properties.removeIf(p -> p.getType() != type);
        }
        if (minRent != null && maxRent != null) {
            properties.removeIf(p -> p.getRentAmount() < minRent || p.getRentAmount() > maxRent);
        }
        if (minBedrooms != null) {
            properties.removeIf(p -> p.getBedrooms() < minBedrooms);
        }
        if (minBathrooms != null) {
            properties.removeIf(p -> p.getBathrooms() < minBathrooms);
        }

        return properties;
    }

    @Transactional
    public Property approveProperty(Long propertyId) {
        Property property = getProperty(propertyId);
        property.setApproved(true);
        return propertyRepository.save(property);
    }

    @Transactional
    public Property rejectProperty(Long propertyId) {
        Property property = getProperty(propertyId);
        property.setApproved(false);
        return propertyRepository.save(property);
    }
}
