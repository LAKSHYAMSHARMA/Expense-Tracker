package com.expenseTracker.controllers;

import com.expenseTracker.dto.ApiResponse;
import com.expenseTracker.dto.SpendingBreakdownDTO;
import com.expenseTracker.dto.TransactionDTO;
import com.expenseTracker.services.TransactionService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Transactions", description = "Transaction management endpoints")
public class TransactionController {
    private final TransactionService transactionService;

    private Integer getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Integer) {
            return (Integer) auth.getPrincipal();
        }
        return null;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get transactions by year and optional month")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getAllTransactionsByUserIdAndYear(
            @RequestParam int year,
            @RequestParam(required = false) Integer month) {
        Integer userId = getAuthenticatedUserId();
        log.info("Fetching transactions for userId={}, year={}, month={}", userId, year, month);

        List<TransactionDTO> transactionsList = (month == null)
                ? transactionService.getAllTransactionsByUserIdAndYear(userId, year)
                : transactionService.getAllTransactionsByUserIdAndYearAndMonth(userId, year, month);

        return ResponseEntity.ok(ApiResponse.success(transactionsList, "Transactions retrieved successfully", HttpStatus.OK.value()));
    }

    @GetMapping("/recent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get recent transactions with pagination")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getRecentTransactionsByUserId(
            @RequestParam int startPage,
            @RequestParam int endPage,
            @RequestParam int size
    ) {
        Integer userId = getAuthenticatedUserId();
        log.info("Fetching recent transactions for userId={}, startPage={}, endPage={}, size={}",
                userId, startPage, endPage, size);

        return ResponseEntity.ok(
                ApiResponse.success(transactionService.getRecentTransactionsByUserId(userId, startPage, endPage, size),
                        "Recent transactions retrieved successfully", HttpStatus.OK.value())
        );
    }

    @GetMapping("/years")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get distinct years with transactions")
    public ResponseEntity<ApiResponse<List<Integer>>> getDistinctTransactionYears() {
        Integer userId = getAuthenticatedUserId();
        log.info("Fetching distinct transaction years for userId={}", userId);
        return ResponseEntity.ok(
                ApiResponse.success(transactionService.getDistinctTransactionYears(userId),
                        "Years retrieved successfully", HttpStatus.OK.value())
        );
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        Integer userId = getAuthenticatedUserId();
        transactionDTO.setUserId(userId);
        log.info("Creating transaction for userId={}", userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(transactionService.createTransaction(transactionDTO),
                        "Transaction created successfully", HttpStatus.CREATED.value())
        );
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update an existing transaction")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        Integer userId = getAuthenticatedUserId();
        transactionDTO.setUserId(userId);
        log.info("Updating transaction id={}", transactionDTO.getId());
        return ResponseEntity.ok(
                ApiResponse.success(transactionService.updateTransaction(transactionDTO),
                        "Transaction updated successfully", HttpStatus.OK.value())
        );
    }

    @DeleteMapping("/{transactionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete a transaction")
    public ResponseEntity<ApiResponse<Void>> deleteTransactionById(@PathVariable int transactionId) {
        Integer userId = getAuthenticatedUserId();
        log.info("Deleting transaction id={} for userId={}", transactionId, userId);
        transactionService.deleteTransactionById(transactionId);
        return ResponseEntity.ok(
                ApiResponse.success(null, "Transaction deleted successfully", HttpStatus.OK.value())
        );
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search transactions by criteria")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> searchTransactions(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount
    ) {
        Integer userId = getAuthenticatedUserId();
        log.info("Searching transactions for userId={}, categoryId={}, type={}, minAmount={}, maxAmount={}",
                userId, categoryId, transactionType, minAmount, maxAmount);

        return ResponseEntity.ok(
                ApiResponse.success(transactionService.searchTransactions(userId, categoryId, transactionType, minAmount, maxAmount),
                        "Search results retrieved successfully", HttpStatus.OK.value())
        );
    }

    @GetMapping("/breakdown")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get spending breakdown")
    public ResponseEntity<ApiResponse<SpendingBreakdownDTO>> getSpendingBreakdown() {
        Integer userId = getAuthenticatedUserId();
        log.info("Fetching spending breakdown for userId={}", userId);
        return ResponseEntity.ok(
                ApiResponse.success(transactionService.getSpendingBreakdown(userId),
                        "Spending breakdown retrieved successfully", HttpStatus.OK.value())
        );
    }
}