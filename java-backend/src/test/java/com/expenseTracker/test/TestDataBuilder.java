package com.expenseTracker.test;

import com.expenseTracker.entities.Transaction;
import com.expenseTracker.entities.TransactionCategory;
import com.expenseTracker.entities.User;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Base class for common test data setup
 */
public class TestDataBuilder {

    public static User buildUser(Integer id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    public static TransactionCategory buildCategory(Integer id, Integer userId, String categoryName) {
        User user = new User();
        user.setId(userId);
        
        TransactionCategory category = new TransactionCategory();
        category.setId(id);
        category.setUser(user);
        category.setCategoryName(categoryName);
        return category;
    }

    public static Transaction buildTransaction(
            Integer id,
            Integer userId,
            Integer categoryId,
            String transactionName,
            BigDecimal amount,
            LocalDate date,
            String transactionType) {
        
        User user = new User();
        user.setId(userId);
        
        TransactionCategory category = new TransactionCategory();
        category.setId(categoryId);
        
        Transaction transaction = new Transaction();
        transaction.setId(id);
        transaction.setUser(user);
        transaction.setTransactionCategory(category);
        transaction.setTransactionName(transactionName);
        transaction.setTransactionAmount(amount);
        transaction.setTransactionDate(date);
        transaction.setTransactionType(transactionType);
        return transaction;
    }
}
