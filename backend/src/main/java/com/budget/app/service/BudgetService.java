package com.budget.app.service;

import com.budget.app.dto.BudgetRequest;
import com.budget.app.dto.BudgetStatusResponse;
import com.budget.app.entity.Budget;
import com.budget.app.entity.Category;
import com.budget.app.entity.Transaction.TransactionType;
import com.budget.app.entity.User;
import com.budget.app.repository.BudgetRepository;
import com.budget.app.repository.CategoryRepository;
import com.budget.app.repository.TransactionRepository;
import com.budget.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Budget createOrUpdate(Long userId, BudgetRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        Category category = categoryRepository.findById(req.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        Budget budget = budgetRepository
            .findByUserIdAndCategoryIdAndYearAndMonth(userId, req.getCategoryId(), req.getYear(), req.getMonth())
            .orElse(Budget.builder().user(user).category(category).year(req.getYear()).month(req.getMonth()).build());

        budget.setAmount(req.getAmount());
        return budgetRepository.save(budget);
    }

    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> getMonthlyStatus(Long userId, int year, int month) {
        List<Budget> budgets = budgetRepository.findMonthlyBudgets(userId, year, month);

        return budgets.stream().map(budget -> {
            Long spent = transactionRepository.sumByUserIdTypeYearMonthCategory(
                userId, TransactionType.EXPENSE, year, month, budget.getCategory().getId());
            spent = spent != null ? spent : 0L;

            double usageRate = budget.getAmount() > 0
                ? (double) spent / budget.getAmount() * 100 : 0;

            return BudgetStatusResponse.builder()
                .budgetId(budget.getId())
                .categoryName(budget.getCategory().getName())
                .budgetAmount(budget.getAmount())
                .spentAmount(spent)
                .usageRate(Math.round(usageRate * 10) / 10.0)
                .isOverBudget(spent > budget.getAmount())
                .build();
        }).collect(Collectors.toList());
    }

    /**
     * 거래 추가 후 예산 초과 여부를 확인하고 로그를 남긴다.
     * (실제 서비스에서는 WebSocket/SSE로 프론트에 푸시)
     */
    public void checkBudgetAlert(Long userId, Long categoryId, int year, int month) {
        budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(userId, categoryId, year, month)
            .ifPresent(budget -> {
                Long spent = transactionRepository.sumByUserIdTypeYearMonthCategory(
                    userId, TransactionType.EXPENSE, year, month, categoryId);
                spent = spent != null ? spent : 0L;

                double rate = (double) spent / budget.getAmount() * 100;
                if (rate >= 100) {
                    log.warn("[예산 초과 알림] userId={}, category={}, {}%", userId, budget.getCategory().getName(), (int)rate);
                } else if (rate >= 80) {
                    log.info("[예산 80% 알림] userId={}, category={}, {}%", userId, budget.getCategory().getName(), (int)rate);
                }
            });
    }

    @Transactional
    public void delete(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예산입니다."));
        if (!budget.getUser().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        budgetRepository.delete(budget);
    }
}
