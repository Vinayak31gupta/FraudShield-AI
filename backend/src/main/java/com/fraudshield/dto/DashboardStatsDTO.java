package com.fraudshield.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDTO {

    private long totalTransactions;
    private long safeTransactions;
    private long mediumRiskTransactions;
    private long highRiskTransactions;
    private double averageRiskScore;
    private double totalAmountAnalyzed;
    private List<TransactionResponseDTO> recentTransactions;
    private Map<String, Long> riskDistribution;
    private Map<String, Double> monthlyTrends;

    public DashboardStatsDTO() {}

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getSafeTransactions() {
        return safeTransactions;
    }

    public void setSafeTransactions(long safeTransactions) {
        this.safeTransactions = safeTransactions;
    }

    public long getMediumRiskTransactions() {
        return mediumRiskTransactions;
    }

    public void setMediumRiskTransactions(long mediumRiskTransactions) {
        this.mediumRiskTransactions = mediumRiskTransactions;
    }

    public long getHighRiskTransactions() {
        return highRiskTransactions;
    }

    public void setHighRiskTransactions(long highRiskTransactions) {
        this.highRiskTransactions = highRiskTransactions;
    }

    public double getAverageRiskScore() {
        return averageRiskScore;
    }

    public void setAverageRiskScore(double averageRiskScore) {
        this.averageRiskScore = averageRiskScore;
    }

    public double getTotalAmountAnalyzed() {
        return totalAmountAnalyzed;
    }

    public void setTotalAmountAnalyzed(double totalAmountAnalyzed) {
        this.totalAmountAnalyzed = totalAmountAnalyzed;
    }

    public List<TransactionResponseDTO> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponseDTO> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public Map<String, Long> getRiskDistribution() {
        return riskDistribution;
    }

    public void setRiskDistribution(Map<String, Long> riskDistribution) {
        this.riskDistribution = riskDistribution;
    }

    public Map<String, Double> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(Map<String, Double> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }
}
