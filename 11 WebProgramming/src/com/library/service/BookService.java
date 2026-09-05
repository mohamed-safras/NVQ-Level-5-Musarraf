package com.library.service;

import com.library.model.Book;

import java.util.List;

public interface BookService {
    Book addBook(String isbn, String title, String category, String author, int copies);
    Book findBook(int bookId);
    List<Book> searchBooks(String keyword);
    List<Book> listBooks();
    boolean updateBookDetails(int bookId, String title, String category, String author);
    boolean deleteBook(int bookId);
}
