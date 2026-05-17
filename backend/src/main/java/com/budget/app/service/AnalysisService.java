package com.budget.app.service;

import com.budget.app.dto.AnalysisResponse;
import com.budget.app.dto.CategoryStat;
import com.budget.app.entity.Transaction;
import com.budget.app.entity.Transaction.TransactionType;
import com.budget.app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public AnalysisResponse analyze(Long userId, int year, int month) {
        List<Transaction> txList = transactionRepository.findByUserIdAndYearMonth(userId, year, month);

        long totalIncome  = txList.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .mapToLong(Transaction::getAmount).sum();
        long totalExpense = txList.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .mapToLong(Transaction::getAmount).sum();

        // 카테고리별 지출 집계
        Map<String, Long> categorySum = txList.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .collect(Collectors.groupingBy(
                t -> {
                    String cat = t.getEffectiveCategory() != null
                        ? t.getEffectiveCategory().getName() : "미분류";
                    return cat;
                },
                Collectors.summingLong(Transaction::getAmount)
            ));

        List<CategoryStat> categoryStats = categorySum.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .map(e -> CategoryStat.builder()
                .categoryName(e.getKey())
                .amount(e.getValue())
                .percentage(totalExpense > 0
                    ? Math.round((double) e.getValue() / totalExpense * 1000) / 10.0
                    : 0.0)
                .build())
            .collect(Collectors.toList());

        // 간단한 인사이트 생성
        List<String> insights = generateInsights(categoryStats, totalIncome, totalExpense);

        return AnalysisResponse.builder()
            .year(year)
            .month(month)
            .totalIncome(totalIncome)
            .totalExpense(totalExpense)
            .netAmount(totalIncome - totalExpense)
            .categoryStats(categoryStats)
            .insights(insights)
            .build();
    }

    private List<String> generateInsights(List<CategoryStat> stats, long income, long expense) {
        List<String> insights = new ArrayList<>();

        if (income > 0 && expense > 0) {
            double savingRate = (double)(income - expense) / income * 100;
            if (savingRate > 30) {
                insights.add(String.format("이번 달 저축률이 %.1f%%로 매우 우수합니다!", savingRate));
            } else if (savingRate < 0) {
                insights.add(String.format("이번 달 지출이 수입을 %,d원 초과했습니다. 지출을 줄여보세요.", expense - income));
            } else {
                insights.add(String.format("이번 달 저축률은 %.1f%%입니다.", savingRate));
            }
        }

        if (!stats.isEmpty()) {
            CategoryStat top = stats.get(0);
            insights.add(String.format("가장 많이 지출한 카테고리는 '%s'로 전체의 %.1f%%를 차지합니다.",
                top.getCategoryName(), top.getPercentage()));
        }

        return insights;
    }
}
