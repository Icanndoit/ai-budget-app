package com.budget.app.controller;

import com.budget.app.dto.BudgetRequest;
import com.budget.app.dto.BudgetStatusResponse;
import com.budget.app.entity.Budget;
import com.budget.app.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Budget", description = "예산 관리 API")
@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(summary = "예산 설정 (생성 또는 수정)")
    @PostMapping
    public ResponseEntity<Budget> createOrUpdate(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody BudgetRequest req) {
        return ResponseEntity.ok(budgetService.createOrUpdate(userId, req));
    }

    @Operation(summary = "월별 예산 현황 조회")
    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusResponse>> getStatus(
            @AuthenticationPrincipal Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(budgetService.getMonthlyStatus(userId, year, month));
    }

    @Operation(summary = "예산 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        budgetService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
