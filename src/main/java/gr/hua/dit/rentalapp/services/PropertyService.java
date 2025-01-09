package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    public Property getPropertyById(Long id) {
        return propertyRepository.findById(id).orElse(null);
    }

    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    public void updateProperty(Long id, Property updatedProperty) {
        Property existing = propertyRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("Property not found: " + id);
        }
        existing.setAddress(updatedProperty.getAddress());
        existing.setType(updatedProperty.getType());
        existing.setRentAmount(updatedProperty.getRentAmount());
        existing.setBedrooms(updatedProperty.getBedrooms());
        existing.setBathrooms(updatedProperty.getBathrooms());
        existing.setApproved(updatedProperty.isApproved());
        existing.setCountry(updatedProperty.getCountry());
        existing.setCity(updatedProperty.getCity());
        existing.setPostalCode(updatedProperty.getPostalCode());
        existing.setDescription(updatedProperty.getDescription());
        existing.setSizeInSquareMeters(updatedProperty.getSizeInSquareMeters());
        existing.setHasParking(updatedProperty.isHasParking());
        existing.setAllowsPets(updatedProperty.isAllowsPets());
        existing.setHasGarden(updatedProperty.isHasGarden());
        existing.setHasBalcony(updatedProperty.isHasBalcony());
        propertyRepository.save(existing);
    }

    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);
    }
}
