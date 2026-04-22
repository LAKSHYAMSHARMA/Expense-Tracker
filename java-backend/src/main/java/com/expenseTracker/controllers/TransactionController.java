package com.expenseTracker.controllers;

import com.expenseTracker.dto.TransactionDTO;
import com.expenseTracker.services.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getAllTransactionsByUserIdAndYear(@PathVariable int userId,
                                                                                 @RequestParam int year,
                                                                                 @RequestParam(required = false) Integer month) {
        log.info("Fetching transactions for userId={}, year={}, month={}", userId, year, month);

        List<TransactionDTO> transactionsList = (month == null)
                ? transactionService.getAllTransactionsByUserIdAndYear(userId, year)
                : transactionService.getAllTransactionsByUserIdAndYearAndMonth(userId, year, month);

        return ResponseEntity.ok(transactionsList);
    }

    @GetMapping("/recent/user/{userId}")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactionsByUserId(
            @PathVariable int userId,
            @RequestParam int startPage,
            @RequestParam int endPage,
            @RequestParam int size
    ) {
        log.info("Fetching recent transactions for userId={}, startPage={}, endPage={}, size={}",
                userId, startPage, endPage, size);

        return ResponseEntity.ok(
                transactionService.getRecentTransactionsByUserId(userId, startPage, endPage, size)
        );
    }

    @GetMapping("/years/{userId}")
    public ResponseEntity<List<Integer>> getDistinctTransactionYears(@PathVariable int userId) {
        log.info("Fetching distinct transaction years for userId={}", userId);
        return ResponseEntity.ok(transactionService.getDistinctTransactionYears(userId));
    }

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        log.info("Creating transaction for userId={}", transactionDTO.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(transactionDTO));
    }

    @PutMapping
    public ResponseEntity<TransactionDTO> updateTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        log.info("Updating transaction id={}", transactionDTO.getId());
        return ResponseEntity.ok(transactionService.updateTransaction(transactionDTO));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransactionById(@PathVariable int transactionId) {
        log.info("Deleting transaction id={}", transactionId);
        transactionService.deleteTransactionById(transactionId);
        return ResponseEntity.noContent().build();
    }
}