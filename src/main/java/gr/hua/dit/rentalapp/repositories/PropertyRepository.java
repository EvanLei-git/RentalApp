package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Property;
import gr.hua.dit.rentalapp.enums.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByType(PropertyType type);
    List<Property> findByCity(String city);
    List<Property> findByCountry(String country);
    List<Property> findByRentAmountBetween(double minRent, double maxRent);
    List<Property> findByBedroomsGreaterThanEqual(int minBedrooms);
    List<Property> findByBathroomsGreaterThanEqual(int minBathrooms);

    @Query("SELECT p FROM Property p LEFT JOIN FETCH p.owner")
    List<Property> findAllWithOwners();
}