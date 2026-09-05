package com.library.repository;

import com.library.model.Book;

import java.util.List;

public interface BookRepository extends Repository<Book, Integer> {
    List<Book> searchByTitleOrAuthor(String keyword);
}
