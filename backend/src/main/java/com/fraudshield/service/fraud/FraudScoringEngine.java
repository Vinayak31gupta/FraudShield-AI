package com.fraudshield.service.fraud;

import com.fraudshield.dto.UserHistoryDTO;
import com.fraudshield.entity.Transaction;

/**
 * Modular interface for fraud scoring engines.
 * Allows seamless hot-swapping between rule-based heuristics and machine learning models
 * (such as Random Forest, XGBoost, or Logistic Regression).
 */
public interface FraudScoringEngine {

    FraudScoreResult evaluate(Transaction transaction, UserHistoryDTO userHistory);

    String getEngineIdentifier();
}
