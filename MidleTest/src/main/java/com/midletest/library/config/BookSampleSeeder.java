package com.midletest.library.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookSampleSeeder implements ApplicationRunner {
  private static final Logger logger = LoggerFactory.getLogger(BookSampleSeeder.class);
  private final JdbcTemplate jdbcTemplate;

  public BookSampleSeeder(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    Map<String, Integer> categoryIds = ensureCategories();
    int inserted = 0;
    for (SeedBook seed : seedBooks()) {
      if (bookExists(seed.title(), seed.author())) {
        continue;
      }
      Integer categoryId = categoryIds.get(seed.categoryName());
      jdbcTemplate.update(
          "INSERT INTO books (title, author, category_id, quantity, cover_image) VALUES (?, ?, ?, ?, ?)",
          seed.title(),
          seed.author(),
          categoryId,
          seed.quantity(),
          seed.coverImage());
      inserted++;
    }
    if (inserted > 0) {
      logger.info("Seeded {} sample books.", inserted);
    } else {
      logger.info("Sample books already exist. No insert needed.");
    }
  }

  private Map<String, Integer> ensureCategories() {
    Map<String, Integer> ids = new LinkedHashMap<>();
    for (String categoryName : List.of("Văn học", "Khoa học", "Lịch sử", "Công nghệ")) {
      Integer id = findCategoryId(categoryName);
      if (id == null) {
        jdbcTemplate.update("INSERT INTO categories (name) VALUES (?)", categoryName);
        id = findCategoryId(categoryName);
      }
      if (id != null) {
        ids.put(categoryName, id);
      }
    }
    return ids;
  }

  private Integer findCategoryId(String name) {
    try {
      return jdbcTemplate.queryForObject(
          "SELECT id FROM categories WHERE name = ? LIMIT 1", Integer.class, name);
    } catch (EmptyResultDataAccessException ex) {
      return null;
    }
  }

  private boolean bookExists(String title, String author) {
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM books WHERE title = ? AND author = ?",
            Integer.class,
            title,
            author);
    return count != null && count > 0;
  }

  private List<SeedBook> seedBooks() {
    return List.of(
        new SeedBook(
            "Nhà giả kim",
            "Paulo Coelho",
            "Văn học",
            8,
            "https://picsum.photos/seed/book-001/400/600"),
        new SeedBook(
            "Tuổi trẻ đáng giá bao nhiêu",
            "Rosie Nguyễn",
            "Văn học",
            12,
            "https://picsum.photos/seed/book-002/400/600"),
        new SeedBook(
            "Muôn kiếp nhân sinh",
            "Nguyên Phong",
            "Văn học",
            9,
            "https://picsum.photos/seed/book-003/400/600"),
        new SeedBook(
            "Dế Mèn phiêu lưu ký",
            "Tô Hoài",
            "Văn học",
            15,
            "https://picsum.photos/seed/book-004/400/600"),
        new SeedBook(
            "Tắt đèn",
            "Ngô Tất Tố",
            "Văn học",
            7,
            "https://picsum.photos/seed/book-005/400/600"),
        new SeedBook(
            "Sapiens: Lược sử loài người",
            "Yuval Noah Harari",
            "Khoa học",
            6,
            "https://picsum.photos/seed/book-006/400/600"),
        new SeedBook(
            "Homo Deus: Lược sử tương lai",
            "Yuval Noah Harari",
            "Khoa học",
            6,
            "https://picsum.photos/seed/book-007/400/600"),
        new SeedBook(
            "Vũ trụ trong vỏ hạt dẻ",
            "Stephen Hawking",
            "Khoa học",
            5,
            "https://picsum.photos/seed/book-008/400/600"),
        new SeedBook(
            "Brief Answers to the Big Questions",
            "Stephen Hawking",
            "Khoa học",
            4,
            "https://picsum.photos/seed/book-009/400/600"),
        new SeedBook(
            "A Brief History of Time",
            "Stephen Hawking",
            "Khoa học",
            5,
            "https://picsum.photos/seed/book-010/400/600"),
        new SeedBook(
            "Lịch sử thế giới",
            "J. M. Roberts",
            "Lịch sử",
            4,
            "https://picsum.photos/seed/book-011/400/600"),
        new SeedBook(
            "Việt Nam sử lược",
            "Trần Trọng Kim",
            "Lịch sử",
            6,
            "https://picsum.photos/seed/book-012/400/600"),
        new SeedBook(
            "Đại Việt sử ký toàn thư",
            "Ngô Sĩ Liên",
            "Lịch sử",
            3,
            "https://picsum.photos/seed/book-013/400/600"),
        new SeedBook(
            "Lịch sử tư tưởng Trung Quốc",
            "Kim Định",
            "Lịch sử",
            2,
            "https://picsum.photos/seed/book-014/400/600"),
        new SeedBook(
            "Thế chiến thứ hai",
            "Antony Beevor",
            "Lịch sử",
            4,
            "https://picsum.photos/seed/book-015/400/600"),
        new SeedBook(
            "Clean Architecture",
            "Robert C. Martin",
            "Công nghệ",
            8,
            "https://picsum.photos/seed/book-016/400/600"),
        new SeedBook(
            "Refactoring",
            "Martin Fowler",
            "Công nghệ",
            7,
            "https://picsum.photos/seed/book-017/400/600"),
        new SeedBook(
            "Design Patterns",
            "Erich Gamma",
            "Công nghệ",
            6,
            "https://picsum.photos/seed/book-018/400/600"),
        new SeedBook(
            "The Pragmatic Programmer",
            "Andrew Hunt",
            "Công nghệ",
            5,
            "https://picsum.photos/seed/book-019/400/600"),
        new SeedBook(
            "Effective Java",
            "Joshua Bloch",
            "Công nghệ",
            9,
            "https://picsum.photos/seed/book-020/400/600"));
  }

  private record SeedBook(
      String title,
      String author,
      String categoryName,
      int quantity,
      String coverImage) {}
}
