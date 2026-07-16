package com.online_compiler.online_compiler_be.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.online_compiler.online_compiler_be.model.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsernameIgnoreCase(String username);

	Optional<User> findByVerificationToken(String verificationToken);

	Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(String username, String email);

	boolean existsByUsernameIgnoreCase(String username);

	boolean existsByEmailIgnoreCase(String email);
}
