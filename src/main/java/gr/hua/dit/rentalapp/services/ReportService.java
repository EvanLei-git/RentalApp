package gr.hua.dit.rentalapp.services;

import gr.hua.dit.rentalapp.entities.Report;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {
    private final ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report createReport(String title, String description, Tenant tenant) {
        Report report = new Report();
        report.setTitle(title);
        report.setDescription(description);
        report.setTenant(tenant);
        return reportRepository.save(report);
    }

    public List<Report> getReportsByTenant(Tenant tenant) {
        return reportRepository.findByTenant(tenant);
    }

    public List<Report> getAllUnresolvedReports() {
        return reportRepository.findByResolvedOrderByCreatedAtDesc(false);
    }

    public Report resolveReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setResolved(true);
        return reportRepository.save(report);
    }
}
