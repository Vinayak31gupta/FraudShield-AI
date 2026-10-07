package com.fraudshield.controller;

import com.fraudshield.dto.AdminStatsDTO;
import com.fraudshield.dto.TransactionResponseDTO;
import com.fraudshield.dto.UserDTO;
import com.fraudshield.entity.AuditLog;
import com.fraudshield.entity.TransactionStatus;
import com.fraudshield.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatsDTO> getStatistics() {
        AdminStatsDTO stats = adminService.getAdminStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponseDTO>> getTransactions(
            @RequestParam(required = false) TransactionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.searchAllTransactions(status, search, pageable));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(adminService.getRecentAuditLogs());
    }
}
