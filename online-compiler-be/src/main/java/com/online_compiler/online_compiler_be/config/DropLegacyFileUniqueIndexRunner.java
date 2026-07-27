package com.online_compiler.online_compiler_be.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-shot cleanup for the pre-folders unique index on {@code saved_files}.
 * Hibernate {@code ddl-auto: update} adds columns/tables but will not drop an
 * existing unique constraint, and {@code (user_id, filename)} would incorrectly
 * block two files with the same name in different folders. Safe to re-run:
 * MySQL returns an error if the index is already gone, which we ignore.
 */
@Component
public class DropLegacyFileUniqueIndexRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DropLegacyFileUniqueIndexRunner.class);

	private final JdbcTemplate jdbcTemplate;

	public DropLegacyFileUniqueIndexRunner(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(ApplicationArguments args) {
		try {
			// InnoDB needs *some* index covering user_id to support the user_id
			// foreign key, and uk_saved_files_user_filename is currently the only
			// one providing that. Add a plain index first so the DROP below
			// doesn't fail with "needed in a foreign key constraint".
			jdbcTemplate.execute("CREATE INDEX idx_saved_files_user_id ON saved_files (user_id)");
			log.info("Created idx_saved_files_user_id on saved_files");
		} catch (Exception e) {
			log.debug("idx_saved_files_user_id already present or could not be created: {}", e.getMessage());
		}
		try {
			jdbcTemplate.execute("ALTER TABLE saved_files DROP INDEX uk_saved_files_user_filename");
			log.info("Dropped legacy unique index uk_saved_files_user_filename from saved_files");
		} catch (Exception e) {
			log.debug("Legacy unique index already absent or could not be dropped: {}", e.getMessage());
		}
	}
}
