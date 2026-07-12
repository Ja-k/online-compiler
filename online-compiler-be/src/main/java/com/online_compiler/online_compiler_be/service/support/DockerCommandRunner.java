package com.online_compiler.online_compiler_be.service.support;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Runs a single "docker run ..." style command and captures its combined
 * stdout/stderr, enforcing a timeout so a stuck/looping submission can't hang
 * the server indefinitely.
 */
public final class DockerCommandRunner {

	private DockerCommandRunner() {
	}

	/**
	 * @param throwOnNonZeroExit if true, a non-zero exit code raises an exception carrying the
	 *                           process output (used for compile steps); if false, the output is
	 *                           returned as-is regardless of exit code (used for run steps, since a
	 *                           user's program legitimately exiting non-zero isn't a service error).
	 */
	/**
	 * Pulls the image (if not already cached) in its own process, so pull-progress
	 * output never gets mixed into a subsequent compile/run step's captured output.
	 */
	public static void ensureImagePulled(String image, long timeoutSeconds) throws Exception {
		run(List.of("docker", "pull", image), timeoutSeconds, true);
	}

	public static String run(List<String> command, long timeoutSeconds, boolean throwOnNonZeroExit) throws Exception {
		Process process = new ProcessBuilder(command)
				.redirectErrorStream(true)
				.start();

		String output = new String(process.getInputStream().readAllBytes());

		boolean finishedInTime = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
		if (!finishedInTime) {
			process.destroyForcibly();
			throw new RuntimeException("Execution timed out after " + timeoutSeconds + "s");
		}

		if (throwOnNonZeroExit && process.exitValue() != 0) {
			throw new RuntimeException(output.isBlank() ? "Process exited with code " + process.exitValue() : output);
		}

		return output;
	}
}
