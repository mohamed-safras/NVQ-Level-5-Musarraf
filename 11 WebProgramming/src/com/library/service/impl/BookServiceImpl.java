package com.library.service.impl;

import com.library.exception.RecordNotFoundException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.service.BookService;

import java.util.List;

public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book addBook(String isbn, String title, String category, String author, int copies) {
        if (copies < 1) {
            throw new IllegalArgumentException("A book must have at least one copy.");
        }
        Book book = new Book(0, isbn, title, category, author, copies);
        return bookRepository.add(book);
    }

    @Override
    public Book findBook(int bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RecordNotFoundException("Book " + bookId + " not found."));
    }

    @Override
    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchByTitleOrAuthor(keyword);
    }

    @Override
    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    @Override
    public boolean updateBookDetails(int bookId, String title, String category, String author) {
        Book book = findBook(bookId);
        book.setTitle(title);
        book.setCategory(category);
        book.setAuthor(author);
        return bookRepository.update(book);
    }

    @Override
    public boolean deleteBook(int bookId) {
        Book book = findBook(bookId);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new IllegalStateException("Cannot delete book " + bookId + " while copies are on loan.");
        }
        return bookRepository.deleteById(bookId);
    }
}
