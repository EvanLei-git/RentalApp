package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Report;
import gr.hua.dit.rentalapp.entities.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByTenant(Tenant tenant);
    List<Report> findByResolvedOrderByCreatedAtDesc(boolean resolved);
}
