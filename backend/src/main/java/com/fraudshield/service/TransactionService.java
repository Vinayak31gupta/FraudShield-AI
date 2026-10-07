package com.fraudshield.service;

import com.fraudshield.dto.*;
import com.fraudshield.entity.*;
import com.fraudshield.exception.BadRequestException;
import com.fraudshield.exception.ResourceNotFoundException;
import com.fraudshield.repository.FraudAnalysisRepository;
import com.fraudshield.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final AuditLogService auditLogService;

    public TransactionService(TransactionRepository transactionRepository,
                              FraudAnalysisRepository fraudAnalysisRepository,
                              AuditLogService auditLogService) {
        this.transactionRepository = transactionRepository;
        this.fraudAnalysisRepository = fraudAnalysisRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Transaction createTransaction(TransactionRequestDTO dto, User user, String clientIp) {
        Transaction transaction = new Transaction();

        String ref = (dto.getTransactionReference() != null && !dto.getTransactionReference().trim().isEmpty())
                ? dto.getTransactionReference().trim().toUpperCase()
                : generateReference();

        // Ensure reference is unique
        if (transactionRepository.findByTransactionReference(ref).isPresent()) {
            ref = generateReference();
        }

        transaction.setTransactionReference(ref);
        transaction.setUser(user);
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurrency() != null ? dto.getCurrency() : "USD");
        transaction.setTransactionType(dto.getTransactionType());
        transaction.setMerchantCategory(dto.getMerchantCategory());
        transaction.setLocation(dto.getLocation());
        transaction.setUsualLocation(dto.getUsualLocation());
        transaction.setDeviceType(dto.getDeviceType());
        transaction.setNewDevice(dto.isNewDevice());
        transaction.setIpAddress(dto.getIpAddress() != null ? dto.getIpAddress() : clientIp);
        transaction.setFailedAttempts(dto.getFailedAttempts());
        transaction.setAccountAgeDays(dto.getAccountAgeDays());
        transaction.setTransactionFrequency(dto.getTransactionFrequency());
        transaction.setStatus(TransactionStatus.APPROVED); // Initial baseline status before analysis

        Transaction saved = transactionRepository.save(transaction);

        auditLogService.log(
                user.getId(),
                user.getEmail(),
                "TRANSACTION_CREATED",
                "TRANSACTION",
                clientIp,
                "Transaction initiated: " + saved.getTransactionReference() + " (" + saved.getAmount() + " " + saved.getCurrency() + ")"
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getUserTransactions(User user, Pageable pageable) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Transaction getTransactionEntityById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransactionById(Long id, User currentUser) {
        Transaction transaction = getTransactionEntityById(id);

        if (currentUser.getRole() != Role.ROLE_ADMIN && !transaction.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Access denied: You are not authorized to view this transaction.");
        }

        return mapToResponseDTO(transaction);
    }

    @Transactional(readOnly = true)
    public UserHistoryDTO getUserHistory(User user) {
        Double avg = transactionRepository.getAverageTransactionAmountByUser(user);
        BigDecimal averageAmount = avg != null ? BigDecimal.valueOf(avg) : BigDecimal.valueOf(150.0);

        List<Transaction> pastTxns = transactionRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        Set<String> locations = pastTxns.stream()
                .map(Transaction::getLocation)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> devices = pastTxns.stream()
                .map(Transaction::getDeviceType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        long count = transactionRepository.countByUser(user);

        return new UserHistoryDTO(averageAmount, count, locations, devices, 90);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getUserDashboardStats(User user) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        long total = transactionRepository.countByUser(user);
        long safe = fraudAnalysisRepository.countByUserAndRiskLevel(user, RiskLevel.LOW);
        long medium = fraudAnalysisRepository.countByUserAndRiskLevel(user, RiskLevel.MEDIUM);
        long high = fraudAnalysisRepository.countByUserAndRiskLevel(user, RiskLevel.HIGH);

        Double avgScore = fraudAnalysisRepository.getUserAverageRiskScore(user);

        stats.setTotalTransactions(total);
        stats.setSafeTransactions(safe);
        stats.setMediumRiskTransactions(medium);
        stats.setHighRiskTransactions(high);
        stats.setAverageRiskScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);

        List<Transaction> recentList = transactionRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        stats.setRecentTransactions(recentList.stream().map(this::mapToResponseDTO).collect(Collectors.toList()));

        BigDecimal totalSum = recentList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTotalAmountAnalyzed(totalSum.doubleValue());

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("LOW", safe);
        distribution.put("MEDIUM", medium);
        distribution.put("HIGH", high);
        stats.setRiskDistribution(distribution);

        // Recent 7-day risk score trend
        Map<String, Double> trends = new LinkedHashMap<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd");
        for (int i = 6; i >= 0; i--) {
            LocalDateTime day = LocalDateTime.now().minusDays(i);
            String dayLabel = day.format(dtf);
            trends.put(dayLabel, 0.0);
        }
        for (Transaction t : recentList) {
            if (t.getFraudAnalysis() != null) {
                String dayKey = t.getCreatedAt().format(dtf);
                trends.put(dayKey, (double) t.getFraudAnalysis().getRiskScore());
            }
        }
        stats.setMonthlyTrends(trends);

        return stats;
    }

    public TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setTransactionReference(transaction.getTransactionReference());
        dto.setUserId(transaction.getUser().getId());
        dto.setUserEmail(transaction.getUser().getEmail());
        dto.setUserFullName(transaction.getUser().getFullName());
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setMerchantCategory(transaction.getMerchantCategory());
        dto.setLocation(transaction.getLocation());
        dto.setUsualLocation(transaction.getUsualLocation());
        dto.setDeviceType(transaction.getDeviceType());
        dto.setNewDevice(transaction.isNewDevice());
        dto.setIpAddress(transaction.getIpAddress());
        dto.setFailedAttempts(transaction.getFailedAttempts());
        dto.setAccountAgeDays(transaction.getAccountAgeDays());
        dto.setTransactionFrequency(transaction.getTransactionFrequency());
        dto.setStatus(transaction.getStatus().name());
        dto.setCreatedAt(transaction.getCreatedAt());

        if (transaction.getFraudAnalysis() != null) {
            FraudAnalysis fa = transaction.getFraudAnalysis();
            dto.setRiskScore(fa.getRiskScore());
            dto.setRiskLevel(fa.getRiskLevel().name());
            dto.setFraudAnalysis(mapToAnalysisDTO(fa));
        }

        return dto;
    }

    public FraudAnalysisResponseDTO mapToAnalysisDTO(FraudAnalysis fa) {
        FraudAnalysisResponseDTO dto = new FraudAnalysisResponseDTO();
        dto.setId(fa.getId());
        dto.setTransactionId(fa.getTransaction().getId());
        dto.setTransactionReference(fa.getTransaction().getTransactionReference());
        dto.setRiskScore(fa.getRiskScore());
        dto.setRiskLevel(fa.getRiskLevel().name());
        dto.setFraudProbability(fa.getFraudProbability());
        dto.setStatus(fa.getTransaction().getStatus().name());
        dto.setRecommendation(fa.getRecommendation());
        dto.setAiExplanation(fa.getAiExplanation());
        dto.setEngineType(fa.getEngineType());
        dto.setAnalyzedAt(fa.getAnalyzedAt());

        if (fa.getRiskFactors() != null) {
            List<RiskFactorDTO> factors = fa.getRiskFactors().stream()
                    .map(rf -> new RiskFactorDTO(
                            rf.getFactorCode(),
                            rf.getDescription(),
                            rf.getSeverity().name(),
                            rf.getWeightContribution()))
                    .collect(Collectors.toList());
            dto.setRiskFactors(factors);
        }

        return dto;
    }

    private String generateReference() {
        return "TXN-" + System.currentTimeMillis() % 10000000 + "-" + String.format("%04d", new Random().nextInt(10000));
    }
}
