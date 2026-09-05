package com.library.repository;

import com.library.model.Member;

import java.util.List;

/**
 * Adds member-specific lookups on top of the generic Repository<T,ID>
 * contract. Kept as its own small interface (Interface Segregation) so a
 * consumer that only needs to search members isn't forced to depend on
 * book- or loan-specific methods that would live on a single "God"
 * repository interface.
 */
public interface MemberRepository extends Repository<Member, Integer> {
    List<Member> searchByName(String keyword);
}
