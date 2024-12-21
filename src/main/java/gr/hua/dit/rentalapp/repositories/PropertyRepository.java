package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Landlord;
import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.enums.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByAddressContaining(String address);
    List<Property> findByType(PropertyType type);
    List<Property> findByIsApproved(boolean isApproved);
    List<Property> findByOwner(Landlord owner);
    List<Property> findByRentAmountBetween(double minRent, double maxRent);
    List<Property> findByBedroomsGreaterThanEqual(int minBedrooms);
    List<Property> findByBathroomsGreaterThanEqual(int minBathrooms);
}