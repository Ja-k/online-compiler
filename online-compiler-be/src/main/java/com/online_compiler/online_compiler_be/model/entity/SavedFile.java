package com.online_compiler.online_compiler_be.model.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Uniqueness of {@code filename} within a folder (or at root when
 * {@code folder} is null) is enforced in {@code SavedFileController}, not via
 * a DB unique constraint: MySQL treats each NULL {@code folder_id} as
 * distinct in a unique index, so a (user_id, folder_id, filename) constraint
 * would not reliably block duplicate root-level names.
 */
@Entity
@Table(name = "saved_files")
public class SavedFile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	/** Null means the file lives at the root (not inside any folder). Eager so
	 * list/detail DTOs can always read {@code folderId} without an open
	 * persistence context (open-in-view is disabled). */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "folder_id")
	private Folder folder;

	@Column(nullable = false, length = 120)
	private String filename;

	@Column(nullable = false, length = 30)
	private String language;

	@Column(nullable = false, length = 20)
	private String version;

	@Lob
	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String code;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public SavedFile() {
	}

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Folder getFolder() {
		return folder;
	}

	public void setFolder(Folder folder) {
		this.folder = folder;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
