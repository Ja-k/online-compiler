package com.online_compiler.online_compiler_be.model.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_username", columnNames = "username"),
		@UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String username;

	@Column(nullable = false, length = 254)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	// ColumnDefault ensures existing rows get `false` when this column is
	// added by Hibernate's `ddl-auto: update`, instead of failing the ALTER
	// TABLE under MySQL's strict mode (which rejects NOT NULL without a
	// default on a non-empty table).
	@Column(name = "email_verified", nullable = false)
	@ColumnDefault("false")
	private boolean emailVerified = false;

	@Column(name = "verification_token", length = 64)
	private String verificationToken;

	@Column(name = "verification_token_expires_at")
	private Instant verificationTokenExpiresAt;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SavedFile> savedFiles = new ArrayList<>();

	public User() {
	}

	public User(String username, String email, String passwordHash) {
		this.username = username;
		this.email = email;
		this.passwordHash = passwordHash;
	}

	@jakarta.persistence.PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public String getVerificationToken() {
		return verificationToken;
	}

	public void setVerificationToken(String verificationToken) {
		this.verificationToken = verificationToken;
	}

	public Instant getVerificationTokenExpiresAt() {
		return verificationTokenExpiresAt;
	}

	public void setVerificationTokenExpiresAt(Instant verificationTokenExpiresAt) {
		this.verificationTokenExpiresAt = verificationTokenExpiresAt;
	}
}
