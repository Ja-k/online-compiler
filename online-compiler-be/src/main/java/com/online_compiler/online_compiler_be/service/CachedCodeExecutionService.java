package com.online_compiler.online_compiler_be.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.service.support.CodeFingerprint;

/**
 * Wraps the per-language {@link LanguageExecutionService} beans with a
 * result cache, so re-running identical code doesn't spin up another Docker
 * container.
 *
 * <p>This is a separate bean (rather than adding {@code @Cacheable} to
 * {@link com.online_compiler.online_compiler_be.controller.CodeExecutionController}
 * directly) specifically so the caching proxy is actually invoked: Spring's
 * caching (like most of its AOP features) works by wrapping this bean in a
 * proxy that intercepts external calls to {@link #run}. A call from
 * <em>within</em> the same class to another {@code @Cacheable} method on
 * {@code this} would skip the proxy and silently bypass the cache
 * altogether - keeping this logic in its own bean, called only from
 * outside, avoids that trap entirely.
 */
@Service
public class CachedCodeExecutionService {

	private final Map<String, LanguageExecutionService> executionServicesByLanguage;

	public CachedCodeExecutionService(List<LanguageExecutionService> executionServices) {
		this.executionServicesByLanguage = executionServices.stream()
				.collect(Collectors.toMap(LanguageExecutionService::getLanguageId, Function.identity()));
	}

	public boolean isSupported(String language) {
		return executionServicesByLanguage.containsKey(language);
	}

	/**
	 * Only reaches the Docker-backed executor on a cache miss. The cache key
	 * is a SHA-256 fingerprint of (language, version, code) - see
	 * {@link CodeFingerprint} - rather than the default "all arguments"
	 * key, so the potentially large source string isn't duplicated as a raw
	 * Redis key.
	 *
	 * <p>Only a normal return value is cached; if the underlying execution
	 * throws (compile/runtime errors are surfaced as exceptions further
	 * down, see {@code DockerCommandRunner}), Spring's caching deliberately
	 * skips caching that outcome, so a one-off Docker/timeout hiccup is
	 * never "remembered" as the permanent result for that code.
	 */
	@Cacheable(cacheNames = "code-execution",
			key = "T(com.online_compiler.online_compiler_be.service.support.CodeFingerprint).of(#language, #version, #code)")
	public String run(String language, String version, String code) throws Exception {
		return executionServicesByLanguage.get(language).run(code, version);
	}
}
