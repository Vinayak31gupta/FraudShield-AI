package com.fraudshield.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fraud_analysis", indexes = {
    @Index(name = "idx_analysis_score", columnList = "risk_score"),
    @Index(name = "idx_analysis_level", columnList = "risk_level")
})
public class FraudAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(name = "fraud_probability", nullable = false)
    private double fraudProbability;

    @Column(nullable = false, length = 255)
    private String recommendation;

    @Lob
    @Column(name = "ai_explanation", columnDefinition = "TEXT")
    private String aiExplanation;

    @Column(name = "engine_type", nullable = false, length = 50)
    private String engineType = "RULE_BASED_V1";

    @OneToMany(mappedBy = "fraudAnalysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<RiskFactor> riskFactors = new ArrayList<>();

    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;

    public FraudAnalysis() {}

    @PrePersist
    protected void onCreate() {
        if (this.analyzedAt == null) {
            this.analyzedAt = LocalDateTime.now();
        }
    }

    public void addRiskFactor(RiskFactor factor) {
        riskFactors.add(factor);
        factor.setFraudAnalysis(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
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

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
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

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}
