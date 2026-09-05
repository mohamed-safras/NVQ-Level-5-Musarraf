package com.library.repository.memory;

import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.repository.LoanRepository;

import java.util.*;

public class InMemoryLoanRepository implements LoanRepository {
    private final Map<Integer, Loan> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Loan add(Loan loan) {
        loan.setLoanId(nextId);
        store.put(nextId, loan);
        nextId++;
        return loan;
    }

    @Override
    public Optional<Loan> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Loan> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Loan> findActiveByMember(int memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : store.values()) {
            if (l.getMember().getMemberId() == memberId && l.getStatus() != LoanStatus.RETURNED) {
                result.add(l);
            }
        }
        return result;
    }

    @Override
    public boolean update(Loan loan) {
        if (!store.containsKey(loan.getLoanId())) return false;
        store.put(loan.getLoanId(), loan);
        return true;
    }

    @Override
    public boolean deleteById(Integer id) {
        return store.remove(id) != null;
    }
}
