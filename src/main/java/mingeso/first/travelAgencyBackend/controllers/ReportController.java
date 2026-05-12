package mingeso.first.travelAgencyBackend.controllers;

import mingeso.first.travelAgencyBackend.dto.ReportResponseDTO;
import mingeso.first.travelAgencyBackend.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/dashboard")
    public ResponseEntity<ReportResponseDTO> getAdminDashboardStats() {
        return ResponseEntity.ok(reportService.getAdminDashboardStats());
    }
}