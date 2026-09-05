package com.library.repository.memory;

import com.library.model.Book;
import com.library.repository.BookRepository;

import java.util.*;

public class InMemoryBookRepository implements BookRepository {
    private final Map<Integer, Book> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Book add(Book book) {
        Book persisted = new Book(nextId++, book.getIsbn(), book.getTitle(),
                book.getCategory(), book.getAuthor(), book.getTotalCopies());
        store.put(persisted.getBookId(), persisted);
        return persisted;
    }

    @Override
    public Optional<Book> findById(Integer id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Book> searchByTitleOrAuthor(String keyword) {
        String k = keyword.toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : store.values()) {
            if (b.getTitle().toLowerCase().contains(k) || b.getAuthor().toLowerCase().contains(k)) {
                result.add(b);
            }
        }
        return result;
    }

    @Override
    public boolean update(Book book) {
        if (!store.containsKey(book.getBookId())) return false;
        store.put(book.getBookId(), book);
        return true;
    }

    @Override
    public boolean deleteById(Integer id) {
        return store.remove(id) != null;
    }
}
