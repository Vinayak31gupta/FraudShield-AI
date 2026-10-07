package com.fraudshield.service;

import com.fraudshield.dto.FraudAnalysisResponseDTO;
import com.fraudshield.dto.UserHistoryDTO;
import com.fraudshield.entity.*;
import com.fraudshield.exception.BadRequestException;
import com.fraudshield.exception.ResourceNotFoundException;
import com.fraudshield.repository.FraudAnalysisRepository;
import com.fraudshield.repository.TransactionRepository;
import com.fraudshield.service.ai.AiExplanationService;
import com.fraudshield.service.fraud.FraudScoreResult;
import com.fraudshield.service.fraud.FraudScoringEngine;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FraudAnalysisService {

    private final TransactionRepository transactionRepository;
    private final FraudAnalysisRepository fraudAnalysisRepository;
    private final FraudScoringEngine fraudScoringEngine;
    private final AiExplanationService aiExplanationService;
    private final TransactionService transactionService;
    private final AuditLogService auditLogService;

    public FraudAnalysisService(TransactionRepository transactionRepository,
                                FraudAnalysisRepository fraudAnalysisRepository,
                                FraudScoringEngine fraudScoringEngine,
                                AiExplanationService aiExplanationService,
                                TransactionService transactionService,
                                AuditLogService auditLogService) {
        this.transactionRepository = transactionRepository;
        this.fraudAnalysisRepository = fraudAnalysisRepository;
        this.fraudScoringEngine = fraudScoringEngine;
        this.aiExplanationService = aiExplanationService;
        this.transactionService = transactionService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public FraudAnalysisResponseDTO analyzeTransaction(Long transactionId, User currentUser, String clientIp) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + transactionId));

        // Authorization check: User must own transaction or be an Admin
        if (currentUser.getRole() != Role.ROLE_ADMIN && !transaction.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Access denied: You cannot analyze transactions belonging to other users.");
        }

        // 1. Gather behavioral baseline history
        UserHistoryDTO history = transactionService.getUserHistory(transaction.getUser());

        // 2. Perform Fraud Risk Scoring
        FraudScoreResult scoreResult = fraudScoringEngine.evaluate(transaction, history);

        // 3. Generate Natural Language AI Explanation
        String aiExplanation = aiExplanationService.generateExplanation(transaction, scoreResult);

        // 4. Persist or Update Fraud Analysis Record
        FraudAnalysis analysis = transaction.getFraudAnalysis();
        if (analysis == null) {
            analysis = new FraudAnalysis();
            analysis.setTransaction(transaction);
        } else {
            analysis.getRiskFactors().clear();
        }

        analysis.setRiskScore(scoreResult.getRiskScore());
        analysis.setRiskLevel(scoreResult.getRiskLevel());
        analysis.setFraudProbability(scoreResult.getFraudProbability());
        analysis.setRecommendation(scoreResult.getRecommendation());
        analysis.setAiExplanation(aiExplanation);
        analysis.setEngineType(scoreResult.getEngineType());
        analysis.setAnalyzedAt(LocalDateTime.now());

        // Attach new risk factors
        for (RiskFactor rf : scoreResult.getRiskFactors()) {
            analysis.addRiskFactor(rf);
        }

        // Update transaction status
        transaction.setStatus(scoreResult.getSuggestedStatus());
        transaction.setFraudAnalysis(analysis);

        FraudAnalysis saved = fraudAnalysisRepository.save(analysis);
        transactionRepository.save(transaction);

        // Record security audit
        auditLogService.log(
                currentUser.getId(),
                currentUser.getEmail(),
                "FRAUD_ANALYSIS_PERFORMED",
                "FRAUD_ENGINE",
                clientIp,
                String.format("Evaluated txn %s: Score %d/100, Level %s, Status %s",
                        transaction.getTransactionReference(),
                        saved.getRiskScore(),
                        saved.getRiskLevel(),
                        transaction.getStatus())
        );

        return transactionService.mapToAnalysisDTO(saved);
    }

    @Transactional(readOnly = true)
    public FraudAnalysisResponseDTO getAnalysisByTransactionId(Long transactionId, User currentUser) {
        FraudAnalysis analysis = fraudAnalysisRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("No fraud analysis record found for transaction ID: " + transactionId));

        if (currentUser.getRole() != Role.ROLE_ADMIN && !analysis.getTransaction().getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Access denied: You are not authorized to view this analysis.");
        }

        return transactionService.mapToAnalysisDTO(analysis);
    }

    @Transactional(readOnly = true)
    public List<FraudAnalysisResponseDTO> getHighRiskFeed() {
        return fraudAnalysisRepository.findTopHighRisk(PageRequest.of(0, 10)).stream()
                .map(transactionService::mapToAnalysisDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getGlobalFraudStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long total = transactionRepository.count();
        long low = fraudAnalysisRepository.countByRiskLevel(RiskLevel.LOW);
        long medium = fraudAnalysisRepository.countByRiskLevel(RiskLevel.MEDIUM);
        long high = fraudAnalysisRepository.countByRiskLevel(RiskLevel.HIGH);
        Double avg = fraudAnalysisRepository.getGlobalAverageRiskScore();

        stats.put("totalTransactions", total);
        stats.put("lowRiskCount", low);
        stats.put("mediumRiskCount", medium);
        stats.put("highRiskCount", high);
        stats.put("averageRiskScore", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        stats.put("fraudDetectionRate", total > 0 ? Math.round(((double) high / total * 100.0) * 10.0) / 10.0 : 0.0);

        return stats;
    }
}
