package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
}