package com.online_compiler.online_compiler_be.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.online_compiler.online_compiler_be.model.dto.SavedFileRequestDto;
import com.online_compiler.online_compiler_be.model.dto.SavedFileResponseDto;
import com.online_compiler.online_compiler_be.model.entity.Folder;
import com.online_compiler.online_compiler_be.model.entity.SavedFile;
import com.online_compiler.online_compiler_be.repository.FolderRepository;
import com.online_compiler.online_compiler_be.repository.SavedFileRepository;
import com.online_compiler.online_compiler_be.repository.UserRepository;
import com.online_compiler.online_compiler_be.security.AppUserPrincipal;

@RestController
@RequestMapping("/v1/files")
public class SavedFileController {

	private final SavedFileRepository savedFileRepository;
	private final FolderRepository folderRepository;
	private final UserRepository userRepository;

	public SavedFileController(SavedFileRepository savedFileRepository, FolderRepository folderRepository,
			UserRepository userRepository) {
		this.savedFileRepository = savedFileRepository;
		this.folderRepository = folderRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<SavedFileResponseDto> listFiles(@AuthenticationPrincipal AppUserPrincipal principal) {
		return savedFileRepository.findByUserIdOrderByUpdatedAtDesc(principal.getId()).stream()
				.map(SavedFileResponseDto::summaryFrom)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<?> getFile(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
		return savedFileRepository.findByIdAndUserId(id, principal.getId())
				.map(file -> ResponseEntity.ok(SavedFileResponseDto.from(file)))
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@PostMapping
	public ResponseEntity<?> createFile(@RequestBody @Valid SavedFileRequestDto request,
			@AuthenticationPrincipal AppUserPrincipal principal) {
		String filename = request.filename().trim();
		Folder folder = resolveOwnedFolder(request.folderId(), principal.getId());
		if (request.folderId() != null && folder == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Folder not found.");
		}
		if (filenameExistsInFolder(principal.getId(), folder, filename)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("A file named '" + filename + "' already exists in this folder. Choose another name.");
		}

		SavedFile file = new SavedFile();
		// getReferenceById avoids an extra SELECT; we only need the id to satisfy the FK.
		file.setUser(userRepository.getReferenceById(principal.getId()));
		file.setFolder(folder);
		file.setFilename(filename);
		file.setLanguage(request.language());
		file.setVersion(request.version());
		file.setCode(request.code());

		SavedFile saved = savedFileRepository.save(file);
		return ResponseEntity.status(HttpStatus.CREATED).body(SavedFileResponseDto.from(saved));
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateFile(@PathVariable Long id, @RequestBody @Valid SavedFileRequestDto request,
			@AuthenticationPrincipal AppUserPrincipal principal) {
		return savedFileRepository.findByIdAndUserId(id, principal.getId())
				.<ResponseEntity<?>>map(file -> {
					String filename = request.filename().trim();
					Folder folder = resolveOwnedFolder(request.folderId(), principal.getId());
					if (request.folderId() != null && folder == null) {
						return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Folder not found.");
					}

					Long currentFolderId = file.getFolder() == null ? null : file.getFolder().getId();
					Long targetFolderId = folder == null ? null : folder.getId();
					boolean movingOrRenaming = !filename.equalsIgnoreCase(file.getFilename())
							|| (currentFolderId == null ? targetFolderId != null : !currentFolderId.equals(targetFolderId));
					if (movingOrRenaming && filenameExistsInFolder(principal.getId(), folder, filename)) {
						return ResponseEntity.status(HttpStatus.CONFLICT)
								.body("A file named '" + filename + "' already exists in this folder. Choose another name.");
					}

					file.setFolder(folder);
					file.setFilename(filename);
					file.setLanguage(request.language());
					file.setVersion(request.version());
					file.setCode(request.code());
					SavedFile saved = savedFileRepository.save(file);
					return ResponseEntity.ok(SavedFileResponseDto.from(saved));
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteFile(@PathVariable Long id, @AuthenticationPrincipal AppUserPrincipal principal) {
		return savedFileRepository.findByIdAndUserId(id, principal.getId())
				.map(file -> {
					savedFileRepository.delete(file);
					return ResponseEntity.noContent().<Void>build();
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	private Folder resolveOwnedFolder(Long folderId, Long userId) {
		if (folderId == null) {
			return null;
		}
		return folderRepository.findByIdAndUserId(folderId, userId).orElse(null);
	}

	private boolean filenameExistsInFolder(Long userId, Folder folder, String filename) {
		if (folder == null) {
			return savedFileRepository.existsByUserIdAndFolderIsNullAndFilenameIgnoreCase(userId, filename);
		}
		return savedFileRepository.existsByUserIdAndFolderIdAndFilenameIgnoreCase(userId, folder.getId(), filename);
	}
}
