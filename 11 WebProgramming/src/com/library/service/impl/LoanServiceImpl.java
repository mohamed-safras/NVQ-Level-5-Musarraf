package com.library.service.impl;

import com.library.exception.RecordNotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.Member;
import com.library.policy.FineCalculator;
import com.library.policy.LoanEligibilityPolicy;
import com.library.policy.LoanPolicy;
import com.library.repository.LoanRepository;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;

import java.time.LocalDate;
import java.util.List;

/**
 * Every dependency here is an interface — LoanRepository, MemberService,
 * BookService, LoanPolicy, LoanEligibilityPolicy, FineCalculator — and
 * every one is supplied through the constructor rather than created
 * internally (Dependency Inversion + constructor injection). Nothing in
 * this class needs to change to support a different fine formula, a
 * different loan period, a stricter eligibility rule, or a JDBC-backed
 * repository: only the composition root (ApiServer) that wires the
 * concrete classes together would change.
 */
public class LoanServiceImpl implements LoanService {
    private final LoanRepository loanRepository;
    private final MemberService memberService;
    private final BookService bookService;
    private final LoanPolicy loanPolicy;
    private final LoanEligibilityPolicy eligibilityPolicy;
    private final FineCalculator fineCalculator;

    public LoanServiceImpl(LoanRepository loanRepository, MemberService memberService, BookService bookService,
                            LoanPolicy loanPolicy, LoanEligibilityPolicy eligibilityPolicy, FineCalculator fineCalculator) {
        this.loanRepository = loanRepository;
        this.memberService = memberService;
        this.bookService = bookService;
        this.loanPolicy = loanPolicy;
        this.eligibilityPolicy = eligibilityPolicy;
        this.fineCalculator = fineCalculator;
    }

    @Override
    public Loan issueLoan(int memberId, int bookId) {
        Member member = memberService.findMember(memberId);
        Book book = bookService.findBook(bookId);

        eligibilityPolicy.checkEligible(member, book);

        book.borrowCopy();
        LocalDate today = LocalDate.now();
        Loan loan = new Loan(0, book, member, today, loanPolicy.calculateDueDate(today));
        return loanRepository.add(loan);
    }

    @Override
    public double returnLoan(int loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RecordNotFoundException("Loan " + loanId + " not found."));
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new IllegalStateException("Loan " + loanId + " was already returned.");
        }
        LocalDate today = LocalDate.now();
        double fine = fineCalculator.calculateFine(loan, today);
        loan.markReturned(today);
        loanRepository.update(loan);
        return fine;
    }

    @Override
    public List<Loan> listOverdueLoans() {
        LocalDate today = LocalDate.now();
        loanRepository.findAll().forEach(l -> l.refreshStatus(today));
        return loanRepository.findAll().stream()
                .filter(l -> l.isOverdue(today))
                .toList();
    }

    @Override
    public List<Loan> listAllLoans() {
        return loanRepository.findAll();
    }
}
