package com.library.policy;

import com.library.model.Book;
import com.library.model.Member;

/**
 * Decides whether a given member may borrow a given book right now.
 * Kept separate from LoanServiceImpl so a stricter rule (e.g. "no more
 * than 3 concurrent loans", "blocked while any fine is unpaid") can be
 * introduced later as a new implementation, or composed with this one,
 * without editing the loan-issuing workflow itself.
 */
public interface LoanEligibilityPolicy {
    /** @throws IllegalStateException with a human-readable reason if not eligible. */
    void checkEligible(Member member, Book book);
}
