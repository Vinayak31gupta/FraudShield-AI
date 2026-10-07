package com.fraudshield.service;

import com.fraudshield.dto.*;
import com.fraudshield.entity.*;
import com.fraudshield.repository.AuditLogRepository;
import com.fraudshield.repository.FraudAnalysisRepository;
import com.fraudshield.repository.TransactionRepository;
import com.fraudshield.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransactionService transactionService;

    public AdminService(UserRepository userRepository,
                        TransactionRepository transactionRepository,
                        FraudAnalysisRepository fraudAnalysisRepository,
                        AuditLogRepository auditLogRepository,
                        TransactionService transactionService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.fraudAnalysisRepository = fraudAnalysisRepository;
        this.auditLogRepository = auditLogRepository;
        this.transactionService = transactionService;
    }

    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStatistics() {
        AdminStatsDTO stats = new AdminStatsDTO();

        long totalUsers = userRepository.count();
        long totalTxns = transactionRepository.count();
        long safe = fraudAnalysisRepository.countByRiskLevel(RiskLevel.LOW);
        long medium = fraudAnalysisRepository.countByRiskLevel(RiskLevel.MEDIUM);
        long high = fraudAnalysisRepository.countByRiskLevel(RiskLevel.HIGH);

        Double avgScore = fraudAnalysisRepository.getGlobalAverageRiskScore();

        stats.setTotalUsers(totalUsers);
        stats.setTotalTransactions(totalTxns);
        stats.setSafeTransactions(safe);
        stats.setSuspiciousTransactions(medium);
        stats.setHighRiskTransactions(high);
        stats.setAverageRiskScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);

        double rate = totalTxns > 0 ? ((double) high / totalTxns) * 100.0 : 0.0;
        stats.setFraudDetectionRate(Math.round(rate * 10.0) / 10.0);

        List<Transaction> recent = transactionRepository.findTop10ByOrderByCreatedAtDesc();
        stats.setRecentTransactions(recent.stream().map(transactionService::mapToResponseDTO).collect(Collectors.toList()));

        List<FraudAnalysis> highAlerts = fraudAnalysisRepository.findTopHighRisk(PageRequest.of(0, 5));
        stats.setHighRiskAlerts(highAlerts.stream().map(transactionService::mapToAnalysisDTO).collect(Collectors.toList()));

        // Category breakdown
        Map<String, Long> categories = new LinkedHashMap<>();
        for (Transaction t : recent) {
            String cat = t.getMerchantCategory();
            categories.put(cat, categories.getOrDefault(cat, 0L) + 1);
        }
        stats.setCategoryBreakdown(categories);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getEmail(),
                        u.getFullName(),
                        u.getRole(),
                        u.isEnabled(),
                        transactionRepository.countByUser(u),
                        u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> searchAllTransactions(TransactionStatus status, String search, Pageable pageable) {
        return transactionRepository.searchTransactions(null, status, search, pageable)
                .map(transactionService::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop20ByOrderByTimestampDesc();
    }
}
