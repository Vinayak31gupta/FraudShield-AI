package com.fraudshield.service.ai;

import com.fraudshield.entity.Transaction;
import com.fraudshield.service.fraud.FraudScoreResult;

public interface AiExplanationService {

    /**
     * Generates a natural language explanation articulating why a transaction
     * was classified at its specific risk score and level.
     *
     * @param transaction The financial transaction being evaluated.
     * @param scoreResult The computed fraud scoring result containing risk score and factors.
     * @return A human-readable, professional explanation for security analysts and customers.
     */
    String generateExplanation(Transaction transaction, FraudScoreResult scoreResult);
}
