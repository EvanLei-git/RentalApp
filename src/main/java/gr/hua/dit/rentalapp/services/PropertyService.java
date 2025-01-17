package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.repositories.PropertyRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public List<Property> getAllProperties() {
        try {
            List<Property> properties = propertyRepository.findAllWithOwners();
            return properties;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching properties: " + e.getMessage());
        }
    }

    public Property getPropertyById(Long id) {
        try {
            Property property = propertyRepository.findById(id).orElse(null);
            return property;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error fetching property: " + e.getMessage());
        }
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
        existing.setCountry(updatedProperty.getCountry());
        existing.setCity(updatedProperty.getCity());
        existing.setPostalCode(updatedProperty.getPostalCode());
        existing.setDescription(updatedProperty.getDescription());
        existing.setSizeInSquareMeters(updatedProperty.getSizeInSquareMeters());
        existing.setHasParking(updatedProperty.isHasParking());
        existing.setAllowsPets(updatedProperty.isAllowsPets());
        existing.setHasGarden(updatedProperty.isHasGarden());
        existing.setHasBalcony(updatedProperty.isHasBalcony());
        existing.setRented(updatedProperty.isRented());
        propertyRepository.save(existing);
    }

    @Transactional
    public void deleteProperty(Long propertyId) {
        try {
            // Delete associated rental applications first
            entityManager.createQuery("DELETE FROM RentalApplication ra WHERE ra.property.propertyId = :propertyId")
                    .setParameter("propertyId", propertyId)
                    .executeUpdate();
            
            // Now delete the property
            Property property = propertyRepository.findById(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));
            
            propertyRepository.delete(property);
            
        } catch (Exception e) {
            throw new RuntimeException("Error deleting property: " + e.getMessage());
        }
    }
}
