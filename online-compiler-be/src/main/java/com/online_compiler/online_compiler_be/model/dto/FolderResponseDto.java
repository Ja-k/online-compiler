package com.online_compiler.online_compiler_be.model.dto;

import java.time.Instant;

import com.online_compiler.online_compiler_be.model.entity.Folder;

public record FolderResponseDto(Long id, String name, Instant createdAt, Instant updatedAt) {

	public static FolderResponseDto from(Folder folder) {
		return new FolderResponseDto(folder.getId(), folder.getName(), folder.getCreatedAt(), folder.getUpdatedAt());
	}
}
