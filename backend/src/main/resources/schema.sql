-- ====================================================================
-- FraudShield AI - Database Schema for MySQL 8.0+
-- ====================================================================

CREATE DATABASE IF NOT EXISTS `fraudshield_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `fraudshield_db`;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(20) NOT NULL,
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NULL,
    INDEX `idx_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Transactions Table
CREATE TABLE IF NOT EXISTS `transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `transaction_reference` VARCHAR(64) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `amount` DECIMAL(12, 2) NOT NULL,
    `currency` VARCHAR(10) NOT NULL DEFAULT 'USD',
    `transaction_type` VARCHAR(50) NOT NULL,
    `merchant_category` VARCHAR(50) NOT NULL,
    `location` VARCHAR(100) NOT NULL,
    `usual_location` VARCHAR(100) NOT NULL,
    `device_type` VARCHAR(50) NOT NULL,
    `is_new_device` BOOLEAN NOT NULL DEFAULT FALSE,
    `ip_address` VARCHAR(45) NULL,
    `failed_attempts` INT NOT NULL DEFAULT 0,
    `account_age_days` INT NOT NULL DEFAULT 30,
    `transaction_frequency` INT NOT NULL DEFAULT 1,
    `status` VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    `created_at` DATETIME NOT NULL,
    CONSTRAINT `fk_txn_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_txn_ref` (`transaction_reference`),
    INDEX `idx_txn_user` (`user_id`),
    INDEX `idx_txn_created` (`created_at`),
    INDEX `idx_txn_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Fraud Analysis Table
CREATE TABLE IF NOT EXISTS `fraud_analysis` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `transaction_id` BIGINT NOT NULL UNIQUE,
    `risk_score` INT NOT NULL,
    `risk_level` VARCHAR(20) NOT NULL,
    `fraud_probability` DOUBLE NOT NULL,
    `recommendation` VARCHAR(255) NOT NULL,
    `ai_explanation` TEXT NULL,
    `engine_type` VARCHAR(50) NOT NULL,
    `analyzed_at` DATETIME NOT NULL,
    CONSTRAINT `fk_analysis_txn` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`id`) ON DELETE CASCADE,
    INDEX `idx_analysis_score` (`risk_score`),
    INDEX `idx_analysis_level` (`risk_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Risk Factors Table
CREATE TABLE IF NOT EXISTS `risk_factors` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `fraud_analysis_id` BIGINT NOT NULL,
    `factor_code` VARCHAR(60) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `severity` VARCHAR(20) NOT NULL,
    `weight_contribution` INT NOT NULL,
    CONSTRAINT `fk_factor_analysis` FOREIGN KEY (`fraud_analysis_id`) REFERENCES `fraud_analysis` (`id`) ON DELETE CASCADE,
    INDEX `idx_factor_analysis` (`fraud_analysis_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Audit Logs Table
CREATE TABLE IF NOT EXISTS `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NULL,
    `user_email` VARCHAR(100) NULL,
    `action` VARCHAR(60) NOT NULL,
    `resource` VARCHAR(60) NOT NULL,
    `ip_address` VARCHAR(45) NULL,
    `details` VARCHAR(500) NULL,
    `timestamp` DATETIME NOT NULL,
    INDEX `idx_audit_user` (`user_email`),
    INDEX `idx_audit_action` (`action`),
    INDEX `idx_audit_time` (`timestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
