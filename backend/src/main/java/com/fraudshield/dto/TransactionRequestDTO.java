package com.fraudshield.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransactionRequestDTO {

    private String transactionReference;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String currency = "USD";

    @NotBlank(message = "Transaction type is required")
    private String transactionType;

    @NotBlank(message = "Merchant category is required")
    private String merchantCategory;

    @NotBlank(message = "Transaction location is required")
    private String location;

    @NotBlank(message = "Usual location is required")
    private String usualLocation;

    @NotBlank(message = "Device type is required")
    private String deviceType;

    private boolean isNewDevice;

    private String ipAddress;

    private int failedAttempts = 0;

    private int accountAgeDays = 60;

    private int transactionFrequency = 1;

    public TransactionRequestDTO() {}

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getMerchantCategory() {
        return merchantCategory;
    }

    public void setMerchantCategory(String merchantCategory) {
        this.merchantCategory = merchantCategory;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUsualLocation() {
        return usualLocation;
    }

    public void setUsualLocation(String usualLocation) {
        this.usualLocation = usualLocation;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isNewDevice() {
        return isNewDevice;
    }

    public void setNewDevice(boolean newDevice) {
        isNewDevice = newDevice;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public int getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(int accountAgeDays) {
        this.accountAgeDays = accountAgeDays;
    }

    public int getTransactionFrequency() {
        return transactionFrequency;
    }

    public void setTransactionFrequency(int transactionFrequency) {
        this.transactionFrequency = transactionFrequency;
    }
}
