USE library_automation_system;

-- =====================================================================
-- Practical SQL Queries - Library Automation System
-- Demonstrates extraction of records from two or more tables (JOINs),
-- aggregation, grouping and subqueries as required for the viva.
-- =====================================================================

-- Q1: List all currently overdue loans with member name, book title and due date
-- (JOIN across 4 tables: Loans, Members, Book_Copies, Books)
SELECT
    l.loan_id,
    CONCAT(m.first_name, ' ', m.last_name) AS member_name,
    b.title,
    l.due_date
FROM Loans l
JOIN Members m       ON l.member_id = m.member_id
JOIN Book_Copies bc  ON l.copy_id   = bc.copy_id
JOIN Books b         ON bc.book_id  = b.book_id
WHERE l.status = 'Overdue';

-- Q2: Total unpaid fine amount for each member
-- (JOIN: Fines -> Loans -> Members, with GROUP BY and SUM)
SELECT
    CONCAT(m.first_name, ' ', m.last_name) AS member_name,
    SUM(f.amount) AS total_unpaid
FROM Fines f
JOIN Loans l   ON f.loan_id   = l.loan_id
JOIN Members m ON l.member_id = m.member_id
WHERE f.paid_status = 'Unpaid'
GROUP BY m.member_id;

-- Q3: List every book together with its category and author(s)
-- (JOIN across 4 tables: Books, Categories, Book_Authors, Authors)
SELECT
    b.title,
    c.category_name,
    CONCAT(a.first_name, ' ', a.last_name) AS author_name
FROM Books b
JOIN Categories c    ON b.category_id = c.category_id
JOIN Book_Authors ba ON b.book_id     = ba.book_id
JOIN Authors a        ON ba.author_id = a.author_id
ORDER BY c.category_name, b.title;

-- Q4: Books that currently have no available copies (all copies out on loan or lost)
-- (JOIN + GROUP BY + HAVING)
SELECT
    b.title,
    COUNT(bc.copy_id) AS total_copies
FROM Books b
JOIN Book_Copies bc ON b.book_id = bc.book_id
GROUP BY b.book_id
HAVING SUM(CASE WHEN bc.status = 'Available' THEN 1 ELSE 0 END) = 0;

-- Q5: Members who are Suspended or Expired (should be blocked from new loans)
SELECT first_name, last_name, membership_status
FROM Members
WHERE membership_status IN ('Suspended', 'Expired');

-- Q6: Full loan history for a specific member, most recent first
-- (JOIN: Loans -> Book_Copies -> Books, filtered by member)
SELECT
    b.title,
    l.loan_date,
    l.due_date,
    l.return_date,
    l.status
FROM Loans l
JOIN Book_Copies bc ON l.copy_id = bc.copy_id
JOIN Books b        ON bc.book_id = b.book_id
WHERE l.member_id = 1
ORDER BY l.loan_date DESC;

-- Q7: Count of active loans handled by each staff member
-- (JOIN + GROUP BY, demonstrates staff workload reporting)
SELECT
    CONCAT(s.first_name, ' ', s.last_name) AS staff_name,
    COUNT(l.loan_id) AS active_loans_handled
FROM Staff s
LEFT JOIN Loans l ON s.staff_id = l.staff_id AND l.status IN ('Active', 'Overdue')
GROUP BY s.staff_id;

-- Q8: Notification log for a specific loan (used to confirm automated emails fired)
SELECT
    n.notification_type,
    n.message,
    n.sent_date,
    n.email_status
FROM Notifications n
WHERE n.loan_id = 1
ORDER BY n.sent_date;
