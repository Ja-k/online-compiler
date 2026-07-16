package com.online_compiler.online_compiler_be.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.online_compiler.online_compiler_be.model.entity.User;

/** Adapts our {@link User} entity to Spring Security's {@link UserDetails}. */
public class AppUserPrincipal implements UserDetails {

	private final User user;

	public AppUserPrincipal(User user) {
		this.user = user;
	}

	public Long getId() {
		return user.getId();
	}

	public User getUser() {
		return user;
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public String getPassword() {
		return user.getPasswordHash();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
