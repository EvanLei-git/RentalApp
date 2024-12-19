package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Landlord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandlordRepository extends JpaRepository<Landlord, Long> {
}