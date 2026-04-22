package com.expenseTracker.services;

import com.expenseTracker.dto.TransactionDTO;
import com.expenseTracker.dto.SpendingBreakdownDTO;
import com.expenseTracker.entities.Transaction;
import com.expenseTracker.entities.TransactionCategory;
import com.expenseTracker.entities.User;
import com.expenseTracker.exception.BusinessException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repositories.TransactionCategoryRepository;
import com.expenseTracker.repositories.TransactionRepository;
import com.expenseTracker.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TransactionDTO> getRecentTransactionsByUserId(Integer userId, int startPage, int endPage, int pageSize) {
        log.info("Fetching recent transactions for user: {} from page {} to {}", userId, startPage, endPage);

        validateUser(userId);

        List<Transaction> combinedResults = transactionRepository
            .findAllByUser_IdOrderByTransactionDateDesc(userId, PageRequest.of(0, (endPage - startPage + 1) * pageSize))
                .stream()
                .skip((long) startPage * pageSize)
                .limit((long) (endPage - startPage + 1) * pageSize)
                .toList();

        return combinedResults.stream()
            .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactionsByUserIdAndYear(Integer userId, int year) {
        log.info("Fetching all transactions for user: {} in year: {}", userId, year);

        validateUser(userId);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        return transactionRepository.findAllByUser_IdAndTransactionDateBetweenOrderByTransactionDateDesc(
                userId, startDate, endDate
        ).stream()
            .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> getAllTransactionsByUserIdAndYearAndMonth(Integer userId, int year, int month) {
        log.info("Fetching all transactions for user: {} in year: {}, month: {}", userId, year, month);

        validateUser(userId);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = LocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth());

        return transactionRepository.findAllByUser_IdAndTransactionDateBetweenOrderByTransactionDateDesc(
                userId, startDate, endDate
        ).stream()
            .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getDistinctTransactionYears(Integer userId) {
        log.info("Fetching distinct transaction years for user: {}", userId);
        validateUser(userId);
        return transactionRepository.findDistinctYears(userId);
    }

    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        log.info("Creating transaction for user: {}", transactionDTO.getUserId());

        Transaction newTransaction = new Transaction();
        applyTransactionFields(newTransaction, transactionDTO);

        Transaction savedTransaction = transactionRepository.save(newTransaction);
        log.info("Transaction created successfully with id: {}", savedTransaction.getId());

        return toDto(savedTransaction);
    }

    public TransactionDTO updateTransaction(TransactionDTO transactionDTO) {
        log.info("Updating transaction with id: {}", transactionDTO.getId());

        Transaction existingTransaction = transactionRepository.findById(transactionDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionDTO.getId()));

        applyTransactionFields(existingTransaction, transactionDTO);

        Transaction savedTransaction = transactionRepository.save(existingTransaction);
        log.info("Transaction updated successfully");

        return toDto(savedTransaction);
    }

    public void deleteTransactionById(Integer transactionId) {
        log.info("Deleting transaction with id: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + transactionId));

        transactionRepository.delete(transaction);
        log.info("Transaction deleted successfully");
    }

    private void validateUser(Integer userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("Invalid user ID provided");
        }
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    private void applyTransactionFields(Transaction transaction, TransactionDTO transactionDTO) {
        if (transactionDTO.getUserId() == null) {
            throw new BusinessException("User ID is required");
        }
        if (transactionDTO.getCategoryId() == null) {
            throw new BusinessException("Category ID is required");
        }

        User user = userRepository.findById(transactionDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + transactionDTO.getUserId()));

        TransactionCategory category = transactionCategoryRepository.findById(transactionDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + transactionDTO.getCategoryId()));

        transaction.setUser(user);
        transaction.setTransactionCategory(category);
        transaction.setTransactionName(transactionDTO.getTransactionName());
        transaction.setTransactionAmount(transactionDTO.getTransactionAmount());
        transaction.setTransactionDate(transactionDTO.getTransactionDate());
        transaction.setTransactionType(transactionDTO.getTransactionType());
    }

    private TransactionDTO toDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser() == null ? null : transaction.getUser().getId());
        dto.setTransactionName(transaction.getTransactionName());
        dto.setTransactionAmount(transaction.getTransactionAmount());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setTransactionType(transaction.getTransactionType());
        if (transaction.getTransactionCategory() != null) {
            dto.setCategoryId(transaction.getTransactionCategory().getId());
            dto.setCategoryName(transaction.getTransactionCategory().getCategoryName());
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<TransactionDTO> searchTransactions(Integer userId, Integer categoryId, String transactionType, 
                                                   BigDecimal minAmount, BigDecimal maxAmount) {
        log.info("Searching transactions for user: {}, category: {}, type: {}, minAmount: {}, maxAmount: {}", 
                 userId, categoryId, transactionType, minAmount, maxAmount);

        validateUser(userId);

        List<Transaction> results = transactionRepository.findTransactionsByCriteria(
                userId, categoryId, transactionType, minAmount, maxAmount
        );

        return results.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SpendingBreakdownDTO getSpendingBreakdown(Integer userId) {
        log.info("Calculating spending breakdown for user: {}", userId);

        validateUser(userId);

        List<Transaction> allTransactions = transactionRepository.findAllByUser_Id(userId);

        BigDecimal needsAmount = BigDecimal.ZERO;
        BigDecimal wantsAmount = BigDecimal.ZERO;
        BigDecimal investmentAmount = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (Transaction tx : allTransactions) {
            if ("INCOME".equals(tx.getTransactionType())) {
                totalIncome = totalIncome.add(tx.getTransactionAmount());
            } else if ("EXPENSE-NEED".equals(tx.getTransactionType())) {
                needsAmount = needsAmount.add(tx.getTransactionAmount());
            } else if ("EXPENSE-WANT".equals(tx.getTransactionType())) {
                wantsAmount = wantsAmount.add(tx.getTransactionAmount());
            } else if ("EXPENSE-INVESTMENT".equals(tx.getTransactionType())) {
                investmentAmount = investmentAmount.add(tx.getTransactionAmount());
            }
        }

        BigDecimal totalSpent = needsAmount.add(wantsAmount).add(investmentAmount);
        BigDecimal savingsAmount = totalIncome.subtract(totalSpent);
        if (savingsAmount.compareTo(BigDecimal.ZERO) < 0) {
            savingsAmount = BigDecimal.ZERO;
        }

        // Calculate percentages based on total income
        Double needsPercentage = 0.0;
        Double wantsPercentage = 0.0;
        Double investmentPercentage = 0.0;
        Double savingsPercentage = 0.0;

        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            needsPercentage = (needsAmount.doubleValue() / totalIncome.doubleValue()) * 100;
            wantsPercentage = (wantsAmount.doubleValue() / totalIncome.doubleValue()) * 100;
            investmentPercentage = (investmentAmount.doubleValue() / totalIncome.doubleValue()) * 100;
            savingsPercentage = (savingsAmount.doubleValue() / totalIncome.doubleValue()) * 100;
        }

        return SpendingBreakdownDTO.builder()
                .needsAmount(needsAmount)
                .wantsAmount(wantsAmount)
                .investmentAmount(investmentAmount)
                .totalSpent(totalSpent)
                .totalIncome(totalIncome)
                .savingsAmount(savingsAmount)
                .needsPercentage(needsPercentage)
                .wantsPercentage(wantsPercentage)
                .investmentPercentage(investmentPercentage)
                .savingsPercentage(savingsPercentage)
                .build();
    }
}