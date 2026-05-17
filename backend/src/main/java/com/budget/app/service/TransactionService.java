package com.budget.app.service;

import com.budget.app.dto.TransactionRequest;
import com.budget.app.dto.TransactionResponse;
import com.budget.app.entity.Category;
import com.budget.app.entity.Transaction;
import com.budget.app.entity.Transaction.TransactionType;
import com.budget.app.entity.User;
import com.budget.app.repository.CategoryRepository;
import com.budget.app.repository.TransactionRepository;
import com.budget.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AiClassifierService aiClassifierService;
    private final BudgetService budgetService;

    @Transactional
    public TransactionResponse create(Long userId, TransactionRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // AI 자동 분류
        String aiCategoryName = aiClassifierService.classify(req.getMemo(), req.getAmount(), req.getType().name());
        Category aiCategory = categoryRepository.findByName(aiCategoryName).orElse(null);

        Transaction tx = Transaction.builder()
            .user(user)
            .amount(req.getAmount())
            .type(req.getType())
            .date(req.getDate())
            .description(req.getDescription())
            .memo(req.getMemo())
            .aiCategory(aiCategory)
            .build();

        Transaction saved = transactionRepository.save(tx);

        // 예산 초과 알림 체크
        if (req.getType() == TransactionType.EXPENSE && aiCategory != null) {
            budgetService.checkBudgetAlert(userId, aiCategory.getId(), req.getDate().getYear(), req.getDate().getMonthValue());
        }

        log.info("거래 추가 - userId={}, amount={}, aiCategory={}", userId, req.getAmount(), aiCategoryName);
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getList(Long userId, LocalDate startDate, LocalDate endDate,
                                              TransactionType type, Long categoryId, String keyword, Pageable pageable) {
        return transactionRepository
            .findWithFilters(userId, startDate, endDate, type, categoryId, keyword, pageable)
            .map(TransactionResponse::from);
    }

    @Transactional
    public TransactionResponse update(Long userId, Long txId, TransactionRequest req) {
        Transaction tx = transactionRepository.findById(txId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 거래입니다."));

        if (!tx.getUser().getId().equals(userId)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        // 사용자가 카테고리를 직접 수정한 경우
        if (req.getCategoryId() != null) {
            Category userCategory = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
            tx.setUserCategory(userCategory);
        }

        tx.setAmount(req.getAmount());
        tx.setType(req.getType());
        tx.setDate(req.getDate());
        tx.setDescription(req.getDescription());
        tx.setMemo(req.getMemo());

        return TransactionResponse.from(transactionRepository.save(tx));
    }

    @Transactional
    public void delete(Long userId, Long txId) {
        Transaction tx = transactionRepository.findById(txId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 거래입니다."));

        if (!tx.getUser().getId().equals(userId)) {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
        transactionRepository.delete(tx);
    }
}
