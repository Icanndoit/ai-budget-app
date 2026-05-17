package com.budget.app.dto;

import com.budget.app.entity.Transaction;
import com.budget.app.entity.Transaction.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

// ── TransactionRequest ──────────────────────────────────────────────────────
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class TransactionRequest {
    @NotNull @Positive
    private Long amount;

    @NotNull
    private TransactionType type;

    @NotNull
    private LocalDate date;

    private String description;
    private String memo;
    private Long categoryId; // 사용자가 직접 지정할 경우 (수정 시)
}

// ── TransactionResponse ─────────────────────────────────────────────────────
@Getter @Builder
class TransactionResponse {
    private Long id;
    private Long amount;
    private TransactionType type;
    private LocalDate date;
    private String description;
    private String memo;
    private String aiCategory;
    private String userCategory;
    private String effectiveCategory;

    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
            .id(tx.getId())
            .amount(tx.getAmount())
            .type(tx.getType())
            .date(tx.getDate())
            .description(tx.getDescription())
            .memo(tx.getMemo())
            .aiCategory(tx.getAiCategory() != null ? tx.getAiCategory().getName() : null)
            .userCategory(tx.getUserCategory() != null ? tx.getUserCategory().getName() : null)
            .effectiveCategory(tx.getEffectiveCategory() != null ? tx.getEffectiveCategory().getName() : "미분류")
            .build();
    }
}

// ── BudgetRequest ───────────────────────────────────────────────────────────
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class BudgetRequest {
    @NotNull
    private Long categoryId;

    @NotNull @Positive
    private Long amount;

    @NotNull
    private Integer year;

    @NotNull
    private Integer month;
}

// ── BudgetStatusResponse ────────────────────────────────────────────────────
@Getter @Builder
class BudgetStatusResponse {
    private Long budgetId;
    private String categoryName;
    private Long budgetAmount;
    private Long spentAmount;
    private Double usageRate;
    private Boolean isOverBudget;
}

// ── CategoryStat ────────────────────────────────────────────────────────────
@Getter @Builder
class CategoryStat {
    private String categoryName;
    private Long amount;
    private Double percentage;
}

// ── AnalysisResponse ────────────────────────────────────────────────────────
@Getter @Builder
class AnalysisResponse {
    private int year;
    private int month;
    private Long totalIncome;
    private Long totalExpense;
    private Long netAmount;
    private java.util.List<CategoryStat> categoryStats;
    private java.util.List<String> insights;
}
