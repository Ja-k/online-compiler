package com.online_compiler.online_compiler_be.model.dto;

import com.online_compiler.online_compiler_be.model.entity.User;

public record AuthUserResponseDto(Long id, String username, String email) {

	public static AuthUserResponseDto from(User user) {
		return new AuthUserResponseDto(user.getId(), user.getUsername(), user.getEmail());
	}
}
