package com.online_compiler.online_compiler_be.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.service.support.DockerCommandRunner;
import com.online_compiler.online_compiler_be.service.support.TempWorkspace;

@Service
public class CppService implements LanguageExecutionService {

	/** A single modern GCC image is used for all standards; the standard itself is a compile flag. */
	private static final String IMAGE = "gcc:13.2.0";
	private static final Set<String> SUPPORTED_STANDARDS = Set.of("11", "14", "17", "20", "23");
	private static final String DEFAULT_STANDARD = "17";
	private static final long PULL_TIMEOUT_SECONDS = 120;
	private static final long COMPILE_TIMEOUT_SECONDS = 30;
	private static final long RUN_TIMEOUT_SECONDS = 10;

	@Override
	public String getLanguageId() {
		return "cpp";
	}

	@Override
	public String run(String code, String version) throws Exception {
		DockerCommandRunner.ensureImagePulled(IMAGE, PULL_TIMEOUT_SECONDS);

		try (TempWorkspace workspace = TempWorkspace.create("cpp-code")) {
			workspace.writeSourceFile("main.cpp", code);
			Path dir = workspace.getDirectory();

			DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					IMAGE,
					"g++", "-std=c++" + resolveStandard(version), "-O2", "-o", "/app/main", "/app/main.cpp"
			), COMPILE_TIMEOUT_SECONDS, true);

			return DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					IMAGE,
					"/app/main"
			), RUN_TIMEOUT_SECONDS, false);
		}
	}

	private String resolveStandard(String version) {
		return SUPPORTED_STANDARDS.contains(version) ? version : DEFAULT_STANDARD;
	}
}
