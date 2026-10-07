package com.fraudshield.repository;

import com.fraudshield.entity.RiskFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RiskFactorRepository extends JpaRepository<RiskFactor, Long> {
    List<RiskFactor> findByFraudAnalysisId(Long fraudAnalysisId);
}
