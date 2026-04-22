package com.expenseTracker.repositories;

import com.expenseTracker.entities.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findAllByUser_Id(int userId);

    List<Transaction> findAllByUser_IdOrderByTransactionDateDesc(int userId, Pageable pageable);

    List<Transaction> findAllByUser_IdAndTransactionDateBetweenOrderByTransactionDateDesc(
            int userId,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("SELECT DISTINCT YEAR(t.transactionDate) FROM Transaction t WHERE t.user.id = :userId")
    List<Integer> findDistinctYears(@Param("userId") int userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND (:categoryId IS NULL OR t.transactionCategory.id = :categoryId) " +
            "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
            "AND (:minAmount IS NULL OR t.transactionAmount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR t.transactionAmount <= :maxAmount) " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByCriteria(
            @Param("userId") Integer userId,
            @Param("categoryId") Integer categoryId,
            @Param("transactionType") String transactionType,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount
    );
}
