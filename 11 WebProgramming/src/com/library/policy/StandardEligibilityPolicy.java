package com.library.policy;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.MembershipStatus;

/** Default policy: member must be ACTIVE, and the book must have a spare copy. */
public class StandardEligibilityPolicy implements LoanEligibilityPolicy {

    @Override
    public void checkEligible(Member member, Book book) {
        if (member.getStatus() != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("Member " + member.getMemberId() + " is "
                    + member.getStatus() + " and cannot borrow books.");
        }
        if (!book.hasAvailableCopy()) {
            throw new IllegalStateException("No available copies of \"" + book.getTitle() + "\".");
        }
    }
}
