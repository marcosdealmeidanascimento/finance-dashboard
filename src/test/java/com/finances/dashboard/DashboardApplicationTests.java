package com.finances.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class DashboardApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void flywayBuildsTheSchemaFromAnEmptyDatabase() {
		Integer applied = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM flyway_schema_history WHERE success AND version IS NOT NULL", Integer.class);
		assertThat(applied).isEqualTo(4);

		// V4: email uniqueness is the partial, case-insensitive index, not the old full constraint.
		Integer index = jdbcTemplate.queryForObject(
				"SELECT count(*) FROM pg_indexes WHERE tablename = 'users' AND indexname = 'uq_users_email_active'",
				Integer.class);
		assertThat(index).isEqualTo(1);
	}

}
