USE library_db;

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Nhà giả kim', 'Paulo Coelho', 1, 8, 'https://picsum.photos/seed/book-001/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Nhà giả kim' AND author = 'Paulo Coelho'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Tuổi trẻ đáng giá bao nhiêu', 'Rosie Nguyễn', 1, 12, 'https://picsum.photos/seed/book-002/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Tuổi trẻ đáng giá bao nhiêu' AND author = 'Rosie Nguyễn'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Muôn kiếp nhân sinh', 'Nguyên Phong', 1, 9, 'https://picsum.photos/seed/book-003/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Muôn kiếp nhân sinh' AND author = 'Nguyên Phong'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Dế Mèn phiêu lưu ký', 'Tô Hoài', 1, 15, 'https://picsum.photos/seed/book-004/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Dế Mèn phiêu lưu ký' AND author = 'Tô Hoài'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Tắt đèn', 'Ngô Tất Tố', 1, 7, 'https://picsum.photos/seed/book-005/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Tắt đèn' AND author = 'Ngô Tất Tố'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Sapiens: Lược sử loài người', 'Yuval Noah Harari', 2, 6, 'https://picsum.photos/seed/book-006/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Sapiens: Lược sử loài người' AND author = 'Yuval Noah Harari'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Homo Deus: Lược sử tương lai', 'Yuval Noah Harari', 2, 6, 'https://picsum.photos/seed/book-007/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Homo Deus: Lược sử tương lai' AND author = 'Yuval Noah Harari'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Vũ trụ trong vỏ hạt dẻ', 'Stephen Hawking', 2, 5, 'https://picsum.photos/seed/book-008/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Vũ trụ trong vỏ hạt dẻ' AND author = 'Stephen Hawking'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Brief Answers to the Big Questions', 'Stephen Hawking', 2, 4, 'https://picsum.photos/seed/book-009/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Brief Answers to the Big Questions' AND author = 'Stephen Hawking'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'A Brief History of Time', 'Stephen Hawking', 2, 5, 'https://picsum.photos/seed/book-010/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'A Brief History of Time' AND author = 'Stephen Hawking'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Lịch sử thế giới', 'J. M. Roberts', 3, 4, 'https://picsum.photos/seed/book-011/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Lịch sử thế giới' AND author = 'J. M. Roberts'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Việt Nam sử lược', 'Trần Trọng Kim', 3, 6, 'https://picsum.photos/seed/book-012/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Việt Nam sử lược' AND author = 'Trần Trọng Kim'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Đại Việt sử ký toàn thư', 'Ngô Sĩ Liên', 3, 3, 'https://picsum.photos/seed/book-013/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Đại Việt sử ký toàn thư' AND author = 'Ngô Sĩ Liên'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Lịch sử tư tưởng Trung Quốc', 'Kim Định', 3, 2, 'https://picsum.photos/seed/book-014/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Lịch sử tư tưởng Trung Quốc' AND author = 'Kim Định'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Thế chiến thứ hai', 'Antony Beevor', 3, 4, 'https://picsum.photos/seed/book-015/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Thế chiến thứ hai' AND author = 'Antony Beevor'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Clean Architecture', 'Robert C. Martin', 4, 8, 'https://picsum.photos/seed/book-016/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Clean Architecture' AND author = 'Robert C. Martin'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Refactoring', 'Martin Fowler', 4, 7, 'https://picsum.photos/seed/book-017/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Refactoring' AND author = 'Martin Fowler'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Design Patterns', 'Erich Gamma', 4, 6, 'https://picsum.photos/seed/book-018/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Design Patterns' AND author = 'Erich Gamma'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'The Pragmatic Programmer', 'Andrew Hunt', 4, 5, 'https://picsum.photos/seed/book-019/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'The Pragmatic Programmer' AND author = 'Andrew Hunt'
);

INSERT INTO books (title, author, category_id, quantity, cover_image)
SELECT 'Effective Java', 'Joshua Bloch', 4, 9, 'https://picsum.photos/seed/book-020/400/600'
WHERE NOT EXISTS (
  SELECT 1 FROM books WHERE title = 'Effective Java' AND author = 'Joshua Bloch'
);
