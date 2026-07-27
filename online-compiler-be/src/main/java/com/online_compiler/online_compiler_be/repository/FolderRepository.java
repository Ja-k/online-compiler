package com.online_compiler.online_compiler_be.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.online_compiler.online_compiler_be.model.entity.Folder;

public interface FolderRepository extends JpaRepository<Folder, Long> {

	List<Folder> findByUserIdOrderByNameAsc(Long userId);

	Optional<Folder> findByIdAndUserId(Long id, Long userId);

	boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);
}
