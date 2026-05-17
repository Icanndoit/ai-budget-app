package com.budget.app.repository;

import com.budget.app.entity.Transaction;
import com.budget.app.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate   IS NULL OR t.date <= :endDate)
          AND (:type      IS NULL OR t.type = :type)
          AND (:categoryId IS NULL
               OR t.userCategory.id = :categoryId
               OR (t.userCategory IS NULL AND t.aiCategory.id = :categoryId))
          AND (:keyword   IS NULL OR t.memo LIKE %:keyword%
               OR t.description LIKE %:keyword%)
        ORDER BY t.date DESC
        """)
    Page<Transaction> findWithFilters(
        @Param("userId")     Long userId,
        @Param("startDate")  LocalDate startDate,
        @Param("endDate")    LocalDate endDate,
        @Param("type")       TransactionType type,
        @Param("categoryId") Long categoryId,
        @Param("keyword")    String keyword,
        Pageable pageable
    );

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
          AND FUNCTION('YEAR',  t.date) = :year
          AND FUNCTION('MONTH', t.date) = :month
        """)
    List<Transaction> findByUserIdAndYearMonth(
        @Param("userId") Long userId,
        @Param("year")   int year,
        @Param("month")  int month
    );

    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
        WHERE t.user.id = :userId
          AND t.type = :type
          AND FUNCTION('YEAR',  t.date) = :year
          AND FUNCTION('MONTH', t.date) = :month
          AND (:categoryId IS NULL
               OR t.userCategory.id = :categoryId
               OR (t.userCategory IS NULL AND t.aiCategory.id = :categoryId))
        """)
    Long sumByUserIdTypeYearMonthCategory(
        @Param("userId")     Long userId,
        @Param("type")       TransactionType type,
        @Param("year")       int year,
        @Param("month")      int month,
        @Param("categoryId") Long categoryId
    );
}
