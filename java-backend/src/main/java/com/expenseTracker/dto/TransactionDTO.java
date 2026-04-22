package com.expenseTracker.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Integer id;

    @NotNull(message = "Category ID is required")
    private Integer categoryId;

    private String categoryName;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Transaction name is required")
    @Size(min = 3, max = 255, message = "Transaction name must be between 3 and 255 characters")
    private String transactionName;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.01", message = "Transaction amount must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Transaction amount cannot exceed 999999.99")
    private BigDecimal transactionAmount;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "INCOME|EXPENSE-NEED|EXPENSE-WANT|EXPENSE-INVESTMENT", message = "Transaction type must be INCOME, EXPENSE-NEED, EXPENSE-WANT, or EXPENSE-INVESTMENT")
    private String transactionType;
}