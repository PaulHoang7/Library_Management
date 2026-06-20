package com.midletest.library.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LegacySchemaCleanupRunner implements ApplicationRunner {
  private static final Logger logger = LoggerFactory.getLogger(LegacySchemaCleanupRunner.class);
  private final JdbcTemplate jdbcTemplate;

  public LegacySchemaCleanupRunner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(ApplicationArguments args) {
    Integer exists =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'login_otps'
            """,
            Integer.class);

    if (exists != null && exists > 0) {
      jdbcTemplate.execute("DROP TABLE login_otps");
      logger.info("Dropped legacy table: login_otps");
    }
  }
}
