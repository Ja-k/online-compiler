package com.online_compiler.online_compiler_be.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class JavaService {

	public String compileAndRun(String code) {
		return null;
	}
	
	
	private static String runJava(String code) throws Exception {
        Path tempDir = Files.createTempDirectory("java-code");
        Path sourceFile = tempDir.resolve("Main.java");
        Files.write(sourceFile, code.getBytes());
        /*
        // Compile
        Process compileProcess = new ProcessBuilder("javac", sourceFile.toString())
                .redirectErrorStream(true).start();
        String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
        if (compileProcess.waitFor() != 0) {
            throw new RuntimeException("Compilation failed:\n" + compileOutput);
        }
        */
        // Step 1: Compile
        ProcessBuilder compilePb = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", tempDir.toAbsolutePath() + ":/app",
                "openjdk:17",
                "javac", "/app/Main.java"
        );
        compilePb.redirectErrorStream(true);
        Process compileProcess = compilePb.start();

        String compileOutput = new String(compileProcess.getInputStream().readAllBytes());
        int compileExit = compileProcess.waitFor();

        if (compileExit != 0) {
            // Compilation failed → return errors
            throw new RuntimeException("Compilation failed:\n" + compileOutput);
        }
        /*
        // Run
        Process runProcess = new ProcessBuilder("java", "-cp", tempDir.toString(), "Main")
                .redirectErrorStream(true).start();
        String runOutput = new String(runProcess.getInputStream().readAllBytes());
        runProcess.waitFor();
        */
     // Step 2: Run
        ProcessBuilder runPb = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", tempDir.toAbsolutePath() + ":/app",
                "openjdk:17",
                "java", "-cp", "/app", "Main"
        );
        runPb.redirectErrorStream(true);
        Process runProcess = runPb.start();

        if (!runProcess.waitFor(5, TimeUnit.SECONDS)) {
            runProcess.destroyForcibly();
            throw new RuntimeException("Execution timed out!");
        }

        String runOutput = new String(runProcess.getInputStream().readAllBytes());
        return runOutput;
    }
}
