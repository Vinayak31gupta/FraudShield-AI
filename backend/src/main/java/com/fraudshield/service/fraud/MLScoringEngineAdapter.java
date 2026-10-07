package com.fraudshield.service.fraud;

import com.fraudshield.dto.UserHistoryDTO;
import com.fraudshield.entity.RiskFactor;
import com.fraudshield.entity.RiskLevel;
import com.fraudshield.entity.Transaction;
import com.fraudshield.entity.TransactionStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Architectural Adapter for Future Machine Learning Model Integration.
 * 
 * Future Models Supported:
 * - XGBoost Classifier (via ONNX runtime or PMML)
 * - Random Forest (via Python REST API / FastAPI microservice)
 * - Logistic Regression / Deep Neural Networks
 * 
 * This adapter implements {@link FraudScoringEngine}, ensuring that switching from
 * heuristic rules to a trained ML model requires zero changes to core business controllers or JPA entities.
 */
@Component
public class MLScoringEngineAdapter implements FraudScoringEngine {

    private final RuleBasedScoringEngine fallbackRuleEngine;

    public MLScoringEngineAdapter(RuleBasedScoringEngine fallbackRuleEngine) {
        this.fallbackRuleEngine = fallbackRuleEngine;
    }

    @Override
    public String getEngineIdentifier() {
        return "ML_MODEL_ADAPTER_XGBOOST";
    }

    @Override
    public FraudScoreResult evaluate(Transaction transaction, UserHistoryDTO userHistory) {
        // In production with an active ML model:
        // 1. Vectorize transaction attributes into feature array: [amount_scaled, location_dist, is_new_device, failed_attempts, hour_of_day, ...]
        // 2. Invoke ONNX Runtime session or REST call to model inference endpoint: float probability = model.predict_proba(features);
        // 3. Extract SHAP (SHapley Additive exPlanations) values to determine feature importance and build RiskFactors.
        
        // As a robust architecture pattern, we delegate to the rule engine while demonstrating how the ML contract is satisfied:
        FraudScoreResult baseResult = fallbackRuleEngine.evaluate(transaction, userHistory);
        baseResult.setEngineType(getEngineIdentifier());
        return baseResult;
    }
}
