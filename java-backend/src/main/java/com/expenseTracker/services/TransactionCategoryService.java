package com.expenseTracker.services;

import com.expenseTracker.dto.TransactionCategoryDTO;
import com.expenseTracker.entities.TransactionCategory;
import com.expenseTracker.entities.User;
import com.expenseTracker.exception.ResourceNotFoundException;
import com.expenseTracker.repositories.TransactionCategoryRepository;
import com.expenseTracker.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionCategoryService {
    private final UserRepository userRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    @Transactional(readOnly = true)
    public TransactionCategoryDTO getTransactionCategoryById(Integer id) {
        log.info("Fetching transaction category with id: {}", id);

        return transactionCategoryRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<TransactionCategoryDTO> getAllTransactionCategoriesByUserId(Integer userId) {
        log.info("Fetching all transaction categories for user: {}", userId);

        return transactionCategoryRepository.findAllByUser_Id(userId).stream()
            .map(this::toDto)
                .collect(Collectors.toList());
    }

    public TransactionCategoryDTO createTransactionCategory(TransactionCategoryDTO transactionCategoryDTO) {
        log.info("Creating transaction category for user: {}", transactionCategoryDTO.getUserId());

        User user = userRepository.findById(transactionCategoryDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + transactionCategoryDTO.getUserId()));

        TransactionCategory transactionCategory = new TransactionCategory();
        transactionCategory.setCategoryName(transactionCategoryDTO.getCategoryName());
        transactionCategory.setUser(user);

        TransactionCategory savedCategory = transactionCategoryRepository.save(transactionCategory);
        log.info("Transaction category created successfully with id: {}", savedCategory.getId());

        return toDto(savedCategory);
    }

    public TransactionCategoryDTO updateTransactionCategoryById(Integer transactionCategoryId, TransactionCategoryDTO updatedDto) {
        log.info("Updating transaction category with id: {}", transactionCategoryId);

        TransactionCategory existingCategory = transactionCategoryRepository.findById(transactionCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + transactionCategoryId));

        existingCategory.setCategoryName(updatedDto.getCategoryName());

        TransactionCategory savedCategory = transactionCategoryRepository.save(existingCategory);
        log.info("Transaction category updated successfully");

        return toDto(savedCategory);
    }

    public void deleteTransactionCategoryById(Integer transactionCategoryId) {
        log.info("Deleting transaction category with id: {}", transactionCategoryId);

        TransactionCategory transactionCategory = transactionCategoryRepository.findById(transactionCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction category not found with id: " + transactionCategoryId));

        transactionCategoryRepository.delete(transactionCategory);
        log.info("Transaction category deleted successfully");
    }

    private TransactionCategoryDTO toDto(TransactionCategory category) {
        TransactionCategoryDTO dto = new TransactionCategoryDTO();
        dto.setId(category.getId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        if (category.getUser() != null) {
            dto.setUserId(category.getUser().getId());
        }
        return dto;
    }
}