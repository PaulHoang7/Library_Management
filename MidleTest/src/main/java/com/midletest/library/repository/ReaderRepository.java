package com.midletest.library.repository;

import com.midletest.library.model.Reader;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReaderRepository {
  private final JdbcTemplate jdbcTemplate;

  public ReaderRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Reader> findAll() {
    String sql = "SELECT id, name, email, phone FROM readers ORDER BY id DESC";
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Reader reader = new Reader();
      reader.setId(rs.getInt("id"));
      reader.setName(rs.getString("name"));
      reader.setEmail(rs.getString("email"));
      reader.setPhone(rs.getString("phone"));
      return reader;
    });
  }

  public Optional<Reader> findById(Integer id) {
    String sql = "SELECT id, name, email, phone FROM readers WHERE id = ?";
    List<Reader> readers = jdbcTemplate.query(sql, (rs, rowNum) -> {
      Reader reader = new Reader();
      reader.setId(rs.getInt("id"));
      reader.setName(rs.getString("name"));
      reader.setEmail(rs.getString("email"));
      reader.setPhone(rs.getString("phone"));
      return reader;
    }, id);
    return readers.stream().findFirst();
  }

  public Optional<Reader> findByEmail(String email) {
    String sql = "SELECT id, name, email, phone FROM readers WHERE email = ?";
    List<Reader> readers = jdbcTemplate.query(sql, (rs, rowNum) -> {
      Reader reader = new Reader();
      reader.setId(rs.getInt("id"));
      reader.setName(rs.getString("name"));
      reader.setEmail(rs.getString("email"));
      reader.setPhone(rs.getString("phone"));
      return reader;
    }, email);
    return readers.stream().findFirst();
  }

  public Integer ensureReaderForUser(String fullName, String username) {
    String normalizedUsername = username == null ? "user" : username.trim().toLowerCase();
    String email = normalizedUsername.contains("@")
        ? normalizedUsername
        : normalizedUsername + "@user.local";
    String name = (fullName != null && !fullName.isBlank()) ? fullName.trim() : normalizedUsername;

    Optional<Reader> existingReader = findByEmail(email);
    if (existingReader.isPresent()) {
      return existingReader.get().getId();
    }

    String insertSql = "INSERT INTO readers (name, email, phone) VALUES (?, ?, ?)";
    jdbcTemplate.update(insertSql, name, email, null);
    return jdbcTemplate.queryForObject(
        "SELECT id FROM readers WHERE email = ?",
        Integer.class,
        email);
  }

  public Integer ensureReaderForUser(String fullName, String username, String phone) {
    Integer readerId = ensureReaderForUser(fullName, username);
    String normalizedPhone = normalizePhone(phone);
    if (normalizedPhone != null) {
      updatePhone(readerId, normalizedPhone);
    }
    return readerId;
  }

  public void updatePhone(Integer id, String phone) {
    jdbcTemplate.update("UPDATE readers SET phone = ? WHERE id = ?", phone, id);
  }

  private String normalizePhone(String phone) {
    if (phone == null) {
      return null;
    }
    String value = phone.trim();
    return value.isEmpty() ? null : value;
  }

  public void create(Reader reader) {
    String sql = "INSERT INTO readers (name, email, phone) VALUES (?, ?, ?)";
    jdbcTemplate.update(sql, reader.getName(), reader.getEmail(), reader.getPhone());
  }

  public void update(Integer id, Reader reader) {
    String sql = "UPDATE readers SET name = ?, email = ?, phone = ? WHERE id = ?";
    jdbcTemplate.update(sql, reader.getName(), reader.getEmail(), reader.getPhone(), id);
  }

  public void delete(Integer id) {
    jdbcTemplate.update("DELETE FROM readers WHERE id = ?", id);
  }

  public int count() {
    Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM readers", Integer.class);
    return total == null ? 0 : total;
  }
}
