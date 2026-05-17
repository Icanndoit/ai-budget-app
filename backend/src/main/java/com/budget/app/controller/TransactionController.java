package com.budget.app.controller;

import com.budget.app.dto.TransactionRequest;
import com.budget.app.dto.TransactionResponse;
import com.budget.app.entity.Transaction.TransactionType;
import com.budget.app.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Transactions", description = "거래 관리 API")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "거래 추가 (AI 자동 분류 포함)")
    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody TransactionRequest req) {
        TransactionResponse response = transactionService.create(userId, req);
        return ResponseEntity
            .created(URI.create("/api/transactions/" + response.getId()))
            .body(response);
    }

    @Operation(summary = "거래 목록 조회 (필터/페이징)")
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
            transactionService.getList(userId, startDate, endDate, type, categoryId, keyword, pageable)
        );
    }

    @Operation(summary = "거래 수정")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest req) {
        return ResponseEntity.ok(transactionService.update(userId, id, req));
    }

    @Operation(summary = "거래 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        transactionService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
