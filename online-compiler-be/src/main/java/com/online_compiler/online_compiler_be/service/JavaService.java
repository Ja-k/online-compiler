package com.online_compiler.online_compiler_be.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.service.support.DockerCommandRunner;
import com.online_compiler.online_compiler_be.service.support.TempWorkspace;

@Service
public class JavaService implements LanguageExecutionService {

	private static final Set<String> SUPPORTED_VERSIONS = Set.of("8", "11", "17", "21");
	private static final String DEFAULT_VERSION = "17";
	private static final long PULL_TIMEOUT_SECONDS = 120;
	private static final long COMPILE_TIMEOUT_SECONDS = 30;
	private static final long RUN_TIMEOUT_SECONDS = 10;

	@Override
	public String getLanguageId() {
		return "java";
	}

	@Override
	public String run(String code, String version) throws Exception {
		// The "-jdk-alpine" variant is smaller but only publishes amd64 manifests for most
		// versions (no arm64), which would break on Apple Silicon / arm64 hosts. The plain
		// "-jdk" tag is multi-arch, so it's used despite being the larger option.
		String image = "eclipse-temurin:" + resolveVersion(version) + "-jdk";
		DockerCommandRunner.ensureImagePulled(image, PULL_TIMEOUT_SECONDS);

		try (TempWorkspace workspace = TempWorkspace.create("java-code")) {
			workspace.writeSourceFile("Main.java", code);
			Path dir = workspace.getDirectory();

			DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					image,
					"javac", "/app/Main.java"
			), COMPILE_TIMEOUT_SECONDS, true);

			return DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					image,
					"java", "-cp", "/app", "Main"
			), RUN_TIMEOUT_SECONDS, false);
		}
	}

	private String resolveVersion(String version) {
		return SUPPORTED_VERSIONS.contains(version) ? version : DEFAULT_VERSION;
	}
}
