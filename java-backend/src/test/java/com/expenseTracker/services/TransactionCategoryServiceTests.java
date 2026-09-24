package com.expenseTracker.services;

import com.expenseTracker.dto.TransactionCategoryDTO;
import com.expenseTracker.entities.TransactionCategory;
import com.expenseTracker.entities.User;
import com.expenseTracker.exception.BusinessException;
import com.expenseTracker.repositories.TransactionCategoryRepository;
import com.expenseTracker.repositories.TransactionRepository;
import com.expenseTracker.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceTests {
    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionCategoryRepository transactionCategoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionCategoryService service;
    private User user;

    @BeforeEach
    void setUp() {
        service = new TransactionCategoryService(userRepository, transactionCategoryRepository, transactionRepository);
        user = new User();
        user.setId(1);
    }

    @Test
    void createRejectsNameUsedByPredefinedCategory() {
        TransactionCategory predefined = new TransactionCategory();
        predefined.setCategoryName("Rent");
        predefined.setPredefined(true);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(transactionCategoryRepository.findAvailableByName("rent", 1)).thenReturn(List.of(predefined));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.createTransactionCategory(categoryDto("  rent  ")));

        assertEquals("A category with this name already exists. Choose a different name.", exception.getMessage());
        verify(transactionCategoryRepository, never()).save(any(TransactionCategory.class));
    }

    @Test
    void createTrimsNameBeforeSaving() {
        TransactionCategory savedCategory = new TransactionCategory();
        savedCategory.setId(7);
        savedCategory.setCategoryName("Side Income");
        savedCategory.setUser(user);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(transactionCategoryRepository.findAvailableByName("Side Income", 1)).thenReturn(List.of());
        when(transactionCategoryRepository.save(any(TransactionCategory.class))).thenReturn(savedCategory);

        TransactionCategoryDTO result = service.createTransactionCategory(categoryDto("  Side Income  "));

        assertEquals("Side Income", result.getCategoryName());
        verify(transactionCategoryRepository).save(any(TransactionCategory.class));
    }

    @Test
    void updateRejectsNameUsedByAnotherCategory() {
        TransactionCategory existing = ownedCategory(3, "Transport");
        when(transactionCategoryRepository.findById(3)).thenReturn(Optional.of(existing));
        when(transactionCategoryRepository.findAvailableByNameExcludingId("Rent", 1, 3))
                .thenReturn(List.of(new TransactionCategory()));

        assertThrows(BusinessException.class,
                () -> service.updateTransactionCategoryById(1, 3, categoryDto("Rent")));

        verify(transactionCategoryRepository, never()).save(any(TransactionCategory.class));
    }

    @Test
    void deleteRejectsCategoryUsedByTransactions() {
        TransactionCategory category = ownedCategory(3, "Transport");
        when(transactionCategoryRepository.findById(3)).thenReturn(Optional.of(category));
        when(transactionRepository.countByTransactionCategory_Id(3)).thenReturn(2L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.deleteTransactionCategoryById(1, 3));

        assertEquals("This category is used by 2 transaction(s) and cannot be deleted. Rename it instead to preserve transaction history.",
                exception.getMessage());
        verify(transactionCategoryRepository, never()).delete(category);
    }

    private TransactionCategoryDTO categoryDto(String name) {
        TransactionCategoryDTO dto = new TransactionCategoryDTO();
        dto.setUserId(1);
        dto.setCategoryName(name);
        return dto;
    }

    private TransactionCategory ownedCategory(Integer id, String name) {
        TransactionCategory category = new TransactionCategory();
        category.setId(id);
        category.setCategoryName(name);
        category.setUser(user);
        return category;
    }
}