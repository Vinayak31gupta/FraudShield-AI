package com.fraudshield.repository;

import com.fraudshield.entity.Transaction;
import com.fraudshield.entity.TransactionStatus;
import com.fraudshield.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionReference(String transactionReference);

    Page<Transaction> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Transaction> findTop10ByUserOrderByCreatedAtDesc(User user);

    List<Transaction> findTop10ByOrderByCreatedAtDesc();

    Page<Transaction> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByUser(User user);

    long countByUserAndStatus(User user, TransactionStatus status);

    long countByStatus(TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.createdAt >= :since")
    List<Transaction> findRecentByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(t.amount) FROM Transaction t WHERE t.user = :user")
    Double getAverageTransactionAmountByUser(@Param("user") User user);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:userId IS NULL OR t.user.id = :userId) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.transactionReference) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(t.merchantCategory) LIKE LOWER(CONCAT('%', :search, '%')) " +
           " OR LOWER(t.location) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> searchTransactions(
            @Param("userId") Long userId,
            @Param("status") TransactionStatus status,
            @Param("search") String search,
            Pageable pageable);
}
