package com.fraudshield.service.ai;

import com.fraudshield.entity.RiskFactor;
import com.fraudshield.entity.RiskLevel;
import com.fraudshield.entity.Transaction;
import com.fraudshield.service.fraud.FraudScoreResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleBasedExplanationService implements AiExplanationService {

    @Override
    public String generateExplanation(Transaction transaction, FraudScoreResult scoreResult) {
        if (scoreResult == null) {
            return "Transaction evaluated under default security baseline.";
        }

        int score = scoreResult.getRiskScore();
        RiskLevel level = scoreResult.getRiskLevel();
        List<RiskFactor> factors = scoreResult.getRiskFactors();

        if (level == RiskLevel.LOW || factors == null || factors.isEmpty()) {
            return String.format(
                    "This transaction is classified as low risk (%d/100). All transactional parameters align with standard operational baselines, originating from expected device infrastructure with zero authentication flags.",
                    score
            );
        }

        List<String> factorDescriptions = factors.stream()
                .map(f -> f.getDescription().toLowerCase())
                .collect(Collectors.toList());

        String factorSummary;
        if (factorDescriptions.size() == 1) {
            factorSummary = factorDescriptions.get(0);
        } else if (factorDescriptions.size() == 2) {
            factorSummary = factorDescriptions.get(0) + " combined with " + factorDescriptions.get(1);
        } else {
            String firstPart = String.join(", ", factorDescriptions.subList(0, factorDescriptions.size() - 1));
            factorSummary = firstPart + ", and " + factorDescriptions.get(factorDescriptions.size() - 1);
        }

        if (level == RiskLevel.HIGH) {
            return String.format(
                    "This transaction has been classified as high risk (%d/100) due to multiple high-severity behavioral anomalies: %s. The divergence from established user behavior indicates potential account takeover or credential compromise.",
                    score, factorSummary
            );
        } else {
            return String.format(
                    "This transaction is classified as medium risk (%d/100) owing to noticeable variance from regular account patterns: %s. Elevated verification is recommended prior to fund settlement.",
                    score, factorSummary
            );
        }
    }
}
