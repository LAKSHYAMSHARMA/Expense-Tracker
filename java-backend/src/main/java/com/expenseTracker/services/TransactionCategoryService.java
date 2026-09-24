package com.expenseTracker.services;

import com.expenseTracker.dto.TransactionCategoryDTO;
import com.expenseTracker.entities.TransactionCategory;
import com.expenseTracker.entities.User;
import com.expenseTracker.exception.BusinessException;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repositories.TransactionCategoryRepository;
import com.expenseTracker.repositories.TransactionRepository;
import com.expenseTracker.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionCategoryService {
    private static final List<String> PREDEFINED_CATEGORY_NAMES = Arrays.asList(
            "Rent", "Salary", "Miscellaneous", "Grocery", "Traveling"
    );

    private final UserRepository userRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public TransactionCategoryDTO getTransactionCategoryById(Integer userId, Integer id) {
        log.info("Fetching transaction category with id: {}", id);

        TransactionCategory category = transactionCategoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + id));
        ensureAvailableToUser(category, userId);
        return toDto(category);
    }

    @Transactional(readOnly = true)
    public List<TransactionCategoryDTO> getAllTransactionCategoriesByUserId(Integer userId) {
        log.info("Fetching all transaction categories for user: {}", userId);

        return transactionCategoryRepository.findAllAvailableForUser(userId).stream()
            .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TransactionCategoryDTO createTransactionCategory(TransactionCategoryDTO transactionCategoryDTO) {
        log.info("Creating transaction category for user: {}", transactionCategoryDTO.getUserId());

        User user = userRepository.findById(transactionCategoryDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + transactionCategoryDTO.getUserId()));

        String categoryName = normalizeName(transactionCategoryDTO.getCategoryName());
        ensureNameIsAvailable(categoryName, transactionCategoryDTO.getUserId(), null);

        TransactionCategory transactionCategory = new TransactionCategory();
        transactionCategory.setCategoryName(categoryName);
        transactionCategory.setUser(user);
        transactionCategory.setPredefined(false);

        TransactionCategory savedCategory = transactionCategoryRepository.save(transactionCategory);
        log.info("Transaction category created successfully with id: {}", savedCategory.getId());

        return toDto(savedCategory);
    }

    public TransactionCategoryDTO updateTransactionCategoryById(Integer userId, Integer transactionCategoryId, TransactionCategoryDTO updatedDto) {
        log.info("Updating transaction category with id: {}", transactionCategoryId);

        TransactionCategory existingCategory = transactionCategoryRepository.findById(transactionCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + transactionCategoryId));

        ensureOwnedByUser(existingCategory, userId);

        String categoryName = normalizeName(updatedDto.getCategoryName());
        ensureNameIsAvailable(categoryName, userId, transactionCategoryId);

        existingCategory.setCategoryName(categoryName);

        TransactionCategory savedCategory = transactionCategoryRepository.save(existingCategory);
        log.info("Transaction category updated successfully");

        return toDto(savedCategory);
    }

    public void deleteTransactionCategoryById(Integer userId, Integer transactionCategoryId) {
        log.info("Deleting transaction category with id: {}", transactionCategoryId);

        TransactionCategory transactionCategory = transactionCategoryRepository.findById(transactionCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + transactionCategoryId));

        ensureOwnedByUser(transactionCategory, userId);

        long transactionCount = transactionRepository.countByTransactionCategory_Id(transactionCategoryId);
        if (transactionCount > 0) {
            throw new BusinessException("This category is used by " + transactionCount
                + " transaction(s) and cannot be deleted. Rename it instead to preserve transaction history.");
        }

        transactionCategoryRepository.delete(transactionCategory);
        log.info("Transaction category deleted successfully");
    }

    private void ensureOwnedByUser(TransactionCategory category, Integer userId) {
        if (category.isPredefined()) {
            throw new BusinessException("Predefined categories cannot be changed");
        }
        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Category not found");
        }
    }

    private void ensureAvailableToUser(TransactionCategory category, Integer userId) {
        if (!category.isPredefined() && (category.getUser() == null || !category.getUser().getId().equals(userId))) {
            throw new ResourceNotFoundException("Category not found");
        }
    }

    private void ensureNameIsAvailable(String categoryName, Integer userId, Integer categoryId) {
        List<TransactionCategory> matchingCategories = categoryId == null
                ? transactionCategoryRepository.findAvailableByName(categoryName, userId)
                : transactionCategoryRepository.findAvailableByNameExcludingId(categoryName, userId, categoryId);

        if (!matchingCategories.isEmpty()) {
            throw new BusinessException("A category with this name already exists. Choose a different name.");
        }
    }

    private String normalizeName(String categoryName) {
        return categoryName.trim();
    }

    public void initializePredefinedCategories() {
        PREDEFINED_CATEGORY_NAMES.forEach(categoryName ->
                transactionCategoryRepository.findByCategoryNameAndUserIsNull(categoryName)
                        .orElseGet(() -> transactionCategoryRepository.save(TransactionCategory.builder()
                                .categoryName(categoryName)
                                .predefined(true)
                                .build()))
        );
    }

    private TransactionCategoryDTO toDto(TransactionCategory category) {
        TransactionCategoryDTO dto = new TransactionCategoryDTO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setPredefined(category.isPredefined());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        if (category.getUser() != null) {
            dto.setUserId(category.getUser().getId());
        }
        return dto;
    }
}