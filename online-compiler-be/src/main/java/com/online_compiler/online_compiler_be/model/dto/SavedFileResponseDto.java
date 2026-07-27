package com.online_compiler.online_compiler_be.model.dto;

import java.time.Instant;

import com.online_compiler.online_compiler_be.model.entity.SavedFile;

public record SavedFileResponseDto(
		Long id,
		String filename,
		String language,
		String version,
		String code,
		Long folderId,
		Instant createdAt,
		Instant updatedAt) {

	public static SavedFileResponseDto from(SavedFile file) {
		return new SavedFileResponseDto(file.getId(), file.getFilename(), file.getLanguage(), file.getVersion(),
				file.getCode(), folderIdOf(file), file.getCreatedAt(), file.getUpdatedAt());
	}

	/** Lighter-weight view for list endpoints: omits the (potentially large) source code. */
	public static SavedFileResponseDto summaryFrom(SavedFile file) {
		return new SavedFileResponseDto(file.getId(), file.getFilename(), file.getLanguage(), file.getVersion(),
				null, folderIdOf(file), file.getCreatedAt(), file.getUpdatedAt());
	}

	private static Long folderIdOf(SavedFile file) {
		return file.getFolder() == null ? null : file.getFolder().getId();
	}
}
