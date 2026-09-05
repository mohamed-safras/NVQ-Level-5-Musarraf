package com.library.repository;

import com.library.model.Loan;

import java.util.List;

public interface LoanRepository extends Repository<Loan, Integer> {
    List<Loan> findActiveByMember(int memberId);
}
