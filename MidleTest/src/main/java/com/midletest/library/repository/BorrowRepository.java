package com.midletest.library.repository;

import com.midletest.library.model.Borrow;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class BorrowRepository {
  private final JdbcTemplate jdbcTemplate;

  public BorrowRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Borrow> findAll() {
    String sql = """
        SELECT br.id, br.reader_id, br.book_id, br.borrow_date, br.due_date, br.return_date, br.status, br.is_overdue,
               r.name AS reader_name, b.title AS book_title
        FROM borrows br
        JOIN readers r ON br.reader_id = r.id
        JOIN books b ON br.book_id = b.id
        ORDER BY br.id DESC
        """;
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Borrow borrow = new Borrow();
      borrow.setId(rs.getInt("id"));
      borrow.setReaderId(rs.getInt("reader_id"));
      borrow.setBookId(rs.getInt("book_id"));
      borrow.setReaderName(rs.getString("reader_name"));
      borrow.setBookTitle(rs.getString("book_title"));
      borrow.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
      if (rs.getDate("due_date") != null) {
        borrow.setDueDate(rs.getDate("due_date").toLocalDate());
      }
      if (rs.getDate("return_date") != null) {
        borrow.setReturnDate(rs.getDate("return_date").toLocalDate());
      }
      borrow.setStatus(rs.getString("status"));
      borrow.setOverdue(rs.getBoolean("is_overdue"));
      return borrow;
    });
  }

  @Transactional
  public void create(Borrow borrow) {
    int decreased =
        jdbcTemplate.update(
            "UPDATE books SET quantity = quantity - 1 WHERE id = ? AND quantity > 0",
            borrow.getBookId());
    if (decreased == 0) {
      throw new IllegalStateException("Sách đã hết hoặc không tồn tại.");
    }

    Integer requestedDays = borrow.getBorrowDays();
    int borrowDays = requestedDays == null ? 14 : requestedDays;
    if (borrowDays < 1 || borrowDays > 30) {
      throw new IllegalStateException("Số ngày mượn phải từ 1 đến 30 ngày.");
    }

    LocalDate borrowDate = borrow.getBorrowDate() != null ? borrow.getBorrowDate() : LocalDate.now();
    LocalDate dueDate = borrowDate.plusDays(borrowDays);

    String sql =
        "INSERT INTO borrows (reader_id, book_id, borrow_date, due_date, status, is_overdue) VALUES (?, ?, ?, ?, 'borrowed', 0)";
    jdbcTemplate.update(sql, borrow.getReaderId(), borrow.getBookId(), borrowDate, dueDate);
  }

  @Transactional
  public boolean returnBook(Integer id) {
    String infoSql = "SELECT id, book_id FROM borrows WHERE id = ? AND status = 'borrowed'";
    List<Map<String, Object>> rows = jdbcTemplate.queryForList(infoSql, id);
    if (rows.isEmpty()) {
      return false;
    }

    Number bookIdValue = (Number) rows.get(0).get("book_id");
    Integer bookId = bookIdValue == null ? null : bookIdValue.intValue();
    if (bookId == null) {
      throw new IllegalStateException("Không xác định được sách của phiếu mượn.");
    }

    String updateBorrowSql = "UPDATE borrows SET status = 'returned', return_date = CURDATE() WHERE id = ?";
    int updated = jdbcTemplate.update(updateBorrowSql, id);
    if (updated == 0) {
      return false;
    }

    jdbcTemplate.update("UPDATE books SET quantity = quantity + 1 WHERE id = ?", bookId);
    return true;
  }

  public int countBorrowed() {
    Integer total = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM borrows WHERE status = 'borrowed'",
        Integer.class);
    return total == null ? 0 : total;
  }

  public int countOverdue() {
    Integer total = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM borrows WHERE status = 'borrowed' AND is_overdue = 1",
        Integer.class);
    return total == null ? 0 : total;
  }

  public void refreshOverdueFlags() {
    String sql = """
        UPDATE borrows
        SET is_overdue = CASE
            WHEN status = 'borrowed' AND COALESCE(due_date, DATE_ADD(borrow_date, INTERVAL 14 DAY)) < CURDATE() THEN 1
            ELSE 0
        END
        """;
    jdbcTemplate.update(sql);
  }

  public int countActiveByReaderEmail(String email) {
    if (email == null || email.isBlank()) {
      return 0;
    }
    Integer total =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM borrows br
            JOIN readers r ON br.reader_id = r.id
            WHERE LOWER(r.email) = LOWER(?) AND br.status = 'borrowed'
            """,
            Integer.class,
            email);
    return total == null ? 0 : total;
  }

  public List<Borrow> findActiveByReaderEmail(String email, int limit) {
    if (email == null || email.isBlank()) {
      return List.of();
    }
    String sql = """
        SELECT br.id, br.reader_id, br.book_id, br.borrow_date, br.due_date, br.return_date, br.status, br.is_overdue,
               r.name AS reader_name, b.title AS book_title
        FROM borrows br
        JOIN readers r ON br.reader_id = r.id
        JOIN books b ON br.book_id = b.id
        WHERE LOWER(r.email) = LOWER(?) AND br.status = 'borrowed'
        ORDER BY br.borrow_date DESC, br.id DESC
        LIMIT ?
        """;
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Borrow borrow = new Borrow();
      borrow.setId(rs.getInt("id"));
      borrow.setReaderId(rs.getInt("reader_id"));
      borrow.setBookId(rs.getInt("book_id"));
      borrow.setReaderName(rs.getString("reader_name"));
      borrow.setBookTitle(rs.getString("book_title"));
      borrow.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
      if (rs.getDate("due_date") != null) {
        borrow.setDueDate(rs.getDate("due_date").toLocalDate());
      }
      if (rs.getDate("return_date") != null) {
        borrow.setReturnDate(rs.getDate("return_date").toLocalDate());
      }
      borrow.setStatus(rs.getString("status"));
      borrow.setOverdue(rs.getBoolean("is_overdue"));
      return borrow;
    }, email, limit);
  }

  public List<Borrow> findRecentByReaderEmail(String email, int limit) {
    if (email == null || email.isBlank()) {
      return List.of();
    }
    String sql = """
        SELECT br.id, br.reader_id, br.book_id, br.borrow_date, br.due_date, br.return_date, br.status, br.is_overdue,
               r.name AS reader_name, b.title AS book_title
        FROM borrows br
        JOIN readers r ON br.reader_id = r.id
        JOIN books b ON br.book_id = b.id
        WHERE LOWER(r.email) = LOWER(?)
        ORDER BY br.id DESC
        LIMIT ?
        """;
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Borrow borrow = new Borrow();
      borrow.setId(rs.getInt("id"));
      borrow.setReaderId(rs.getInt("reader_id"));
      borrow.setBookId(rs.getInt("book_id"));
      borrow.setReaderName(rs.getString("reader_name"));
      borrow.setBookTitle(rs.getString("book_title"));
      borrow.setBorrowDate(rs.getDate("borrow_date").toLocalDate());
      if (rs.getDate("due_date") != null) {
        borrow.setDueDate(rs.getDate("due_date").toLocalDate());
      }
      if (rs.getDate("return_date") != null) {
        borrow.setReturnDate(rs.getDate("return_date").toLocalDate());
      }
      borrow.setStatus(rs.getString("status"));
      borrow.setOverdue(rs.getBoolean("is_overdue"));
      return borrow;
    }, email, limit);
  }
}
