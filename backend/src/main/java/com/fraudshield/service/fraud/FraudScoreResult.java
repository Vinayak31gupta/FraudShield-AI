package com.fraudshield.service.fraud;

import com.fraudshield.entity.RiskFactor;
import com.fraudshield.entity.RiskLevel;
import com.fraudshield.entity.TransactionStatus;

import java.util.ArrayList;
import java.util.List;

public class FraudScoreResult {

    private int riskScore;
    private RiskLevel riskLevel;
    private double fraudProbability;
    private TransactionStatus suggestedStatus;
    private String recommendation;
    private String engineType;
    private List<RiskFactor> riskFactors = new ArrayList<>();

    public FraudScoreResult() {}

    public FraudScoreResult(int riskScore, RiskLevel riskLevel, double fraudProbability,
                            TransactionStatus suggestedStatus, String recommendation, String engineType) {
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.fraudProbability = fraudProbability;
        this.suggestedStatus = suggestedStatus;
        this.recommendation = recommendation;
        this.engineType = engineType;
    }

    public void addRiskFactor(RiskFactor factor) {
        this.riskFactors.add(factor);
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getFraudProbability() {
        return fraudProbability;
    }

    public void setFraudProbability(double fraudProbability) {
        this.fraudProbability = fraudProbability;
    }

    public TransactionStatus getSuggestedStatus() {
        return suggestedStatus;
    }

    public void setSuggestedStatus(TransactionStatus suggestedStatus) {
        this.suggestedStatus = suggestedStatus;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public List<RiskFactor> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<RiskFactor> riskFactors) {
        this.riskFactors = riskFactors;
    }
}
