package com.library.service;

import com.library.model.Member;

import java.util.List;

public interface MemberService {
    Member registerMember(String firstName, String lastName, String email, String phone);
    Member findMember(int memberId);
    List<Member> searchMembers(String keyword);
    List<Member> listMembers();
    boolean updateMemberContact(int memberId, String phone, String email);
    boolean deleteMember(int memberId);
}
