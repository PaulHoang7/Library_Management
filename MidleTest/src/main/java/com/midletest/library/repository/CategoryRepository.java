package com.midletest.library.repository;

import com.midletest.library.model.Category;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryRepository {
  private final JdbcTemplate jdbcTemplate;

  public CategoryRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<Category> findAll() {
    String sql = "SELECT id, name FROM categories ORDER BY name";
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Category category = new Category();
      category.setId(rs.getInt("id"));
      category.setName(rs.getString("name"));
      return category;
    });
  }
}
