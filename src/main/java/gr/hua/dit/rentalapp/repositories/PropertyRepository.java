package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByAddressContaining(String address);
    List<Property> findByType(String type);
}