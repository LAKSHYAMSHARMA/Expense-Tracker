package com.expenseTracker.services;

import com.expenseTracker.dto.SpendingBreakdownDTO;
import com.expenseTracker.dto.TransactionDTO;
import com.expenseTracker.entities.Transaction;
import com.expenseTracker.entities.TransactionCategory;
import com.expenseTracker.entities.User;
import com.expenseTracker.exception.BusinessException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repositories.TransactionCategoryRepository;
import com.expenseTracker.repositories.TransactionRepository;
import com.expenseTracker.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TransactionServiceTests {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private TransactionCategory testCategory;
    private Transaction testTransaction;
    private TransactionDTO testTransactionDTO;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testCategory = new TransactionCategory();
        testCategory.setId(1);
        testCategory.setCategoryName("Groceries");
        testCategory.setUser(testUser);

        testTransaction = new Transaction();
        testTransaction.setId(1);
        testTransaction.setUser(testUser);
        testTransaction.setTransactionCategory(testCategory);
        testTransaction.setTransactionName("Weekly groceries");
        testTransaction.setTransactionAmount(new BigDecimal("50.00"));
        testTransaction.setTransactionDate(LocalDate.now());
        testTransaction.setTransactionType("EXPENSE-NEED");

        testTransactionDTO = new TransactionDTO();
        testTransactionDTO.setId(1);
        testTransactionDTO.setUserId(1);
        testTransactionDTO.setCategoryId(1);
        testTransactionDTO.setTransactionName("Weekly groceries");
        testTransactionDTO.setTransactionAmount(new BigDecimal("50.00"));
        testTransactionDTO.setTransactionDate(LocalDate.now());
        testTransactionDTO.setTransactionType("EXPENSE-NEED");
    }

    @Test
    public void testCreateTransaction_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionCategoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.createTransaction(testTransactionDTO);

        assertNotNull(result);
        assertEquals("Weekly groceries", result.getTransactionName());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testCreateTransaction_InvalidUser() {
        testTransactionDTO.setUserId(null);
        assertThrows(BusinessException.class, () -> transactionService.createTransaction(testTransactionDTO));
    }

    @Test
    public void testCreateTransaction_UserNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.createTransaction(testTransactionDTO));
    }

    @Test
    public void testDeleteTransaction_Success() {
        when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));

        assertDoesNotThrow(() -> transactionService.deleteTransactionById(1));
        verify(transactionRepository, times(1)).delete(testTransaction);
    }

    @Test
    public void testDeleteTransaction_NotFound() {
        when(transactionRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransactionById(1));
    }

    @Test
    public void testGetAllTransactionsByYear_Success() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(transactionRepository.findAllByUser_IdAndTransactionDateBetweenOrderByTransactionDateDesc(
                eq(1), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(testTransaction));

        List<TransactionDTO> results = transactionService.getAllTransactionsByUserIdAndYear(1, 2025);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Weekly groceries", results.get(0).getTransactionName());
    }

    @Test
    public void testGetAllTransactionsByYear_InvalidUser() {
        when(userRepository.existsById(0)).thenReturn(false);
        assertThrows(BusinessException.class, () -> transactionService.getAllTransactionsByUserIdAndYear(0, 2025));
    }

    @Test
    public void testGetSpendingBreakdown_Success() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(transactionRepository.findAllByUser_Id(1)).thenReturn(List.of(testTransaction));

        SpendingBreakdownDTO result = transactionService.getSpendingBreakdown(1);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getNeedsAmount());
        assertEquals(0.0, result.getWantsPercentage());
    }

    @Test
    public void testGetDistinctTransactionYears_Success() {
        when(userRepository.existsById(1)).thenReturn(true);
        when(transactionRepository.findDistinctYears(1)).thenReturn(List.of(2023, 2024, 2025));

        List<Integer> results = transactionService.getDistinctTransactionYears(1);

        assertNotNull(results);
        assertEquals(3, results.size());
        assertTrue(results.contains(2025));
    }

    @Test
    public void testUpdateTransaction_Success() {
        when(transactionRepository.findById(1)).thenReturn(Optional.of(testTransaction));
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(transactionCategoryRepository.findById(1)).thenReturn(Optional.of(testCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionDTO result = transactionService.updateTransaction(testTransactionDTO);

        assertNotNull(result);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    public void testGetRecentTransactions_InvalidPagination() {
        when(userRepository.existsById(1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> 
            transactionService.getRecentTransactionsByUserId(1, 5, 2, 10));
    }
}
