package com.online_compiler.online_compiler_be.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.service.support.DockerCommandRunner;
import com.online_compiler.online_compiler_be.service.support.TempWorkspace;

@Service
public class RustService implements LanguageExecutionService {

	/**
	 * A single modern Rust toolchain image is used for all editions; the edition is a compile flag.
	 * The "-alpine" variant of this image is essentially the same size as "-slim" (the rustc/cargo
	 * toolchain itself dominates the size either way), so the more common glibc-based slim image is
	 * used to avoid any musl-specific linking surprises for no size benefit.
	 */
	private static final String IMAGE = "rust:1.82-slim";
	private static final Set<String> SUPPORTED_EDITIONS = Set.of("2015", "2018", "2021", "2024");
	private static final String DEFAULT_EDITION = "2021";
	private static final long PULL_TIMEOUT_SECONDS = 120;
	private static final long COMPILE_TIMEOUT_SECONDS = 30;
	private static final long RUN_TIMEOUT_SECONDS = 10;

	@Override
	public String getLanguageId() {
		return "rust";
	}

	@Override
	public String run(String code, String version) throws Exception {
		DockerCommandRunner.ensureImagePulled(IMAGE, PULL_TIMEOUT_SECONDS);

		try (TempWorkspace workspace = TempWorkspace.create("rust-code")) {
			workspace.writeSourceFile("main.rs", code);
			Path dir = workspace.getDirectory();

			DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					IMAGE,
					"rustc", "--edition=" + resolveEdition(version), "-O", "-o", "/app/main", "/app/main.rs"
			), COMPILE_TIMEOUT_SECONDS, true);

			return DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					IMAGE,
					"/app/main"
			), RUN_TIMEOUT_SECONDS, false);
		}
	}

	private String resolveEdition(String version) {
		return SUPPORTED_EDITIONS.contains(version) ? version : DEFAULT_EDITION;
	}
}
