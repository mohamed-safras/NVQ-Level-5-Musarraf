package com.library.policy;

import com.library.model.Loan;

import java.time.LocalDate;

/** Default policy: a flat rate per day overdue (Rs. 30/day). */
public class DailyRateFineCalculator implements FineCalculator {
    private final double ratePerDay;

    public DailyRateFineCalculator() {
        this(30.0);
    }

    public DailyRateFineCalculator(double ratePerDay) {
        this.ratePerDay = ratePerDay;
    }

    @Override
    public double calculateFine(Loan loan, LocalDate asOf) {
        return loan.daysOverdue(asOf) * ratePerDay;
    }
}
