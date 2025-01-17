package gr.hua.dit.rentalapp.repositories;

import gr.hua.dit.rentalapp.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
