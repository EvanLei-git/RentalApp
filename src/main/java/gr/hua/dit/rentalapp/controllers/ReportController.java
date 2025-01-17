package gr.hua.dit.rentalapp.controllers;

import gr.hua.dit.rentalapp.entities.Report;
import gr.hua.dit.rentalapp.entities.User;
import gr.hua.dit.rentalapp.entities.Role;
import gr.hua.dit.rentalapp.enums.RoleType;
import gr.hua.dit.rentalapp.repositories.ReportRepository;
import gr.hua.dit.rentalapp.services.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;
    
    @Autowired
    private UserAuthService userService;

    @GetMapping("/all")
    public ResponseEntity<List<Map<String, Object>>> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        List<Map<String, Object>> reportDTOs = reports.stream().map(report -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", report.getId());
            dto.put("title", report.getTitle());
            dto.put("description", report.getDescription());
            dto.put("createDate", report.getCreateDate());
            dto.put("resolved", report.isResolved());
            dto.put("userRole", report.getUserRole());
            
            Map<String, Object> userDto = new HashMap<>();
            userDto.put("id", report.getUser().getUserId());
            userDto.put("username", report.getUser().getUsername());
            dto.put("user", userDto);
            
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(reportDTOs);
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<?> getReportDetails(@PathVariable Long id) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", report.getId());
            dto.put("title", report.getTitle());
            dto.put("description", report.getDescription());
            dto.put("createDate", report.getCreateDate());
            dto.put("resolved", report.isResolved());
            dto.put("userRole", report.getUserRole());
            
            Map<String, Object> userDto = new HashMap<>();
            userDto.put("id", report.getUser().getUserId());
            userDto.put("username", report.getUser().getUsername());
            dto.put("user", userDto);
            
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReport(@PathVariable Long id) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", report.getId());
            dto.put("title", report.getTitle());
            dto.put("description", report.getDescription());
            dto.put("createDate", report.getCreateDate());
            dto.put("resolved", report.isResolved());
            dto.put("userRole", report.getUserRole());
            
            Map<String, Object> userDto = new HashMap<>();
            userDto.put("id", report.getUser().getUserId());
            userDto.put("username", report.getUser().getUsername());
            dto.put("user", userDto);
            
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReport(@PathVariable Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleReportStatus(@PathVariable Long id) {
        Optional<Report> reportOpt = reportRepository.findById(id);
        if (reportOpt.isPresent()) {
            Report report = reportOpt.get();
            report.setResolved(!report.isResolved());
            reportRepository.save(report);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/create")
    public ResponseEntity<?> createReport(@RequestBody Map<String, String> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "User must be authenticated"));
            }

            String username = authentication.getName();
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "User not found"));
            }
            User user = userOpt.get();

            String userRole = "UNKNOWN";
            for (Role role : user.getRoles()) {
                if (role.getName().equals(RoleType.ADMINISTRATOR)) {
                    userRole = "ADMINISTRATOR";
                    break;
                } else if (role.getName().equals(RoleType.LANDLORD)) {
                    userRole = "LANDLORD";
                    break;
                } else if (role.getName().equals(RoleType.TENANT)) {
                    userRole = "TENANT";
                    break;
                }
            }

            if (userRole.equals("UNKNOWN")) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "message", "Invalid user role"));
            }

            Report report = new Report();
            report.setTitle(payload.get("title"));
            report.setDescription(payload.get("description"));
            report.setUser(user);
            report.setUserRole(userRole);
            report.setCreateDate(new Date());
            report.setResolved(false);
            
            reportRepository.save(report);
            return ResponseEntity.ok()
                .body(Map.of("success", true, "message", "Report submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Failed to submit report: " + e.getMessage()));
        }
    }
}
