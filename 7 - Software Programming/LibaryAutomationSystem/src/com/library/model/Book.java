package com.library.model;

/**
 * Represents a row of the Books table. totalCopies/availableCopies summarise
 * the Book_Copies table for the purposes of this console application.
 */
public class Book {
    private int bookId;
    private String isbn;
    private String title;
    private String category;
    private String author;
    private int totalCopies;
    private int availableCopies;

    public Book(int bookId, String isbn, String title, String category, String author, int totalCopies) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.category = category;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public int getAvailableCopies() { return availableCopies; }

    public boolean hasAvailableCopy() { return availableCopies > 0; }

    public void borrowCopy() {
        if (!hasAvailableCopy()) {
            throw new IllegalStateException("No available copies of \"" + title + "\".");
        }
        availableCopies--;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    @Override
    public String toString() {
        return String.format("[%d] %-30s %-18s %-20s copies:%d/%d",
                bookId, title, category, author, availableCopies, totalCopies);
    }
}
