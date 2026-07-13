package com.online_compiler.online_compiler_be.service;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.service.support.DockerCommandRunner;
import com.online_compiler.online_compiler_be.service.support.TempWorkspace;

@Service
public class PythonService implements LanguageExecutionService {

	private static final Set<String> SUPPORTED_VERSIONS = Set.of("3.8", "3.9", "3.10", "3.11", "3.12", "3.13");
	private static final String DEFAULT_VERSION = "3.11";
	private static final long PULL_TIMEOUT_SECONDS = 120;
	private static final long RUN_TIMEOUT_SECONDS = 10;

	@Override
	public String getLanguageId() {
		return "python";
	}

	@Override
	public String run(String code, String version) throws Exception {
		// Alpine variant: roughly half the size of "-slim", plenty for running plain scripts.
		String image = "python:" + resolveVersion(version) + "-alpine";
		DockerCommandRunner.ensureImagePulled(image, PULL_TIMEOUT_SECONDS);

		try (TempWorkspace workspace = TempWorkspace.create("python-code")) {
			workspace.writeSourceFile("main.py", code);
			Path dir = workspace.getDirectory();

			return DockerCommandRunner.run(List.of(
					"docker", "run", "--rm",
					"-v", dir.toAbsolutePath() + ":/app",
					image,
					"python", "/app/main.py"
			), RUN_TIMEOUT_SECONDS, false);
		}
	}

	private String resolveVersion(String version) {
		return SUPPORTED_VERSIONS.contains(version) ? version : DEFAULT_VERSION;
	}
}
