package com.fraudshield.service;

import com.fraudshield.dto.UserHistoryDTO;
import com.fraudshield.entity.RiskLevel;
import com.fraudshield.entity.Transaction;
import com.fraudshield.entity.TransactionStatus;
import com.fraudshield.service.fraud.FraudScoreResult;
import com.fraudshield.service.fraud.RuleBasedScoringEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RuleBasedScoringEngineTest {

    private RuleBasedScoringEngine engine;
    private UserHistoryDTO regularHistory;

    @BeforeEach
    void setUp() {
        engine = new RuleBasedScoringEngine();
        regularHistory = new UserHistoryDTO(
                new BigDecimal("150.00"),
                25,
                Set.of("San Francisco, USA"),
                Set.of("iPhone 15 Pro"),
                365
        );
    }

    @Test
    @DisplayName("Should evaluate normal transaction as LOW RISK / SAFE")
    void testLowRiskTransaction() {
        Transaction txn = new Transaction();
        txn.setAmount(new BigDecimal("45.00"));
        txn.setMerchantCategory("RETAIL");
        txn.setLocation("San Francisco, USA");
        txn.setUsualLocation("San Francisco, USA");
        txn.setDeviceType("iPhone 15 Pro");
        txn.setNewDevice(false);
        txn.setFailedAttempts(0);
        txn.setAccountAgeDays(365);
        txn.setTransactionFrequency(1);

        FraudScoreResult result = engine.evaluate(txn, regularHistory);

        assertNotNull(result);
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertTrue(result.getRiskScore() <= 30, "Score should be <= 30 for safe transactions");
        assertEquals(TransactionStatus.APPROVED, result.getSuggestedStatus());
        assertTrue(result.getRecommendation().contains("Low risk detected"));
    }

    @Test
    @DisplayName("Should evaluate moderate deviations as MEDIUM RISK")
    void testMediumRiskTransaction() {
        Transaction txn = new Transaction();
        txn.setAmount(new BigDecimal("450.00")); // ~3x average
        txn.setMerchantCategory("ELECTRONICS");
        txn.setLocation("Seattle, USA"); // Location anomaly
        txn.setUsualLocation("San Francisco, USA");
        txn.setDeviceType("iPhone 15 Pro");
        txn.setNewDevice(false);
        txn.setFailedAttempts(1);
        txn.setAccountAgeDays(120);
        txn.setTransactionFrequency(2);

        FraudScoreResult result = engine.evaluate(txn, regularHistory);

        assertNotNull(result);
        assertEquals(RiskLevel.MEDIUM, result.getRiskLevel());
        assertTrue(result.getRiskScore() >= 31 && result.getRiskScore() <= 70,
                "Score should be within [31, 70] for medium risk transactions");
        assertEquals(TransactionStatus.FLAGGED_FOR_REVIEW, result.getSuggestedStatus());
        assertFalse(result.getRiskFactors().isEmpty());
    }

    @Test
    @DisplayName("Should evaluate multiple severe anomalies as HIGH RISK / POTENTIAL FRAUD")
    void testHighRiskTransaction() {
        Transaction txn = new Transaction();
        txn.setAmount(new BigDecimal("8500.00")); // Extreme deviation
        txn.setMerchantCategory("CRYPTO_EXCHANGE"); // High risk merchant
        txn.setLocation("London, UK"); // Location mismatch
        txn.setUsualLocation("San Francisco, USA");
        txn.setDeviceType("Unknown Linux Terminal");
        txn.setNewDevice(true); // Unknown device
        txn.setFailedAttempts(4); // Multiple failed attempts
        txn.setAccountAgeDays(10);
        txn.setTransactionFrequency(7); // High frequency burst

        FraudScoreResult result = engine.evaluate(txn, regularHistory);

        assertNotNull(result);
        assertEquals(RiskLevel.HIGH, result.getRiskLevel());
        assertTrue(result.getRiskScore() >= 71, "Score should be >= 71 for high risk fraud");
        assertEquals(TransactionStatus.BLOCKED, result.getSuggestedStatus());
        assertTrue(result.getRiskFactors().size() >= 4, "Should detect multiple risk factors");
    }
}
