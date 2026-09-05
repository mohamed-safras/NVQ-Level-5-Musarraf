package com.library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a row of the Loans table.
 *
 * Deliberately holds no business policy (fine rate, loan period): a fixed
 * "Rs. 30/day, 14-day loan" rule baked into this class would mean every
 * policy change requires editing and re-testing the entity itself, which
 * breaks the Open/Closed Principle. Those rules now live in swappable
 * policy classes (see com.library.policy) that are injected into the
 * service layer instead. Loan itself only knows plain, non-negotiable
 * facts about its own state (is the due date in the past? has it been
 * returned?), which is why isOverdue()/daysOverdue() stay here.
 */
public class Loan {
    private int loanId;
    private final Book book;
    private final Member member;
    private final LocalDate loanDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private LoanStatus status;

    public Loan(int loanId, Book book, Member member, LocalDate loanDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.book = book;
        this.member = member;
        this.loanDate = loanDate;
        this.dueDate = dueDate;
        this.status = LoanStatus.ACTIVE;
    }

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public LocalDate getLoanDate() { return loanDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public LoanStatus getStatus() { return status; }

    public boolean isOverdue(LocalDate asOf) {
        return status != LoanStatus.RETURNED && asOf.isAfter(dueDate);
    }

    public long daysOverdue(LocalDate asOf) {
        return isOverdue(asOf) ? ChronoUnit.DAYS.between(dueDate, asOf) : 0;
    }

    public void markReturned(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.status = LoanStatus.RETURNED;
        book.returnCopy();
    }

    public void refreshStatus(LocalDate today) {
        if (status == LoanStatus.ACTIVE && today.isAfter(dueDate)) {
            status = LoanStatus.OVERDUE;
        }
    }

    @Override
    public String toString() {
        return String.format("[%d] %-25s -> %-25s due:%s status:%s",
                loanId, member.getFullName(), book.getTitle(), dueDate, status);
    }
}
