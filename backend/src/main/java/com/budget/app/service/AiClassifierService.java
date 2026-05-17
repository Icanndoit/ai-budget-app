package com.budget.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiClassifierService {

    private final RestTemplate restTemplate;

    @Value("${ai.module.url:http://localhost:5000}")
    private String aiModuleUrl;

    /**
     * 거래 설명 텍스트를 AI 모듈에 전송하여 카테고리를 분류받는다.
     * AI 모듈이 응답하지 않을 경우 "기타"로 기본 분류한다.
     */
    @SuppressWarnings("unchecked")
    public String classify(String memo, Long amount, String type) {
        try {
            Map<String, Object> request = Map.of(
                "memo",   memo   != null ? memo : "",
                "amount", amount != null ? amount : 0L,
                "type",   type   != null ? type : "EXPENSE"
            );

            Map<String, Object> response = restTemplate.postForObject(
                aiModuleUrl + "/classify", request, Map.class
            );

            if (response != null && response.containsKey("category")) {
                String category = (String) response.get("category");
                double confidence = response.containsKey("confidence")
                    ? ((Number) response.get("confidence")).doubleValue() : 0.0;
                log.info("AI 분류 결과: memo='{}' → category='{}' (confidence={:.2f})", memo, category, confidence);
                return category;
            }
        } catch (Exception e) {
            log.warn("AI 분류 모듈 연결 실패, 기본값 사용: {}", e.getMessage());
        }
        return "기타";
    }
}
