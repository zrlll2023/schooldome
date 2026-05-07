package com.yucairoad.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class FinancialReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal monthlyIncome;

    private BigDecimal monthlyExpense;

    private BigDecimal netProfit;

    private BigDecimal balance;

    private IncomeBreakdown incomeBreakdown;

    private ExpenseBreakdown expenseBreakdown;

    public FinancialReport() {
        this.incomeBreakdown = new IncomeBreakdown();
        this.expenseBreakdown = new ExpenseBreakdown();
    }

    @Data
    public static class IncomeBreakdown implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigDecimal tuition;
        private BigDecimal governmentGrant;
        private BigDecimal donation;
        private BigDecimal cooperation;
        private BigDecimal branchRemittance;
    }

    @Data
    public static class ExpenseBreakdown implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigDecimal teacherSalary;
        private BigDecimal studentSubsidy;
        private BigDecimal buildingMaintenance;
        private BigDecimal activities;
        private BigDecimal loanInterest;
    }
}
