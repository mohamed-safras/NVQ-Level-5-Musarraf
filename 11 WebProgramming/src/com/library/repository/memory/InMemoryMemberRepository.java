package com.library.repository.memory;

import com.library.model.Member;
import com.library.repository.MemberRepository;

import java.util.*;

/**
 * In-memory implementation of MemberRepository, used for this offline
 * evidence build. Because every consumer depends on the MemberRepository
 * interface (never on this class directly — see the service layer), a
 * JdbcMemberRepository backed by the Database Systems MySQL schema could
 * be dropped in later with a one-line change at the composition root
 * (ApiServer) and nothing else in the codebase would need to change.
 */
public class InMemoryMemberRepository implements MemberRepository {
    private final Map<Integer, Member> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Member add(Member member) {
        Member persisted = new Member(nextId++, member.getFirstName(), member.getLastName(),
                member.getEmail(), member.getPhone(), member.getMembershipDate(), member.getStatus());
        store.put(persisted.getMemberId(), persisted);
        return persisted;
    }

    @Override
    public Optional<Member> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Member> searchByName(String keyword) {
        String k = keyword.toLowerCase();
        List<Member> result = new ArrayList<>();
        for (Member m : store.values()) {
            if (m.getFullName().toLowerCase().contains(k) || m.getEmail().toLowerCase().contains(k)) {
                result.add(m);
            }
        }
        return result;
    }

    @Override
    public boolean update(Member member) {
        if (!store.containsKey(member.getMemberId())) return false;
        store.put(member.getMemberId(), member);
        return true;
    }

    @Override
    public boolean deleteById(Integer id) {
        return store.remove(id) != null;
    }
}
