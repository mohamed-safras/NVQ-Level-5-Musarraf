package com.library.policy;

import java.time.LocalDate;

/**
 * Decides how long a loan period is. Extracted as its own interface so a
 * new policy (e.g. a longer period for staff, a shorter one for reference-
 * only titles) can be added as a brand new class — LoanServiceImpl and
 * Loan never need to change. This is the Open/Closed Principle in
 * practice: the system is open to new loan-period rules, closed to
 * modification of the classes that already work.
 */
public interface LoanPolicy {
    LocalDate calculateDueDate(LocalDate loanDate);
}
