package com.midletest.library.repository;

import com.midletest.library.model.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {
  private final JdbcTemplate jdbcTemplate;

  public BookRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Book> findAll() {
    String sql = """
        SELECT b.id, b.title, b.author, b.category_id, b.quantity, b.cover_image, c.name AS category_name
        FROM books b
        LEFT JOIN categories c ON b.category_id = c.id
        ORDER BY b.id DESC
        """;
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Book book = new Book();
      book.setId(rs.getInt("id"));
      book.setTitle(rs.getString("title"));
      book.setAuthor(rs.getString("author"));
      book.setCategoryId((Integer) rs.getObject("category_id"));
      book.setCategoryName(rs.getString("category_name"));
      book.setQuantity(rs.getInt("quantity"));
      book.setCoverImage(rs.getString("cover_image"));
      return book;
    });
  }

  public List<Book> search(String keyword) {
    String sql = """
        SELECT b.id, b.title, b.author, b.category_id, b.quantity, b.cover_image, c.name AS category_name
        FROM books b
        LEFT JOIN categories c ON b.category_id = c.id
        WHERE b.title LIKE ? OR b.author LIKE ?
        ORDER BY b.id DESC
        """;
    String pattern = "%" + keyword + "%";
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Book book = new Book();
      book.setId(rs.getInt("id"));
      book.setTitle(rs.getString("title"));
      book.setAuthor(rs.getString("author"));
      book.setCategoryId((Integer) rs.getObject("category_id"));
      book.setCategoryName(rs.getString("category_name"));
      book.setQuantity(rs.getInt("quantity"));
      book.setCoverImage(rs.getString("cover_image"));
      return book;
    }, pattern, pattern);
  }

  public Optional<Book> findById(Integer id) {
    String sql = """
        SELECT b.id, b.title, b.author, b.category_id, b.quantity, b.cover_image, c.name AS category_name
        FROM books b
        LEFT JOIN categories c ON b.category_id = c.id
        WHERE b.id = ?
        """;
    List<Book> books = jdbcTemplate.query(sql, (rs, rowNum) -> {
      Book book = new Book();
      book.setId(rs.getInt("id"));
      book.setTitle(rs.getString("title"));
      book.setAuthor(rs.getString("author"));
      book.setCategoryId((Integer) rs.getObject("category_id"));
      book.setCategoryName(rs.getString("category_name"));
      book.setQuantity(rs.getInt("quantity"));
      book.setCoverImage(rs.getString("cover_image"));
      return book;
    }, id);
    return books.stream().findFirst();
  }

  public void create(Book book) {
    String sql = "INSERT INTO books (title, author, category_id, quantity, cover_image) VALUES (?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql,
        book.getTitle(),
        book.getAuthor(),
        book.getCategoryId(),
        book.getQuantity() == null ? 0 : book.getQuantity(),
        book.getCoverImage());
  }

  public void update(Integer id, Book book) {
    String sql = "UPDATE books SET title = ?, author = ?, category_id = ?, quantity = ?, cover_image = ? WHERE id = ?";
    jdbcTemplate.update(sql,
        book.getTitle(),
        book.getAuthor(),
        book.getCategoryId(),
        book.getQuantity() == null ? 0 : book.getQuantity(),
        book.getCoverImage(),
        id);
  }

  public void delete(Integer id) {
    jdbcTemplate.update("DELETE FROM books WHERE id = ?", id);
  }

  public int count() {
    Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
    return total == null ? 0 : total;
  }

  public int countAvailable() {
    Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM books WHERE quantity > 0", Integer.class);
    return total == null ? 0 : total;
  }

  public List<Book> findFeatured(int limit) {
    String sql = """
        SELECT b.id, b.title, b.author, b.category_id, b.quantity, b.cover_image, c.name AS category_name
        FROM books b
        LEFT JOIN categories c ON b.category_id = c.id
        ORDER BY b.id DESC
        LIMIT ?
        """;
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Book book = new Book();
      book.setId(rs.getInt("id"));
      book.setTitle(rs.getString("title"));
      book.setAuthor(rs.getString("author"));
      book.setCategoryId((Integer) rs.getObject("category_id"));
      book.setCategoryName(rs.getString("category_name"));
      book.setQuantity(rs.getInt("quantity"));
      book.setCoverImage(rs.getString("cover_image"));
      return book;
    }, limit);
  }
}
