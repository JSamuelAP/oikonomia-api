package dev.jsamuelap.oikonomiaapi.support;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import dev.jsamuelap.oikonomiaapi.TestcontainersConfiguration;
import dev.jsamuelap.oikonomiaapi.category.domain.model.FlowType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Import(TestcontainersConfiguration.class)
public abstract class IntegrationTestSupport {
  @Autowired
  protected JdbcTemplate jdbc;

  @Autowired
  protected TransactionTemplate transactionTemplate;

  @BeforeEach
  void cleanDatabase() {
    jdbc.execute("""
          TRUNCATE TABLE users, refresh_tokens, categories, transactions, monthly_budgets, event_publication CASCADE
      """);
  }

  protected UUID insertUser() {
    UUID id = UUID.randomUUID();
    jdbc.update("""
          INSERT INTO users (id, first_name, last_name, email, password_hash)
          VALUES (?, 'Sam', 'Aldana', ?, 'hash')
      """, id, id + "@example.com");
    return id;
  }

  protected UUID insertCategory(UUID userId, String name, FlowType flowType) {
    UUID id = UUID.randomUUID();
    jdbc.update("""
          INSERT INTO categories (id, user_id, name, flow_type)
          VALUES (?, ?, ?, ?::flow_type)
      """, id, userId, name, flowType.name());
    return id;
  }
}
