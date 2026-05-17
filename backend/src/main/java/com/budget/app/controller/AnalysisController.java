package com.budget.app.controller;

import com.budget.app.dto.AnalysisResponse;
import com.budget.app.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Analysis", description = "소비 분석 API")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @Operation(summary = "월별 소비 분석 조회")
    @GetMapping
    public ResponseEntity<AnalysisResponse> analyze(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getYear()}") int year,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int month) {
        return ResponseEntity.ok(analysisService.analyze(userId, year, month));
    }
}
