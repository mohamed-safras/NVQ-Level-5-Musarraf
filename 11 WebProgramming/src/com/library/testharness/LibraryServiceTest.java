package com.library.testharness;

import com.library.exception.RecordNotFoundException;
import com.library.model.*;
import com.library.policy.DailyRateFineCalculator;
import com.library.policy.StandardEligibilityPolicy;
import com.library.policy.StandardLoanPolicy;
import com.library.repository.memory.InMemoryBookRepository;
import com.library.repository.memory.InMemoryLoanRepository;
import com.library.repository.memory.InMemoryMemberRepository;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import com.library.service.impl.BookServiceImpl;
import com.library.service.impl.LoanServiceImpl;
import com.library.service.impl.MemberServiceImpl;

import java.time.LocalDate;

import static com.library.testharness.MiniTest.*;

/**
 * Test suite for the Software Testing unit, updated for the SOLID-refactored
 * service layer. Each test wires a fresh set of in-memory repositories and
 * services (mirroring ApiServer's composition root) so cases never share state.
 */
public class LibraryServiceTest {

    /** A small bundle of freshly-wired services, built the same way ApiServer wires them. */
    private static class Services {
        final MemberService members;
        final BookService books;
        final LoanService loans;

        Services() {
            InMemoryMemberRepository memberRepo = new InMemoryMemberRepository();
            InMemoryBookRepository bookRepo = new InMemoryBookRepository();
            InMemoryLoanRepository loanRepo = new InMemoryLoanRepository();
            this.members = new MemberServiceImpl(memberRepo, loanRepo);
            this.books = new BookServiceImpl(bookRepo);
            this.loans = new LoanServiceImpl(loanRepo, members, books,
                    new StandardLoanPolicy(), new StandardEligibilityPolicy(), new DailyRateFineCalculator());
        }
    }

    private static Services fresh() { return new Services(); }

    public static void main(String[] args) {

        // ---------------- Member CRUD ----------------
        run("registerMember: valid data returns a persisted member with generated ID", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Nadeesha", "Perera", "nadeesha@mail.com", "0771234561");
            assertTrue(m.getMemberId() > 0, "member id should be auto-generated");
            assertEquals("Nadeesha Perera", m.getFullName(), "full name should combine first+last");
        });

        run("registerMember: invalid email is rejected", () -> {
            Services s = fresh();
            assertThrows(IllegalArgumentException.class,
                    () -> s.members.registerMember("Kasun", "Fernando", "not-an-email", "0771234562"),
                    "should reject an email without '@'");
        });

        run("findMember: unknown ID throws RecordNotFoundException", () -> {
            Services s = fresh();
            assertThrows(RecordNotFoundException.class, () -> s.members.findMember(999),
                    "should throw when member does not exist");
        });

        run("updateMemberContact: updates phone and email", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Ishara", "Silva", "ishara@mail.com", "0771111111");
            s.members.updateMemberContact(m.getMemberId(), "0772222222", "ishara.new@mail.com");
            Member updated = s.members.findMember(m.getMemberId());
            assertEquals("0772222222", updated.getPhone(), "phone should be updated");
            assertEquals("ishara.new@mail.com", updated.getEmail(), "email should be updated");
        });

        run("deleteMember: member with no active loans is deleted", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Ruwan", "Jaya", "ruwan@mail.com", "0773333333");
            assertTrue(s.members.deleteMember(m.getMemberId()), "delete should succeed");
            assertThrows(RecordNotFoundException.class, () -> s.members.findMember(m.getMemberId()),
                    "member should no longer be found");
        });

        run("deleteMember: member with an active loan cannot be deleted", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Dilani", "Ratna", "dilani@mail.com", "0774444444");
            Book b = s.books.addBook("978-1", "Test Book", "Fiction", "Some Author", 1);
            s.loans.issueLoan(m.getMemberId(), b.getBookId());
            assertThrows(IllegalStateException.class, () -> s.members.deleteMember(m.getMemberId()),
                    "should refuse to delete a member with an active loan");
        });

        // ---------------- Book CRUD ----------------
        run("addBook: zero copies is rejected", () -> {
            Services s = fresh();
            assertThrows(IllegalArgumentException.class,
                    () -> s.books.addBook("978-2", "Bad Book", "Fiction", "Nobody", 0),
                    "a book must have at least one copy");
        });

        run("searchBooks: matches by title or author, case-insensitively", () -> {
            Services s = fresh();
            s.books.addBook("978-3", "Clean Code", "Computer Science", "Robert Martin", 2);
            s.books.addBook("978-4", "1984", "Fiction", "George Orwell", 1);
            assertEquals(1, s.books.searchBooks("clean").size(), "should match title case-insensitively");
            assertEquals(1, s.books.searchBooks("ORWELL").size(), "should match author case-insensitively");
            assertEquals(0, s.books.searchBooks("nonexistent").size(), "should return no matches for unrelated keyword");
        });

        run("deleteBook: cannot delete while a copy is on loan", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Chamara", "Weera", "chamara@mail.com", "0775555555");
            Book b = s.books.addBook("978-5", "On Loan Book", "Fiction", "Author X", 1);
            s.loans.issueLoan(m.getMemberId(), b.getBookId());
            assertThrows(IllegalStateException.class, () -> s.books.deleteBook(b.getBookId()),
                    "should refuse to delete a book while copies are on loan");
        });

        // ---------------- Loan operations ----------------
        run("issueLoan: reduces available copies by one", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Sanduni", "Guna", "sanduni@mail.com", "0776666666");
            Book b = s.books.addBook("978-6", "Two Copies", "Science", "Author Y", 2);
            s.loans.issueLoan(m.getMemberId(), b.getBookId());
            assertEquals(1, s.books.findBook(b.getBookId()).getAvailableCopies(), "one copy should now be on loan");
        });

        run("issueLoan: fails when no copies are available", () -> {
            Services s = fresh();
            Member m1 = s.members.registerMember("A", "One", "a1@mail.com", "011");
            Member m2 = s.members.registerMember("B", "Two", "b2@mail.com", "022");
            Book b = s.books.addBook("978-7", "Only Copy", "Fiction", "Author Z", 1);
            s.loans.issueLoan(m1.getMemberId(), b.getBookId());
            assertThrows(IllegalStateException.class, () -> s.loans.issueLoan(m2.getMemberId(), b.getBookId()),
                    "second member should not be able to borrow the last copy");
        });

        run("issueLoan: suspended member cannot borrow", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Tharindu", "Bandara", "tharindu@mail.com", "033");
            m.setStatus(MembershipStatus.SUSPENDED);
            Book b = s.books.addBook("978-8", "Some Title", "Fiction", "Author W", 1);
            assertThrows(IllegalStateException.class, () -> s.loans.issueLoan(m.getMemberId(), b.getBookId()),
                    "a suspended member must not be able to borrow");
        });

        run("returnLoan: returning on time produces zero fine and frees the copy", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Priya", "K", "priya@mail.com", "044");
            Book b = s.books.addBook("978-9", "Prompt Return", "Fiction", "Author V", 1);
            var loan = s.loans.issueLoan(m.getMemberId(), b.getBookId());
            double fine = s.loans.returnLoan(loan.getLoanId());
            assertEquals(0.0, fine, "on-time return should incur no fine");
            assertEquals(1, s.books.findBook(b.getBookId()).getAvailableCopies(), "copy should be available again");
        });

        run("returnLoan: already-returned loan cannot be returned again", () -> {
            Services s = fresh();
            Member m = s.members.registerMember("Nimal", "Perera", "nimal@mail.com", "055");
            Book b = s.books.addBook("978-10", "Book", "Fiction", "Author U", 1);
            var loan = s.loans.issueLoan(m.getMemberId(), b.getBookId());
            s.loans.returnLoan(loan.getLoanId());
            assertThrows(IllegalStateException.class, () -> s.loans.returnLoan(loan.getLoanId()),
                    "returning the same loan twice should be rejected");
        });

        // ---------------- Policy classes, tested directly ----------------
        run("StandardLoanPolicy: due date is loanDate + 14 days by default", () -> {
            LocalDate loanDate = LocalDate.of(2026, 1, 1);
            LocalDate due = new StandardLoanPolicy().calculateDueDate(loanDate);
            assertEquals(LocalDate.of(2026, 1, 15), due, "default loan period should be 14 days");
        });

        run("StandardLoanPolicy: custom loan period is honoured", () -> {
            LocalDate loanDate = LocalDate.of(2026, 1, 1);
            LocalDate due = new StandardLoanPolicy(7).calculateDueDate(loanDate);
            assertEquals(LocalDate.of(2026, 1, 8), due, "custom 7-day loan period should be used");
        });

        run("DailyRateFineCalculator: Rs.30/day is charged only for days overdue", () -> {
            Member m = new Member(1, "Test", "User", "t@mail.com", "000", LocalDate.now(), MembershipStatus.ACTIVE);
            Book b = new Book(1, "978-x", "Overdue Book", "Fiction", "Author", 1);
            LocalDate loanDate = LocalDate.now().minusDays(20);
            LocalDate dueDate = loanDate.plusDays(14); // 6 days ago
            Loan loan = new Loan(1, b, m, loanDate, dueDate);
            LocalDate today = LocalDate.now();
            assertTrue(loan.isOverdue(today), "loan should be flagged overdue");
            assertEquals(6L, loan.daysOverdue(today), "should be exactly 6 days overdue");
            assertEquals(180.0, new DailyRateFineCalculator().calculateFine(loan, today), "fine should be 6 x Rs.30 = Rs.180");
        });

        run("DailyRateFineCalculator: no fine before the due date", () -> {
            Member m = new Member(1, "Test", "User", "t@mail.com", "000", LocalDate.now(), MembershipStatus.ACTIVE);
            Book b = new Book(1, "978-y", "Fresh Loan", "Fiction", "Author", 1);
            LocalDate today = LocalDate.now();
            Loan loan = new Loan(1, b, m, today, today.plusDays(14));
            assertTrue(!loan.isOverdue(today), "a brand-new loan should not be overdue");
            assertEquals(0.0, new DailyRateFineCalculator().calculateFine(loan, today), "no fine should apply before the due date");
        });

        run("DailyRateFineCalculator: a custom rate is honoured (proves OCP — no Loan/service change needed)", () -> {
            Member m = new Member(1, "Test", "User", "t@mail.com", "000", LocalDate.now(), MembershipStatus.ACTIVE);
            Book b = new Book(1, "978-z", "Overdue Book", "Fiction", "Author", 1);
            LocalDate loanDate = LocalDate.now().minusDays(20);
            LocalDate dueDate = loanDate.plusDays(14); // 6 days ago
            Loan loan = new Loan(1, b, m, loanDate, dueDate);
            LocalDate today = LocalDate.now();
            double fine = new DailyRateFineCalculator(50.0).calculateFine(loan, today);
            assertEquals(300.0, fine, "a 50/day calculator should charge 6 x 50 = 300 with zero changes elsewhere");
        });

        printSummary();
        if (getFailed() > 0) {
            System.exit(1);
        }
    }
}
