-- =====================================================================
-- Library Automation System - Database Schema
-- NVQ Level 5 - Database Systems Unit
-- Engine: MySQL 8.0
-- =====================================================================

DROP DATABASE IF EXISTS library_automation_system;
CREATE DATABASE library_automation_system;
USE library_automation_system;

-- ---------------------------------------------------------------------
-- 1. MEMBERS
-- ---------------------------------------------------------------------
CREATE TABLE Members (
    member_id           INT AUTO_INCREMENT PRIMARY KEY,
    first_name          VARCHAR(50)  NOT NULL,
    last_name           VARCHAR(50)  NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    phone               VARCHAR(15),
    address             VARCHAR(150),
    membership_date     DATE NOT NULL,
    membership_status   ENUM('Active', 'Suspended', 'Expired') NOT NULL DEFAULT 'Active'
);

-- ---------------------------------------------------------------------
-- 2. STAFF
-- ---------------------------------------------------------------------
CREATE TABLE Staff (
    staff_id            INT AUTO_INCREMENT PRIMARY KEY,
    first_name          VARCHAR(50) NOT NULL,
    last_name           VARCHAR(50) NOT NULL,
    email               VARCHAR(100) NOT NULL UNIQUE,
    role                ENUM('Librarian', 'Assistant', 'Admin') NOT NULL DEFAULT 'Librarian',
    hire_date           DATE NOT NULL
);

-- ---------------------------------------------------------------------
-- 3. CATEGORIES
-- ---------------------------------------------------------------------
CREATE TABLE Categories (
    category_id         INT AUTO_INCREMENT PRIMARY KEY,
    category_name       VARCHAR(60) NOT NULL UNIQUE,
    description         VARCHAR(200)
);

-- ---------------------------------------------------------------------
-- 4. AUTHORS
-- ---------------------------------------------------------------------
CREATE TABLE Authors (
    author_id           INT AUTO_INCREMENT PRIMARY KEY,
    first_name          VARCHAR(50) NOT NULL,
    last_name            VARCHAR(50) NOT NULL,
    nationality          VARCHAR(50)
);

-- ---------------------------------------------------------------------
-- 5. BOOKS
-- ---------------------------------------------------------------------
CREATE TABLE Books (
    book_id             INT AUTO_INCREMENT PRIMARY KEY,
    isbn                VARCHAR(20) NOT NULL UNIQUE,
    title               VARCHAR(150) NOT NULL,
    category_id         INT NOT NULL,
    publisher           VARCHAR(100),
    publish_year        YEAR,
    edition             VARCHAR(20),
    CONSTRAINT fk_books_category
        FOREIGN KEY (category_id) REFERENCES Categories(category_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- 6. BOOK_AUTHORS (resolves the M:N relationship between Books & Authors)
-- ---------------------------------------------------------------------
CREATE TABLE Book_Authors (
    book_id             INT NOT NULL,
    author_id           INT NOT NULL,
    PRIMARY KEY (book_id, author_id),
    CONSTRAINT fk_ba_book
        FOREIGN KEY (book_id) REFERENCES Books(book_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ba_author
        FOREIGN KEY (author_id) REFERENCES Authors(author_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 7. BOOK_COPIES (each physical copy of a title held by the library)
-- ---------------------------------------------------------------------
CREATE TABLE Book_Copies (
    copy_id             INT AUTO_INCREMENT PRIMARY KEY,
    book_id             INT NOT NULL,
    copy_number         INT NOT NULL,
    shelf_location      VARCHAR(20),
    status              ENUM('Available', 'Loaned', 'Lost', 'Damaged') NOT NULL DEFAULT 'Available',
    CONSTRAINT fk_copies_book
        FOREIGN KEY (book_id) REFERENCES Books(book_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT uq_book_copy UNIQUE (book_id, copy_number)
);

-- ---------------------------------------------------------------------
-- 8. LOANS (a lending transaction)
-- ---------------------------------------------------------------------
CREATE TABLE Loans (
    loan_id             INT AUTO_INCREMENT PRIMARY KEY,
    copy_id             INT NOT NULL,
    member_id           INT NOT NULL,
    staff_id            INT NOT NULL,
    loan_date           DATE NOT NULL,
    due_date            DATE NOT NULL,
    return_date         DATE NULL,
    status              ENUM('Active', 'Returned', 'Overdue') NOT NULL DEFAULT 'Active',
    CONSTRAINT fk_loans_copy
        FOREIGN KEY (copy_id) REFERENCES Book_Copies(copy_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_loans_member
        FOREIGN KEY (member_id) REFERENCES Members(member_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_loans_staff
        FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------
-- 9. FINES (overdue / damage fines linked to a loan)
-- ---------------------------------------------------------------------
CREATE TABLE Fines (
    fine_id             INT AUTO_INCREMENT PRIMARY KEY,
    loan_id             INT NOT NULL,
    amount              DECIMAL(8,2) NOT NULL,
    reason              ENUM('Overdue', 'Damage', 'Lost') NOT NULL DEFAULT 'Overdue',
    issued_date         DATE NOT NULL,
    paid_status         ENUM('Unpaid', 'Paid') NOT NULL DEFAULT 'Unpaid',
    paid_date           DATE NULL,
    CONSTRAINT fk_fines_loan
        FOREIGN KEY (loan_id) REFERENCES Loans(loan_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 10. NOTIFICATIONS (automated email log for due/overdue/returns)
-- ---------------------------------------------------------------------
CREATE TABLE Notifications (
    notification_id     INT AUTO_INCREMENT PRIMARY KEY,
    member_id           INT NOT NULL,
    loan_id             INT NULL,
    notification_type   ENUM('Due Reminder', 'Overdue Alert', 'Return Confirmation', 'Fine Notice') NOT NULL,
    message             VARCHAR(255),
    sent_date           DATETIME NOT NULL,
    email_status        ENUM('Sent', 'Failed', 'Pending') NOT NULL DEFAULT 'Pending',
    CONSTRAINT fk_notif_member
        FOREIGN KEY (member_id) REFERENCES Members(member_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_notif_loan
        FOREIGN KEY (loan_id) REFERENCES Loans(loan_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);