package com.online_compiler.online_compiler_be.model.dto;

import jakarta.validation.constraints.NotBlank;

/** Accepts either a username or an email, since the login page only has a username field. */
public record ResendVerificationRequestDto(@NotBlank String usernameOrEmail) {
}
