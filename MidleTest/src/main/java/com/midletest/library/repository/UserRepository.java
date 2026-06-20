package com.midletest.library.repository;

import com.midletest.library.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final JdbcTemplate jdbcTemplate;

  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<User> findByUsername(String username) {
    String sql = "SELECT id, username, password_hash, full_name, email, role FROM users WHERE username = ?";
    List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
      User user = new User();
      user.setId(rs.getInt("id"));
      user.setUsername(rs.getString("username"));
      user.setPasswordHash(rs.getString("password_hash"));
      user.setFullName(rs.getString("full_name"));
      user.setEmail(rs.getString("email"));
      user.setRole(rs.getString("role"));
      return user;
    }, username);
    return users.stream().findFirst();
  }

  public Optional<User> findByEmail(String email) {
    String sql = "SELECT id, username, password_hash, full_name, email, role FROM users WHERE email = ?";
    List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
      User user = new User();
      user.setId(rs.getInt("id"));
      user.setUsername(rs.getString("username"));
      user.setPasswordHash(rs.getString("password_hash"));
      user.setFullName(rs.getString("full_name"));
      user.setEmail(rs.getString("email"));
      user.setRole(rs.getString("role"));
      return user;
    }, email);
    return users.stream().findFirst();
  }

  public Optional<User> findByUsernameOrEmail(String value) {
    String sql =
        "SELECT id, username, password_hash, full_name, email, role FROM users WHERE username = ? OR email = ?";
    List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
      User user = new User();
      user.setId(rs.getInt("id"));
      user.setUsername(rs.getString("username"));
      user.setPasswordHash(rs.getString("password_hash"));
      user.setFullName(rs.getString("full_name"));
      user.setEmail(rs.getString("email"));
      user.setRole(rs.getString("role"));
      return user;
    }, value, value);
    return users.stream().findFirst();
  }

  public boolean existsByUsername(String username) {
    Integer total = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE username = ?",
        Integer.class,
        username);
    return total != null && total > 0;
  }

  public boolean existsByEmail(String email) {
    Integer total = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM users WHERE email = ?",
        Integer.class,
        email);
    return total != null && total > 0;
  }

  public void create(String username, String passwordHash, String fullName, String email, String role) {
    String sql = "INSERT INTO users (username, password_hash, full_name, email, role) VALUES (?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, username, passwordHash, fullName, email, role);
  }

  public int countUsers() {
    Integer total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
    return total == null ? 0 : total;
  }
}
