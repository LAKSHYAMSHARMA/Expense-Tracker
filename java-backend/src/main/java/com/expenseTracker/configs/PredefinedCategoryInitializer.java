package com.expenseTracker.configs;

import com.expenseTracker.services.TransactionCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PredefinedCategoryInitializer implements CommandLineRunner {
    private final TransactionCategoryService transactionCategoryService;

    @Override
    public void run(String... args) {
        transactionCategoryService.initializePredefinedCategories();
    }
}