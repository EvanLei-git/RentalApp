package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    //  findByEmail(String email)
}
