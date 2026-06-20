CREATE DATABASE IF NOT EXISTS library_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE library_db;
ALTER DATABASE library_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS books (
  id INT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(150) NOT NULL,
  category_id INT,
  quantity INT NOT NULL DEFAULT 0,
  cover_image VARCHAR(500),
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS readers (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  phone VARCHAR(15)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS borrows (
  id INT AUTO_INCREMENT PRIMARY KEY,
  reader_id INT NOT NULL,
  book_id INT NOT NULL,
  borrow_date DATE NOT NULL,
  due_date DATE,
  return_date DATE,
  status ENUM('borrowed', 'returned') DEFAULT 'borrowed',
  is_overdue TINYINT(1) NOT NULL DEFAULT 0,
  FOREIGN KEY (reader_id) REFERENCES readers(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(150) NOT NULL,
  email VARCHAR(150) UNIQUE,
  role ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DROP TABLE IF EXISTS login_otps;

SET @has_overdue_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'library_db' AND TABLE_NAME = 'borrows' AND COLUMN_NAME = 'is_overdue'
);
SET @add_overdue_sql := IF(@has_overdue_col = 0,
  'ALTER TABLE borrows ADD COLUMN is_overdue TINYINT(1) NOT NULL DEFAULT 0',
  'SELECT 1'
);
PREPARE stmt_overdue FROM @add_overdue_sql;
EXECUTE stmt_overdue;
DEALLOCATE PREPARE stmt_overdue;

SET @has_due_date_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'library_db' AND TABLE_NAME = 'borrows' AND COLUMN_NAME = 'due_date'
);
SET @add_due_date_sql := IF(@has_due_date_col = 0,
  'ALTER TABLE borrows ADD COLUMN due_date DATE NULL AFTER borrow_date',
  'SELECT 1'
);
PREPARE stmt_due_date FROM @add_due_date_sql;
EXECUTE stmt_due_date;
DEALLOCATE PREPARE stmt_due_date;

UPDATE borrows
SET due_date = DATE_ADD(borrow_date, INTERVAL 14 DAY)
WHERE due_date IS NULL;

SET @has_cover_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'library_db' AND TABLE_NAME = 'books' AND COLUMN_NAME = 'cover_image'
);
SET @add_cover_sql := IF(@has_cover_col = 0,
  'ALTER TABLE books ADD COLUMN cover_image VARCHAR(500) NULL',
  'SELECT 1'
);
PREPARE stmt_cover FROM @add_cover_sql;
EXECUTE stmt_cover;
DEALLOCATE PREPARE stmt_cover;

SET @has_role_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'library_db' AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role'
);
SET @add_role_sql := IF(@has_role_col = 0,
  'ALTER TABLE users ADD COLUMN role ENUM(''ADMIN'', ''USER'') NOT NULL DEFAULT ''USER''',
  'SELECT 1'
);
PREPARE stmt_role FROM @add_role_sql;
EXECUTE stmt_role;
DEALLOCATE PREPARE stmt_role;

SET @has_email_col := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = 'library_db' AND TABLE_NAME = 'users' AND COLUMN_NAME = 'email'
);
SET @add_email_sql := IF(@has_email_col = 0,
  'ALTER TABLE users ADD COLUMN email VARCHAR(150) NULL',
  'SELECT 1'
);
PREPARE stmt_email FROM @add_email_sql;
EXECUTE stmt_email;
DEALLOCATE PREPARE stmt_email;

SET @has_email_index := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = 'library_db'
    AND TABLE_NAME = 'users'
    AND COLUMN_NAME = 'email'
    AND NON_UNIQUE = 0
);
SET @add_email_index_sql := IF(@has_email_index = 0,
  'ALTER TABLE users ADD UNIQUE KEY uk_users_email (email)',
  'SELECT 1'
);
PREPARE stmt_email_index FROM @add_email_index_sql;
EXECUTE stmt_email_index;
DEALLOCATE PREPARE stmt_email_index;

SET @has_admin := (SELECT COUNT(*) FROM users WHERE role = 'ADMIN');
SET @has_user := (SELECT COUNT(*) FROM users);
SET @promote_sql := IF(@has_admin = 0 AND @has_user > 0,
  'UPDATE users SET role = ''ADMIN'' WHERE id = (SELECT id FROM (SELECT id FROM users ORDER BY id ASC LIMIT 1) t)',
  'SELECT 1'
);
PREPARE stmt_promote FROM @promote_sql;
EXECUTE stmt_promote;
DEALLOCATE PREPARE stmt_promote;

INSERT IGNORE INTO categories (id, name) VALUES
  (1, 'Văn học'),
  (2, 'Khoa học'),
  (3, 'Lịch sử'),
  (4, 'Công nghệ');

INSERT IGNORE INTO books (id, title, author, category_id, quantity, cover_image) VALUES
  (1, 'Truyện Kiều', 'Nguyễn Du', 1, 5, 'https://images.unsplash.com/photo-1474932430478-367dbb6832c1?auto=format&fit=crop&w=900&q=80'),
  (2, 'Lược sử thời gian', 'Stephen Hawking', 2, 3, 'https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=900&q=80'),
  (3, 'Đắc Nhân Tâm', 'Dale Carnegie', 1, 10, 'https://images.unsplash.com/photo-1521587760476-6c12a4b040da?auto=format&fit=crop&w=900&q=80'),
  (4, 'Lịch sử Việt Nam', 'Trần Trọng Kim', 3, 4, 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=900&q=80'),
  (5, 'Clean Code', 'Robert C. Martin', 4, 2, 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=900&q=80');

INSERT IGNORE INTO readers (id, name, email, phone) VALUES
  (1, 'Nguyễn Văn A', 'nguyenvana@email.com', '0901234567'),
  (2, 'Trần Thị B', 'tranthib@email.com', '0912345678'),
  (3, 'Lê Văn C', 'levanc@email.com', '0923456789');

INSERT IGNORE INTO borrows (id, reader_id, book_id, borrow_date, due_date, status, is_overdue) VALUES
  (1, 1, 1, '2026-03-01', '2026-03-15', 'borrowed', 0),
  (2, 2, 3, '2026-03-15', '2026-03-29', 'borrowed', 0),
  (3, 3, 5, '2026-03-10', '2026-03-24', 'returned', 0);
