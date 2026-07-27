package com.online_compiler.online_compiler_be.service.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Builds a compact, stable cache key for a (language, version, code) triple.
 *
 * <p>The source code itself can be arbitrarily long and contain any
 * character (newlines, colons, unicode, ...), so it's hashed with SHA-256
 * rather than embedded in the key directly. Two requests for the exact same
 * code produce the exact same fingerprint; a single-character change
 * produces a completely different one, which is exactly the property a
 * cache key needs.
 */
public final class CodeFingerprint {

	private CodeFingerprint() {
	}

	public static String of(String language, String version, String code) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));
			String versionPart = version == null || version.isBlank() ? "default" : version;
			return language + ':' + versionPart + ':' + HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 should always be available on the JVM", e);
		}
	}
}
