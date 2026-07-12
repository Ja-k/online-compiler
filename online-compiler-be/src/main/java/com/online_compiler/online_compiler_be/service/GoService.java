package com.online_compiler.online_compiler_be.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.service.support.DockerCommandRunner;
import com.online_compiler.online_compiler_be.service.support.TempWorkspace;

@Service
public class GoService implements LanguageExecutionService {

	private static final Set<String> SUPPORTED_VERSIONS = Set.of("1.20", "1.21", "1.22", "1.23");
	private static final String DEFAULT_VERSION = "1.22";
	private static final long PULL_TIMEOUT_SECONDS = 120;
	private static final long COMPILE_TIMEOUT_SECONDS = 30;
	private static final long RUN_TIMEOUT_SECONDS = 10;

	@Override
	public String getLanguageId() {
		return "go";
	}

	@Override
	public String run(String code, String version) throws Exception {
		String image = "golang:" + resolveVersion(version);
		DockerCommandRunner.ensureImagePulled(image, PULL_TIMEOUT_SECONDS);

		try (TempWorkspace workspace = TempWorkspace.create("go-code")) {
			workspace.writeSourceFile("main.go", code);
			Path dir = workspace.getDirectory();

			// A single main.go file builds fine without a go.mod, since `go build`
			// accepts an explicit list of source files as a standalone package.
			DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					image,
					"go", "build", "-o", "/app/main", "/app/main.go"
			), COMPILE_TIMEOUT_SECONDS, true);

			return DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					image,
					"/app/main"
			), RUN_TIMEOUT_SECONDS, false);
		}
	}

	private String resolveVersion(String version) {
		return SUPPORTED_VERSIONS.contains(version) ? version : DEFAULT_VERSION;
	}
}
