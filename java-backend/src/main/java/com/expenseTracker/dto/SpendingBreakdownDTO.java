package com.expenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingBreakdownDTO {
    private BigDecimal needsAmount;
    private BigDecimal wantsAmount;
    private BigDecimal investmentAmount;
    private BigDecimal totalSpent;
    private BigDecimal totalIncome;
    private BigDecimal savingsAmount;
    
    // Percentages
    private Double needsPercentage;
    private Double wantsPercentage;
    private Double investmentPercentage;
    private Double savingsPercentage;
}
