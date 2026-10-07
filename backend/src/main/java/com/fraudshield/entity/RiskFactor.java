package com.fraudshield.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "risk_factors", indexes = {
    @Index(name = "idx_factor_analysis", columnList = "fraud_analysis_id")
})
public class RiskFactor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fraud_analysis_id", nullable = false)
    private FraudAnalysis fraudAnalysis;

    @Column(name = "factor_code", nullable = false, length = 60)
    private String factorCode;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel severity;

    @Column(name = "weight_contribution", nullable = false)
    private int weightContribution;

    public RiskFactor() {}

    public RiskFactor(String factorCode, String description, RiskLevel severity, int weightContribution) {
        this.factorCode = factorCode;
        this.description = description;
        this.severity = severity;
        this.weightContribution = weightContribution;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FraudAnalysis getFraudAnalysis() {
        return fraudAnalysis;
    }

    public void setFraudAnalysis(FraudAnalysis fraudAnalysis) {
        this.fraudAnalysis = fraudAnalysis;
    }

    public String getFactorCode() {
        return factorCode;
    }

    public void setFactorCode(String factorCode) {
        this.factorCode = factorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RiskLevel getSeverity() {
        return severity;
    }

    public void setSeverity(RiskLevel severity) {
        this.severity = severity;
    }

    public int getWeightContribution() {
        return weightContribution;
    }

    public void setWeightContribution(int weightContribution) {
        this.weightContribution = weightContribution;
    }
}
