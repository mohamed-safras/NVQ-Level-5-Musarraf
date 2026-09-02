-- ---------------------------------------------------------------------
-- MEMBERS
-- ---------------------------------------------------------------------
INSERT INTO Members (first_name, last_name, email, phone, address, membership_date, membership_status) VALUES
('Nadeesha', 'Perera',   'nadeesha.perera@mail.com', '0771234561', 'No 12, Galle Rd, Colombo',   '2023-01-10', 'Active'),
('Kasun',    'Fernando', 'kasun.fernando@mail.com',  '0771234562', 'No 45, Kandy Rd, Kadawatha', '2023-02-14', 'Active'),
('Ishara',   'Silva',    'ishara.silva@mail.com',    '0771234563', 'No 8, Temple Rd, Kandy',     '2023-03-05', 'Active'),
('Ruwan',    'Jayasuriya','ruwan.j@mail.com',        '0771234564', 'No 21, Lake Rd, Kurunegala', '2023-04-20', 'Suspended'),
('Dilani',   'Rathnayake','dilani.r@mail.com',       '0771234565', 'No 3, Hill St, Matara',      '2023-05-11', 'Active'),
('Chamara',  'Weerasinghe','chamara.w@mail.com',     '0771234566', 'No 77, Main St, Negombo',    '2023-06-01', 'Active'),
('Sanduni',  'Gunawardena','sanduni.g@mail.com',     '0771234567', 'No 15, Park Rd, Galle',      '2023-06-18', 'Expired'),
('Tharindu', 'Bandara',  'tharindu.b@mail.com',      '0771234568', 'No 9, Church St, Jaffna',    '2023-07-22', 'Active');

-- ---------------------------------------------------------------------
-- STAFF
-- ---------------------------------------------------------------------
INSERT INTO Staff (first_name, last_name, email, role, hire_date) VALUES
('Anusha', 'Wickramasinghe', 'anusha.w@library.lk', 'Admin',     '2020-01-15'),
('Priyantha', 'Kumara',      'priyantha.k@library.lk', 'Librarian', '2021-03-10'),
('Menaka', 'De Silva',       'menaka.d@library.lk', 'Assistant', '2022-07-01');

-- ---------------------------------------------------------------------
-- CATEGORIES
-- ---------------------------------------------------------------------
INSERT INTO Categories (category_name, description) VALUES
('Computer Science', 'Programming, databases, networking and IT concepts'),
('Fiction',           'Novels and short story collections'),
('Science',           'Physics, chemistry, biology and general science'),
('Business',          'Management, finance and entrepreneurship'),
('History',           'World and local history');

-- ---------------------------------------------------------------------
-- AUTHORS
-- ---------------------------------------------------------------------
INSERT INTO Authors (first_name, last_name, nationality) VALUES
('Robert',   'Martin',    'American'),
('Martin',   'Fowler',    'British'),
('George',   'Orwell',    'British'),
('Yuval',    'Noah Harari','Israeli'),
('Chimamanda','Ngozi Adichie','Nigerian'),
('James',    'Gosling',   'Canadian');

-- ---------------------------------------------------------------------
-- BOOKS
-- ---------------------------------------------------------------------
INSERT INTO Books (isbn, title, category_id, publisher, publish_year, edition) VALUES
('978-0132350884', 'Clean Code',                       1, 'Prentice Hall',      2008, '1st'),
('978-0201485677', 'Refactoring',                      1, 'Addison-Wesley',     1999, '1st'),
('978-0451524935', '1984',                             2, 'Signet Classic',     1949, 'Reprint'),
('978-0062316097', 'Sapiens: A Brief History of Humankind', 3, 'Harper',        2015, '1st'),
('978-0307455925', 'Half of a Yellow Sun',              2, 'Anchor Books',       2007, '1st'),
('978-0596007126', 'Head First Java',                   1, "O'Reilly",          2005, '2nd');

-- ---------------------------------------------------------------------
-- BOOK_AUTHORS (M:N)
-- ---------------------------------------------------------------------
INSERT INTO Book_Authors (book_id, author_id) VALUES
(1, 1),  -- Clean Code -> Robert Martin
(2, 2),  -- Refactoring -> Martin Fowler
(3, 3),  -- 1984 -> George Orwell
(4, 4),  -- Sapiens -> Yuval Noah Harari
(5, 5),  -- Half of a Yellow Sun -> Chimamanda Ngozi Adichie
(6, 6);  -- Head First Java -> James Gosling (co-authorship omitted for brevity)

-- ---------------------------------------------------------------------
-- BOOK_COPIES (multiple physical copies per title)
-- ---------------------------------------------------------------------
INSERT INTO Book_Copies (book_id, copy_number, shelf_location, status) VALUES
(1, 1, 'CS-A1', 'Loaned'),
(1, 2, 'CS-A1', 'Available'),
(2, 1, 'CS-A2', 'Available'),
(3, 1, 'FIC-B1', 'Loaned'),
(3, 2, 'FIC-B1', 'Available'),
(4, 1, 'SCI-C1', 'Loaned'),
(5, 1, 'FIC-B2', 'Available'),
(6, 1, 'CS-A3', 'Loaned'),
(6, 2, 'CS-A3', 'Lost');

-- ---------------------------------------------------------------------
-- LOANS
-- ---------------------------------------------------------------------
INSERT INTO Loans (copy_id, member_id, staff_id, loan_date, due_date, return_date, status) VALUES
(1, 1, 2, '2024-11-01', '2024-11-15', NULL,          'Overdue'),
(4, 2, 2, '2024-11-05', '2024-11-19', '2024-11-18',  'Returned'),
(6, 3, 3, '2024-11-10', '2024-11-24', NULL,          'Active'),
(8, 4, 2, '2024-10-20', '2024-11-03', NULL,          'Overdue'),
(2, 5, 3, '2024-11-12', '2024-11-26', NULL,          'Active'),
(5, 6, 2, '2024-09-15', '2024-09-29', '2024-09-27',  'Returned');

-- ---------------------------------------------------------------------
-- FINES (linked to overdue loans)
-- ---------------------------------------------------------------------
INSERT INTO Fines (loan_id, amount, reason, issued_date, paid_status, paid_date) VALUES
(1, 150.00, 'Overdue', '2024-11-16', 'Unpaid', NULL),
(4, 450.00, 'Overdue', '2024-11-04', 'Paid',   '2024-11-10');

-- ---------------------------------------------------------------------
-- NOTIFICATIONS (automated email log)
-- ---------------------------------------------------------------------
INSERT INTO Notifications (member_id, loan_id, notification_type, message, sent_date, email_status) VALUES
(1, 1, 'Overdue Alert',        'Your book "Clean Code" is overdue. Please return it as soon as possible.', '2024-11-16 09:00:00', 'Sent'),
(2, 2, 'Return Confirmation',  'Thank you for returning "Sapiens: A Brief History of Humankind".',        '2024-11-18 14:30:00', 'Sent'),
(3, 3, 'Due Reminder',         'Your loan for "Sapiens: A Brief History of Humankind" is due in 3 days.', '2024-11-21 08:00:00', 'Sent'),
(4, 4, 'Fine Notice',          'A fine of Rs. 450.00 has been issued to your account.',                   '2024-11-04 10:15:00', 'Sent'),
(5, 5, 'Due Reminder',         'Your loan for "Refactoring" is due in 3 days.',                            '2024-11-23 08:00:00', 'Pending');