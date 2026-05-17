package com.budget.app.repository;

import com.budget.app.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndYearAndMonth(Long userId, int year, int month);

    Optional<Budget> findByUserIdAndCategoryIdAndYearAndMonth(
        Long userId, Long categoryId, int year, int month);

    @Query("""
        SELECT b FROM Budget b
        WHERE b.user.id = :userId
          AND b.year = :year
          AND b.month = :month
        """)
    List<Budget> findMonthlyBudgets(
        @Param("userId") Long userId,
        @Param("year")   int year,
        @Param("month")  int month
    );
}
