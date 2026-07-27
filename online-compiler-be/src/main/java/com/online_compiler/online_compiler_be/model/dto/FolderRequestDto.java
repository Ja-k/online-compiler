package com.online_compiler.online_compiler_be.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FolderRequestDto(@NotBlank @Size(max = 120) String name) {
}
