package com.expenseTracker.controllers;

import com.expenseTracker.dto.ApiResponse;
import com.expenseTracker.dto.TransactionCategoryDTO;
import com.expenseTracker.services.TransactionCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction-categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transaction Categories", description = "Transaction category management endpoints")
public class TransactionCategoryController {
    private final TransactionCategoryService transactionCategoryService;

    private Integer getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer) {
            return (Integer) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all transaction categories for authenticated user")
    public ResponseEntity<ApiResponse<List<TransactionCategoryDTO>>> getAllTransactionCategoriesByUserId() {
        Integer userId = getAuthenticatedUserId();
        log.info("GET request: Fetch all transaction categories for user: {}", userId);
        List<TransactionCategoryDTO> transactionCategories = transactionCategoryService.getAllTransactionCategoriesByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(transactionCategories, "Categories retrieved successfully", HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a transaction category by ID")
    public ResponseEntity<ApiResponse<TransactionCategoryDTO>> getTransactionCategoryById(@PathVariable Integer id) {
        log.info("GET request: Fetch transaction category with id: {}", id);
        TransactionCategoryDTO transactionCategory = transactionCategoryService.getTransactionCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(transactionCategory, "Category retrieved successfully", HttpStatus.OK.value()));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new transaction category")
    public ResponseEntity<ApiResponse<TransactionCategoryDTO>> createTransactionCategory(@Valid @RequestBody TransactionCategoryDTO transactionCategoryDTO) {
        Integer userId = getAuthenticatedUserId();
        transactionCategoryDTO.setUserId(userId);
        log.info("POST request: Create transaction category: {} for user: {}", transactionCategoryDTO.getCategoryName(), userId);
        TransactionCategoryDTO createdCategory = transactionCategoryService.createTransactionCategory(transactionCategoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdCategory, "Category created successfully", HttpStatus.CREATED.value()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update an existing transaction category")
    public ResponseEntity<ApiResponse<TransactionCategoryDTO>> updateTransactionCategoryById(
            @PathVariable Integer id,
            @Valid @RequestBody TransactionCategoryDTO transactionCategoryDTO) {
        Integer userId = getAuthenticatedUserId();
        transactionCategoryDTO.setUserId(userId);
        log.info("PUT request: Update transaction category with id: {} for user: {}", id, userId);
        TransactionCategoryDTO updatedTransactionCategory = transactionCategoryService.updateTransactionCategoryById(id, transactionCategoryDTO);
        return ResponseEntity.ok(ApiResponse.success(updatedTransactionCategory, "Category updated successfully", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a transaction category")
    public ResponseEntity<ApiResponse<Void>> deleteTransactionCategoryById(@PathVariable Integer id) {
        log.info("DELETE request: Delete transaction category with id: {}", id);
        transactionCategoryService.deleteTransactionCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully", HttpStatus.OK.value()));
    }
}