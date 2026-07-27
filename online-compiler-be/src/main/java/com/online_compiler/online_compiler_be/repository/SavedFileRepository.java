package com.online_compiler.online_compiler_be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.online_compiler.online_compiler_be.model.entity.SavedFile;

public interface SavedFileRepository extends JpaRepository<SavedFile, Long> {

	List<SavedFile> findByUserIdOrderByUpdatedAtDesc(Long userId);

	Optional<SavedFile> findByIdAndUserId(Long id, Long userId);

	boolean existsByUserIdAndFolderIdAndFilenameIgnoreCase(Long userId, Long folderId, String filename);

	boolean existsByUserIdAndFolderIsNullAndFilenameIgnoreCase(Long userId, String filename);

	void deleteByUserIdAndFolderId(Long userId, Long folderId);
}
