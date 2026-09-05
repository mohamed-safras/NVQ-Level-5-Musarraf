package com.library.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract. In this evidence build the concrete repositories
 * store data in memory (ArrayList) so the whole program is self-contained
 * and runnable without a live MySQL server; the method signatures mirror
 * what a JDBC-backed implementation against the Database Systems schema
 * would expose (see DatabaseNote.txt for the JDBC mapping).
 */
public interface Repository<T, ID> {
    T add(T item);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean update(T item);
    boolean deleteById(ID id);
}
