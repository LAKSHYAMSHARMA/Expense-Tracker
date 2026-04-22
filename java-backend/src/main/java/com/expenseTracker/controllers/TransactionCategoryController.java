package com.expenseTracker.controllers;

import com.expenseTracker.dto.ApiResponse;
import com.expenseTracker.dto.TransactionCategoryDTO;
import com.expenseTracker.services.TransactionCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction-categories")
@RequiredArgsConstructor
@Slf4j
public class TransactionCategoryController {
    private final TransactionCategoryService transactionCategoryService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TransactionCategoryDTO>>> getAllTransactionCategoriesByUserId(@PathVariable Integer userId) {
        log.info("GET request: Fetch all transaction categories for user: {}", userId);
        List<TransactionCategoryDTO> transactionCategories = transactionCategoryService.getAllTransactionCategoriesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(transactionCategories, "Categories retrieved successfully", HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionCategoryDTO>> getTransactionCategoryById(@PathVariable Integer id) {
        log.info("GET request: Fetch transaction category with id: {}", id);
        TransactionCategoryDTO transactionCategory = transactionCategoryService.getTransactionCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(transactionCategory, "Category retrieved successfully", HttpStatus.OK.value()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionCategoryDTO>> createTransactionCategory(@Valid @RequestBody TransactionCategoryDTO transactionCategoryDTO) {
        log.info("POST request: Create transaction category: {}", transactionCategoryDTO.getCategoryName());
        TransactionCategoryDTO createdCategory = transactionCategoryService.createTransactionCategory(transactionCategoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCategory, "Category created successfully", HttpStatus.CREATED.value()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionCategoryDTO>> updateTransactionCategoryById(
            @PathVariable Integer id,
            @Valid @RequestBody TransactionCategoryDTO transactionCategoryDTO) {
        log.info("PUT request: Update transaction category with id: {}", id);
        TransactionCategoryDTO updatedTransactionCategory = transactionCategoryService.updateTransactionCategoryById(id, transactionCategoryDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedTransactionCategory, "Category updated successfully", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransactionCategoryById(@PathVariable Integer id) {
        log.info("DELETE request: Delete transaction category with id: {}", id);
        transactionCategoryService.deleteTransactionCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully", HttpStatus.OK.value()));
    }
}