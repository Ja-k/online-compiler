package com.online_compiler.online_compiler_be.service;

/**
 * Implemented by each supported language's compile/run backend.
 */
public interface LanguageExecutionService {

	/** Matches the {@code language} id sent by the frontend, e.g. "java", "cpp", "python", "rust". */
	String getLanguageId();

	/**
	 * Compiles (if needed) and runs the given source code, returning combined stdout/stderr.
	 *
	 * @throws Exception if compilation fails, execution times out, or the process cannot be started
	 */
	String run(String code, String version) throws Exception;
}
