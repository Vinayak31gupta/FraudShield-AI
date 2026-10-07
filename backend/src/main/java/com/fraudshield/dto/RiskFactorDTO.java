package com.fraudshield.dto;

public class RiskFactorDTO {

    private String factorCode;
    private String description;
    private String severity;
    private int weightContribution;

    public RiskFactorDTO() {}

    public RiskFactorDTO(String factorCode, String description, String severity, int weightContribution) {
        this.factorCode = factorCode;
        this.description = description;
        this.severity = severity;
        this.weightContribution = weightContribution;
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

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public int getWeightContribution() {
        return weightContribution;
    }

    public void setWeightContribution(int weightContribution) {
        this.weightContribution = weightContribution;
    }
}
