package com.library.service.impl;

import com.library.exception.RecordNotFoundException;
import com.library.model.Member;
import com.library.model.MembershipStatus;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.service.MemberService;

import java.time.LocalDate;
import java.util.List;

/**
 * Depends on MemberRepository and LoanRepository — both interfaces, never
 * the in-memory implementations (Dependency Inversion). It needs
 * LoanRepository only for the one cross-entity rule ("can't delete a
 * member with an active loan"); reaching for the repository rather than
 * LoanService here avoids a circular service-to-service dependency
 * (LoanServiceImpl needs MemberService to check eligibility, so
 * MemberService cannot also depend on LoanService).
 */
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public MemberServiceImpl(MemberRepository memberRepository, LoanRepository loanRepository) {
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public Member registerMember(String firstName, String lastName, String email, String phone) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email address is required.");
        }
        Member member = new Member(0, firstName, lastName, email, phone, LocalDate.now(), MembershipStatus.ACTIVE);
        return memberRepository.add(member);
    }

    @Override
    public Member findMember(int memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RecordNotFoundException("Member " + memberId + " not found."));
    }

    @Override
    public List<Member> searchMembers(String keyword) {
        return memberRepository.searchByName(keyword);
    }

    @Override
    public List<Member> listMembers() {
        return memberRepository.findAll();
    }

    @Override
    public boolean updateMemberContact(int memberId, String phone, String email) {
        Member member = findMember(memberId);
        member.setPhone(phone);
        member.setEmail(email);
        return memberRepository.update(member);
    }

    @Override
    public boolean deleteMember(int memberId) {
        if (!loanRepository.findActiveByMember(memberId).isEmpty()) {
            throw new IllegalStateException("Cannot delete member " + memberId + " with active loans.");
        }
        return memberRepository.deleteById(memberId);
    }
}
