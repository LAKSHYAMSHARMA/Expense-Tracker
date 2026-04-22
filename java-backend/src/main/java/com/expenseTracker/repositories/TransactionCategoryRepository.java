package com.expenseTracker.repositories;

import com.expenseTracker.entities.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Integer> {
    List<TransactionCategory> findAllByUser_Id(int userId);
}
