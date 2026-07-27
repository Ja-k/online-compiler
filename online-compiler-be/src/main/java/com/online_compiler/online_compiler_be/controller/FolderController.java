package com.online_compiler.online_compiler_be.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.online_compiler.online_compiler_be.model.dto.FolderRequestDto;
import com.online_compiler.online_compiler_be.model.dto.FolderResponseDto;
import com.online_compiler.online_compiler_be.model.entity.Folder;
import com.online_compiler.online_compiler_be.repository.FolderRepository;
import com.online_compiler.online_compiler_be.repository.SavedFileRepository;
import com.online_compiler.online_compiler_be.repository.UserRepository;
import com.online_compiler.online_compiler_be.security.AppUserPrincipal;

@RestController
@RequestMapping("/v1/folders")
public class FolderController {

	private final FolderRepository folderRepository;
	private final SavedFileRepository savedFileRepository;
	private final UserRepository userRepository;

	public FolderController(FolderRepository folderRepository, SavedFileRepository savedFileRepository,
			UserRepository userRepository) {
		this.folderRepository = folderRepository;
		this.savedFileRepository = savedFileRepository;
		this.userRepository = userRepository;
	}

	@GetMapping
	public List<FolderResponseDto> listFolders(@AuthenticationPrincipal AppUserPrincipal principal) {
		return folderRepository.findByUserIdOrderByNameAsc(principal.getId()).stream()
				.map(FolderResponseDto::from)
				.toList();
	}

	@PostMapping
	public ResponseEntity<?> createFolder(@RequestBody @Valid FolderRequestDto request,
			@AuthenticationPrincipal AppUserPrincipal principal) {
		String name = request.name().trim();
		if (name.isEmpty()) {
			return ResponseEntity.badRequest().body("Folder name is required.");
		}
		if (folderRepository.existsByUserIdAndNameIgnoreCase(principal.getId(), name)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("A folder named '" + name + "' already exists. Choose another name.");
		}

		Folder folder = new Folder();
		folder.setUser(userRepository.getReferenceById(principal.getId()));
		folder.setName(name);
		Folder saved = folderRepository.save(folder);
		return ResponseEntity.status(HttpStatus.CREATED).body(FolderResponseDto.from(saved));
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> renameFolder(@PathVariable Long id, @RequestBody @Valid FolderRequestDto request,
			@AuthenticationPrincipal AppUserPrincipal principal) {
		return folderRepository.findByIdAndUserId(id, principal.getId())
				.map(folder -> {
					String name = request.name().trim();
					if (name.isEmpty()) {
						return ResponseEntity.badRequest().body("Folder name is required.");
					}
					if (!name.equalsIgnoreCase(folder.getName())
							&& folderRepository.existsByUserIdAndNameIgnoreCase(principal.getId(), name)) {
						return ResponseEntity.status(HttpStatus.CONFLICT)
								.body("A folder named '" + name + "' already exists. Choose another name.");
					}
					folder.setName(name);
					Folder saved = folderRepository.save(folder);
					return ResponseEntity.ok(FolderResponseDto.from(saved));
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}

	/**
	 * Deletes the folder and every file inside it. The frontend shows a confirm
	 * dialog before calling this, since the cascade is intentional and irreversible.
	 * {@code @Transactional} is required because Spring Data's derived
	 * {@code deleteBy...} methods need an active EntityManager transaction.
	 */
	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> deleteFolder(@PathVariable Long id,
			@AuthenticationPrincipal AppUserPrincipal principal) {
		return folderRepository.findByIdAndUserId(id, principal.getId())
				.map(folder -> {
					savedFileRepository.deleteByUserIdAndFolderId(principal.getId(), folder.getId());
					folderRepository.delete(folder);
					return ResponseEntity.noContent().<Void>build();
				})
				.orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
	}
}
