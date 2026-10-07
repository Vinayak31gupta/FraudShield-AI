package com.fraudshield.controller;

import com.fraudshield.dto.FraudAnalysisResponseDTO;
import com.fraudshield.entity.User;
import com.fraudshield.service.AuthService;
import com.fraudshield.service.FraudAnalysisService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fraud")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FraudController {

    private final FraudAnalysisService fraudAnalysisService;
    private final AuthService authService;

    public FraudController(FraudAnalysisService fraudAnalysisService, AuthService authService) {
        this.fraudAnalysisService = fraudAnalysisService;
        this.authService = authService;
    }

    @PostMapping("/analyze/{transactionId}")
    public ResponseEntity<FraudAnalysisResponseDTO> analyzeTransaction(
            @PathVariable Long transactionId,
            HttpServletRequest request) {

        User currentUser = authService.getCurrentAuthenticatedUser();
        String clientIp = request.getRemoteAddr();

        FraudAnalysisResponseDTO response = fraudAnalysisService.analyzeTransaction(transactionId, currentUser, clientIp);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analysis/{transactionId}")
    public ResponseEntity<FraudAnalysisResponseDTO> getAnalysisByTransactionId(@PathVariable Long transactionId) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        FraudAnalysisResponseDTO response = fraudAnalysisService.getAnalysisByTransactionId(transactionId, currentUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<FraudAnalysisResponseDTO>> getHighRiskFeed() {
        return ResponseEntity.ok(fraudAnalysisService.getHighRiskFeed());
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFraudStatistics() {
        return ResponseEntity.ok(fraudAnalysisService.getGlobalFraudStatistics());
    }
}
