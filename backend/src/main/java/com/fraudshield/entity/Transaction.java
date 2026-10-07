package com.fraudshield.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_ref", columnList = "transaction_reference", unique = true),
    @Index(name = "idx_txn_user", columnList = "user_id"),
    @Index(name = "idx_txn_created", columnList = "created_at"),
    @Index(name = "idx_txn_status", columnList = "status")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 64)
    private String transactionReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "USD";

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "merchant_category", nullable = false, length = 50)
    private String merchantCategory;

    @Column(nullable = false, length = 100)
    private String location;

    @Column(name = "usual_location", nullable = false, length = 100)
    private String usualLocation;

    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;

    @Column(name = "is_new_device", nullable = false)
    private boolean isNewDevice;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "account_age_days", nullable = false)
    private int accountAgeDays = 30;

    @Column(name = "transaction_frequency", nullable = false)
    private int transactionFrequency = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status = TransactionStatus.APPROVED;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FraudAnalysis fraudAnalysis;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Transaction() {}

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public FraudAnalysis getFraudAnalysis() {
        return fraudAnalysis;
    }

    public void setFraudAnalysis(FraudAnalysis fraudAnalysis) {
        this.fraudAnalysis = fraudAnalysis;
        if (fraudAnalysis != null) {
            fraudAnalysis.setTransaction(this);
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
