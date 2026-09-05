package com.library.policy;

import com.library.model.Loan;

import java.time.LocalDate;

/**
 * Decides how much a fine is for a given loan. The Database Systems
 * schema's Fines table only records reason/amount — it makes no
 * assumption about how that amount was derived, which is exactly what
 * lets this be swapped later (e.g. a capped fine, a tiered rate that
 * increases after a week) without touching Loan or LoanServiceImpl.
 */
public interface FineCalculator {
    double calculateFine(Loan loan, LocalDate asOf);
}
