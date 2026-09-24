package com.expenseTracker.repositories;

import com.expenseTracker.entities.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Integer> {
    List<TransactionCategory> findAllByUser_Id(int userId);

    @Query("SELECT c FROM TransactionCategory c WHERE LOWER(TRIM(c.categoryName)) = LOWER(TRIM(:categoryName)) " +
        "AND (c.user IS NULL OR c.user.id = :userId)")
    List<TransactionCategory> findAvailableByName(@Param("categoryName") String categoryName, @Param("userId") Integer userId);

    @Query("SELECT c FROM TransactionCategory c WHERE LOWER(TRIM(c.categoryName)) = LOWER(TRIM(:categoryName)) " +
        "AND (c.user IS NULL OR c.user.id = :userId) AND c.id <> :categoryId")
    List<TransactionCategory> findAvailableByNameExcludingId(
        @Param("categoryName") String categoryName,
        @Param("userId") Integer userId,
        @Param("categoryId") Integer categoryId
    );

    @Query("SELECT c FROM TransactionCategory c WHERE c.user IS NULL OR c.user.id = :userId ORDER BY c.predefined DESC, c.categoryName ASC")
    List<TransactionCategory> findAllAvailableForUser(@Param("userId") Integer userId);

    Optional<TransactionCategory> findByCategoryNameAndUserIsNull(String categoryName);
}
