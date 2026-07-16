package com.online_compiler.online_compiler_be.model.dto;

/**
 * Structured error body with a stable machine-readable {@code code} (e.g.
 * {@code EMAIL_NOT_VERIFIED}) so the frontend can branch on it, plus a
 * human-readable {@code message} for display.
 */
public record ErrorResponseDto(String code, String message) {
}
