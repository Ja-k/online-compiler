package com.online_compiler.online_compiler_be.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SavedFileRequestDto(
		@NotBlank @Size(max = 120) String filename,
		@NotBlank String language,
		@NotBlank String version,
		@NotBlank String code,
		/** Null (or omitted) places the file at the root; otherwise the owning folder id. */
		Long folderId) {
}
