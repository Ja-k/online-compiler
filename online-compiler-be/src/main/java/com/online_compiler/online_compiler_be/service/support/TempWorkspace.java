package com.online_compiler.online_compiler_be.service.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A short-lived temp directory holding one submission's source file, mounted
 * into a Docker container for compilation/execution and cleaned up afterwards.
 */
public final class TempWorkspace implements AutoCloseable {

	private final Path directory;

	private TempWorkspace(Path directory) {
		this.directory = directory;
	}

	public static TempWorkspace create(String prefix) throws IOException {
		return new TempWorkspace(Files.createTempDirectory(prefix));
	}

	public Path writeSourceFile(String fileName, String code) throws IOException {
		Path sourceFile = directory.resolve(fileName);
		Files.writeString(sourceFile, code);
		return sourceFile;
	}

	public Path getDirectory() {
		return directory;
	}

	@Override
	public void close() {
		try (Stream<Path> paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException ignored) {
					// best-effort cleanup
				}
			});
		} catch (IOException ignored) {
			// best-effort cleanup
		}
	}
}
