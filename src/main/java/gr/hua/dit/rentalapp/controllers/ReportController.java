package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Report;
import gr.hua.dit.rentalapp.entities.Tenant;
import gr.hua.dit.rentalapp.repositories.TenantRepository;
import gr.hua.dit.rentalapp.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;
    private final TenantRepository tenantRepository;

    @Autowired
    public ReportController(ReportService reportService, TenantRepository tenantRepository) {
        this.reportService = reportService;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<?> submitReport(@RequestBody Map<String, String> reportData, Authentication authentication) {
        try {
            String username = authentication.getName();
            Tenant tenant = tenantRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Tenant not found"));

            Report report = reportService.createReport(
                    reportData.get("title"),
                    reportData.get("description"),
                    tenant
            );
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting report: " + e.getMessage());
        }
    }

    @GetMapping("/tenant")
    @ResponseBody
    public ResponseEntity<List<Report>> getTenantReports(Authentication authentication) {
        String username = authentication.getName();
        Tenant tenant = tenantRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return ResponseEntity.ok(reportService.getReportsByTenant(tenant));
    }

    @GetMapping("/unresolved")
    @ResponseBody
    public ResponseEntity<List<Report>> getUnresolvedReports() {
        return ResponseEntity.ok(reportService.getAllUnresolvedReports());
    }

    @PostMapping("/{reportId}/resolve")
    @ResponseBody
    public ResponseEntity<?> resolveReport(@PathVariable Long reportId) {
        try {
            Report report = reportService.resolveReport(reportId);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error resolving report: " + e.getMessage());
        }
    }
}
