package com.fraudshield.repository;

import com.fraudshield.entity.FraudAnalysis;
import com.fraudshield.entity.RiskLevel;
import com.fraudshield.entity.Transaction;
import com.fraudshield.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudAnalysisRepository extends JpaRepository<FraudAnalysis, Long> {

    Optional<FraudAnalysis> findByTransaction(Transaction transaction);

    Optional<FraudAnalysis> findByTransactionId(Long transactionId);

    long countByRiskLevel(RiskLevel riskLevel);

    @Query("SELECT COUNT(fa) FROM FraudAnalysis fa WHERE fa.transaction.user = :user AND fa.riskLevel = :riskLevel")
    long countByUserAndRiskLevel(@Param("user") User user, @Param("riskLevel") RiskLevel riskLevel);

    @Query("SELECT AVG(fa.riskScore) FROM FraudAnalysis fa")
    Double getGlobalAverageRiskScore();

    @Query("SELECT AVG(fa.riskScore) FROM FraudAnalysis fa WHERE fa.transaction.user = :user")
    Double getUserAverageRiskScore(@Param("user") User user);

    @Query("SELECT fa FROM FraudAnalysis fa WHERE fa.riskLevel = 'HIGH' ORDER BY fa.riskScore DESC")
    List<FraudAnalysis> findTopHighRisk(Pageable pageable);

    @Query("SELECT fa FROM FraudAnalysis fa WHERE fa.transaction.user = :user ORDER BY fa.analyzedAt DESC")
    List<FraudAnalysis> findRecentByUser(@Param("user") User user, Pageable pageable);
}
