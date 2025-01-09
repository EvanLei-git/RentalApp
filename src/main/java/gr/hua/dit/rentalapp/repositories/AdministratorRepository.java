package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Administrator;
import gr.hua.dit.rentalapp.entities.Landlord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByUsername(String username);

}