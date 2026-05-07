package com.yucairoad.service;

import com.yucairoad.dto.FinancialReport;

public interface FinanceService {

    FinancialReport calculateMonthlyIncome(Long saveId);

    FinancialReport calculateMonthlyExpense(Long saveId);

    FinancialReport getFinancialReport(Long saveId);

    FinancialReport processMonthlySettlement(Long saveId);
}
