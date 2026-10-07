package com.fraudshield.dto;

import java.math.BigDecimal;
import java.util.Set;

public class UserHistoryDTO {

    private BigDecimal averageAmount;
    private long totalPriorTransactions;
    private Set<String> knownLocations;
    private Set<String> knownDevices;
    private int accountAgeDays;

    public UserHistoryDTO() {}

    public UserHistoryDTO(BigDecimal averageAmount, long totalPriorTransactions, Set<String> knownLocations, Set<String> knownDevices, int accountAgeDays) {
        this.averageAmount = averageAmount;
        this.totalPriorTransactions = totalPriorTransactions;
        this.knownLocations = knownLocations;
        this.knownDevices = knownDevices;
        this.accountAgeDays = accountAgeDays;
    }

    public BigDecimal getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(BigDecimal averageAmount) {
        this.averageAmount = averageAmount;
    }

    public long getTotalPriorTransactions() {
        return totalPriorTransactions;
    }

    public void setTotalPriorTransactions(long totalPriorTransactions) {
        this.totalPriorTransactions = totalPriorTransactions;
    }

    public Set<String> getKnownLocations() {
        return knownLocations;
    }

    public void setKnownLocations(Set<String> knownLocations) {
        this.knownLocations = knownLocations;
    }

    public Set<String> getKnownDevices() {
        return knownDevices;
    }

    public void setKnownDevices(Set<String> knownDevices) {
        this.knownDevices = knownDevices;
    }

    public int getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(int accountAgeDays) {
        this.accountAgeDays = accountAgeDays;
    }
}
