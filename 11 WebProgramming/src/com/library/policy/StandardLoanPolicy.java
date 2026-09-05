package com.library.policy;

import java.time.LocalDate;

/** Default policy: every loan runs for a fixed number of days. */
public class StandardLoanPolicy implements LoanPolicy {
    private final int loanPeriodDays;

    public StandardLoanPolicy() {
        this(14);
    }

    public StandardLoanPolicy(int loanPeriodDays) {
        this.loanPeriodDays = loanPeriodDays;
    }

    @Override
    public LocalDate calculateDueDate(LocalDate loanDate) {
        return loanDate.plusDays(loanPeriodDays);
    }
}
