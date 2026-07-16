package com.online_compiler.online_compiler_be.model.dto;

import java.time.Instant;

import com.online_compiler.online_compiler_be.model.entity.SavedFile;

public record SavedFileResponseDto(
		Long id,
		String filename,
		String language,
		String version,
		String code,
		Instant createdAt,
		Instant updatedAt) {

	public static SavedFileResponseDto from(SavedFile file) {
		return new SavedFileResponseDto(file.getId(), file.getFilename(), file.getLanguage(), file.getVersion(),
				file.getCode(), file.getCreatedAt(), file.getUpdatedAt());
	}

	/** Lighter-weight view for list endpoints: omits the (potentially large) source code. */
	public static SavedFileResponseDto summaryFrom(SavedFile file) {
		return new SavedFileResponseDto(file.getId(), file.getFilename(), file.getLanguage(), file.getVersion(),
				null, file.getCreatedAt(), file.getUpdatedAt());
	}
}
