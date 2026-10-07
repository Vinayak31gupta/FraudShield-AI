package com.fraudshield.service.fraud;

import com.fraudshield.dto.UserHistoryDTO;
import com.fraudshield.entity.RiskFactor;
import com.fraudshield.entity.RiskLevel;
import com.fraudshield.entity.Transaction;
import com.fraudshield.entity.TransactionStatus;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Primary
public class RuleBasedScoringEngine implements FraudScoringEngine {

    private static final Set<String> HIGH_RISK_MERCHANTS = Set.of(
            "CRYPTO_EXCHANGE", "GAMBLING", "LUXURY_WATCHES", "HIGH_VALUE_JEWELRY", "CASINO", "OFFSHORE_BETTING"
    );

    private static final Set<String> MEDIUM_RISK_MERCHANTS = Set.of(
            "ELECTRONICS", "WIRE_TRANSFER", "GIFT_CARDS", "FOREIGN_ATM", "CURRENCY_EXCHANGE"
    );

    @Override
    public String getEngineIdentifier() {
        return "RULE_BASED_ENGINE_V1";
    }

    @Override
    public FraudScoreResult evaluate(Transaction transaction, UserHistoryDTO userHistory) {
        int totalScore = 0;
        List<RiskFactor> factors = new ArrayList<>();

        // 1. Transaction Amount Anomaly
        BigDecimal amount = transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO;
        BigDecimal avgAmount = (userHistory != null && userHistory.getAverageAmount() != null &&
                userHistory.getAverageAmount().compareTo(BigDecimal.ZERO) > 0)
                ? userHistory.getAverageAmount()
                : new BigDecimal("150.00");

        if (amount.compareTo(new BigDecimal("10000.00")) >= 0) {
            int weight = 30;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "EXTREME_AMOUNT",
                    "Transaction amount exceeds high-value threshold ($10,000)",
                    RiskLevel.HIGH,
                    weight
            ));
        } else if (amount.compareTo(avgAmount.multiply(new BigDecimal("4.0"))) >= 0 || amount.compareTo(new BigDecimal("5000.00")) >= 0) {
            int weight = 24;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "AMOUNT_DEVIATION",
                    "Transaction amount significantly exceeds user's normal spending baseline",
                    RiskLevel.HIGH,
                    weight
            ));
        } else if (amount.compareTo(avgAmount.multiply(new BigDecimal("2.5"))) >= 0) {
            int weight = 12;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "MODERATE_AMOUNT_SPIKE",
                    "Transaction amount is notably higher than historical average",
                    RiskLevel.MEDIUM,
                    weight
            ));
        }

        // 2. Unrecognized or New Device
        if (transaction.isNewDevice()) {
            int weight = 20;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "UNKNOWN_DEVICE",
                    "Transaction initiated from an unrecognized or newly registered device",
                    RiskLevel.HIGH,
                    weight
            ));
        }

        // 3. Location Anomaly / Geographical Mismatch
        String txnLoc = transaction.getLocation() != null ? transaction.getLocation().trim() : "";
        String usualLoc = transaction.getUsualLocation() != null ? transaction.getUsualLocation().trim() : "";

        if (!txnLoc.isEmpty() && !usualLoc.isEmpty() && !txnLoc.equalsIgnoreCase(usualLoc)) {
            int weight = 22;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "LOCATION_ANOMALY",
                    "Transaction location (" + txnLoc + ") differs from habitual user location (" + usualLoc + ")",
                    RiskLevel.HIGH,
                    weight
            ));
        }

        // 4. Failed Authentication Attempts
        int failedAttempts = transaction.getFailedAttempts();
        if (failedAttempts >= 3) {
            int weight = 22;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "MULTIPLE_FAILED_ATTEMPTS",
                    "Multiple consecutive failed authentication attempts (" + failedAttempts + ") preceding transaction",
                    RiskLevel.HIGH,
                    weight
            ));
        } else if (failedAttempts > 0) {
            int weight = 10;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "FAILED_ATTEMPT_PENALTY",
                    "Prior failed authentication attempt detected (" + failedAttempts + ")",
                    RiskLevel.MEDIUM,
                    weight
            ));
        }

        // 5. Transaction Frequency & Rapid Velocity
        int frequency = transaction.getTransactionFrequency();
        if (frequency >= 5) {
            int weight = 18;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "RAPID_VELOCITY_BURST",
                    "High frequency burst: " + frequency + " transactions initiated within short time window",
                    RiskLevel.HIGH,
                    weight
            ));
        } else if (frequency >= 3) {
            int weight = 10;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "ELEVATED_FREQUENCY",
                    "Elevated transaction frequency (" + frequency + " within active period)",
                    RiskLevel.MEDIUM,
                    weight
            ));
        }

        // 6. High-Risk Merchant Category
        String category = transaction.getMerchantCategory() != null ? transaction.getMerchantCategory().toUpperCase().trim() : "";
        if (HIGH_RISK_MERCHANTS.contains(category)) {
            int weight = 16;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "HIGH_RISK_MERCHANT",
                    "Merchant category '" + category + "' is classified in high-risk fraud profile",
                    RiskLevel.HIGH,
                    weight
            ));
        } else if (MEDIUM_RISK_MERCHANTS.contains(category)) {
            int weight = 8;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "MEDIUM_RISK_MERCHANT",
                    "Merchant category '" + category + "' exhibits higher than average dispute rates",
                    RiskLevel.MEDIUM,
                    weight
            ));
        }

        // 7. Account Age Factor
        int accountAgeDays = transaction.getAccountAgeDays();
        if (accountAgeDays < 7 && amount.compareTo(new BigDecimal("1000.00")) >= 0) {
            int weight = 15;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "NEW_ACCOUNT_HIGH_VALUE",
                    "Brand new account (age " + accountAgeDays + " days) executing high-value transaction",
                    RiskLevel.HIGH,
                    weight
            ));
        } else if (accountAgeDays < 30 && amount.compareTo(new BigDecimal("2500.00")) >= 0) {
            int weight = 8;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "YOUNG_ACCOUNT_RISK",
                    "Relatively young account profile with limited behavioral baseline",
                    RiskLevel.LOW,
                    weight
            ));
        }

        // 8. Unusual Time of Day (e.g. 2:00 AM - 5:00 AM local time)
        LocalDateTime txnTime = transaction.getCreatedAt() != null ? transaction.getCreatedAt() : LocalDateTime.now();
        int hour = txnTime.getHour();
        if (hour >= 2 && hour <= 4) {
            int weight = 7;
            totalScore += weight;
            factors.add(new RiskFactor(
                    "UNUSUAL_TRANSACTION_HOURS",
                    "Transaction initiated during high-risk late night window (" + String.format("%02d:00", hour) + ")",
                    RiskLevel.LOW,
                    weight
            ));
        }

        // Clamp final score to 0–100
        int finalScore = Math.min(100, Math.max(0, totalScore));
        double probability = finalScore; // Scaled probability percentage

        // Classify Risk Level and Decision Status
        RiskLevel riskLevel;
        TransactionStatus status;
        String recommendation;

        if (finalScore <= 30) {
            riskLevel = RiskLevel.LOW;
            status = TransactionStatus.APPROVED;
            recommendation = "Low risk detected. Transaction approved under standard verification protocols.";
        } else if (finalScore <= 70) {
            riskLevel = RiskLevel.MEDIUM;
            status = TransactionStatus.FLAGGED_FOR_REVIEW;
            recommendation = "Moderate risk indicators detected. Require additional step-up verification (OTP/2FA) before fulfillment.";
        } else {
            riskLevel = RiskLevel.HIGH;
            status = TransactionStatus.BLOCKED;
            recommendation = "Critical risk indicators detected. Block transaction immediately and mandate biometric or customer service verification.";
        }

        FraudScoreResult result = new FraudScoreResult(
                finalScore,
                riskLevel,
                probability,
                status,
                recommendation,
                getEngineIdentifier()
        );
        result.setRiskFactors(factors);

        return result;
    }
}
