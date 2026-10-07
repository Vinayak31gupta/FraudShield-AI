package com.fraudshield.config;

import com.fraudshield.dto.TransactionRequestDTO;
import com.fraudshield.entity.Role;
import com.fraudshield.entity.User;
import com.fraudshield.repository.UserRepository;
import com.fraudshield.service.AuditLogService;
import com.fraudshield.service.FraudAnalysisService;
import com.fraudshield.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;
    private final FraudAnalysisService fraudAnalysisService;
    private final AuditLogService auditLogService;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           TransactionService transactionService,
                           FraudAnalysisService fraudAnalysisService,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.transactionService = transactionService;
        this.fraudAnalysisService = fraudAnalysisService;
        this.auditLogService = auditLogService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            logger.info("Database already contains users. Skipping initial seed.");
            return;
        }

        logger.info("Bootstrapping FraudShield AI sample data and user accounts...");

        // 1. Create Admin Account
        User admin = new User(
                "admin@fraudshield.ai",
                passwordEncoder.encode("Admin@123"),
                "Chief Security Officer",
                Role.ROLE_ADMIN
        );
        userRepository.save(admin);

        // 2. Create Standard Demo Users
        User john = new User(
                "john.doe@example.com",
                passwordEncoder.encode("User@123"),
                "John Doe",
                Role.ROLE_USER
        );
        userRepository.save(john);

        User sarah = new User(
                "sarah.smith@example.com",
                passwordEncoder.encode("User@123"),
                "Sarah Smith",
                Role.ROLE_USER
        );
        userRepository.save(sarah);

        // 3. Seed Realistic Transactions for John Doe
        // A) Safe everyday purchase
        seedAndAnalyze(john, "TXN-1001", new BigDecimal("42.50"), "PURCHASE", "RETAIL",
                "San Francisco, USA", "San Francisco, USA", "iPhone 15 Pro", false,
                "192.168.1.15", 0, 365, 1);

        // B) Safe dining purchase
        seedAndAnalyze(john, "TXN-1002", new BigDecimal("88.00"), "PURCHASE", "RETAIL",
                "San Francisco, USA", "San Francisco, USA", "iPhone 15 Pro", false,
                "192.168.1.15", 0, 365, 2);

        // C) Medium Risk - Unusual location & higher amount
        seedAndAnalyze(john, "TXN-1003", new BigDecimal("780.00"), "PURCHASE", "ELECTRONICS",
                "Seattle, USA", "San Francisco, USA", "MacBook Pro", true,
                "73.189.42.10", 1, 365, 3);

        // D) High Risk / Potential Fraud - Mismatch location, unknown device, multiple failed attempts, high amount
        seedAndAnalyze(john, "TXN-1004", new BigDecimal("4850.00"), "ONLINE_TRANSFER", "CRYPTO_EXCHANGE",
                "London, UK", "San Francisco, USA", "Linux Workstation", true,
                "185.220.101.4", 3, 365, 6);

        // E) High Risk - Extreme amount, luxury, foreign country
        seedAndAnalyze(john, "TXN-1005", new BigDecimal("11500.00"), "PURCHASE", "LUXURY_WATCHES",
                "Dubai, UAE", "San Francisco, USA", "Unknown Android", true,
                "94.200.15.88", 4, 365, 5);

        // F) Safe utility payment
        seedAndAnalyze(john, "TXN-1006", new BigDecimal("125.00"), "PAYMENT", "UTILITIES",
                "San Francisco, USA", "San Francisco, USA", "iPhone 15 Pro", false,
                "192.168.1.15", 0, 365, 1);

        // Seed transaction for Sarah
        seedAndAnalyze(sarah, "TXN-2001", new BigDecimal("320.00"), "PURCHASE", "TRAVEL",
                "New York, USA", "New York, USA", "iPad Pro", false,
                "67.245.10.12", 0, 180, 2);

        logger.info("Sample database initialization completed successfully.");
    }

    private void seedAndAnalyze(User user, String ref, BigDecimal amount, String type, String category,
                                String location, String usualLocation, String device, boolean isNewDevice,
                                String ip, int failedAttempts, int accountAge, int frequency) {
        TransactionRequestDTO dto = new TransactionRequestDTO();
        dto.setTransactionReference(ref);
        dto.setAmount(amount);
        dto.setCurrency("USD");
        dto.setTransactionType(type);
        dto.setMerchantCategory(category);
        dto.setLocation(location);
        dto.setUsualLocation(usualLocation);
        dto.setDeviceType(device);
        dto.setNewDevice(isNewDevice);
        dto.setIpAddress(ip);
        dto.setFailedAttempts(failedAttempts);
        dto.setAccountAgeDays(accountAge);
        dto.setTransactionFrequency(frequency);

        var txn = transactionService.createTransaction(dto, user, ip);
        fraudAnalysisService.analyzeTransaction(txn.getId(), user, ip);
    }
}
