package com.online_compiler.online_compiler_be.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
		@NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9_.-]+$", message = "Username may only contain letters, numbers, '.', '_' and '-'") String username,
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8, max = 100) String password) {
}
