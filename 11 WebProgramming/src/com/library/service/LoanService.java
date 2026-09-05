package com.library.service;

import com.library.model.Loan;

import java.util.List;

public interface LoanService {
    Loan issueLoan(int memberId, int bookId);
    double returnLoan(int loanId);
    List<Loan> listOverdueLoans();
    List<Loan> listAllLoans();
}
