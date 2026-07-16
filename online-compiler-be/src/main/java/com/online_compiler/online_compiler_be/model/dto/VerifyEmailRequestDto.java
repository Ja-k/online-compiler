package com.online_compiler.online_compiler_be.model.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequestDto(@NotBlank String token) {
}
