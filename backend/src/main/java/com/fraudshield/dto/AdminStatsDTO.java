package com.fraudshield.dto;

import java.util.List;
import java.util.Map;

public class AdminStatsDTO {

    private long totalUsers;
    private long totalTransactions;
    private long safeTransactions;
    private long suspiciousTransactions;
    private long highRiskTransactions;
    private double fraudDetectionRate;
    private double averageRiskScore;
    private List<TransactionResponseDTO> recentTransactions;
    private List<FraudAnalysisResponseDTO> highRiskAlerts;
    private Map<String, Long> categoryBreakdown;

    public AdminStatsDTO() {}

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

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

    public long getSuspiciousTransactions() {
        return suspiciousTransactions;
    }

    public void setSuspiciousTransactions(long suspiciousTransactions) {
        this.suspiciousTransactions = suspiciousTransactions;
    }

    public long getHighRiskTransactions() {
        return highRiskTransactions;
    }

    public void setHighRiskTransactions(long highRiskTransactions) {
        this.highRiskTransactions = highRiskTransactions;
    }

    public double getFraudDetectionRate() {
        return fraudDetectionRate;
    }

    public void setFraudDetectionRate(double fraudDetectionRate) {
        this.fraudDetectionRate = fraudDetectionRate;
    }

    public double getAverageRiskScore() {
        return averageRiskScore;
    }

    public void setAverageRiskScore(double averageRiskScore) {
        this.averageRiskScore = averageRiskScore;
    }

    public List<TransactionResponseDTO> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<TransactionResponseDTO> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<FraudAnalysisResponseDTO> getHighRiskAlerts() {
        return highRiskAlerts;
    }

    public void setHighRiskAlerts(List<FraudAnalysisResponseDTO> highRiskAlerts) {
        this.highRiskAlerts = highRiskAlerts;
    }

    public Map<String, Long> getCategoryBreakdown() {
        return categoryBreakdown;
    }

    public void setCategoryBreakdown(Map<String, Long> categoryBreakdown) {
        this.categoryBreakdown = categoryBreakdown;
    }
}
