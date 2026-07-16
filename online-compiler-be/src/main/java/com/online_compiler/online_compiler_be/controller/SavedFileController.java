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
import com.online_compiler.online_compiler_be.model.entity.SavedFile;
import com.online_compiler.online_compiler_be.repository.SavedFileRepository;
import com.online_compiler.online_compiler_be.repository.UserRepository;
import com.online_compiler.online_compiler_be.security.AppUserPrincipal;

@RestController
@RequestMapping("/v1/files")
public class SavedFileController {

	private final SavedFileRepository savedFileRepository;
	private final UserRepository userRepository;

	public SavedFileController(SavedFileRepository savedFileRepository, UserRepository userRepository) {
		this.savedFileRepository = savedFileRepository;
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
		if (savedFileRepository.existsByUserIdAndFilenameIgnoreCase(principal.getId(), filename)) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body("A file named '" + filename + "' already exists. Choose another name.");
		}

		SavedFile file = new SavedFile();
		// getReferenceById avoids an extra SELECT; we only need the id to satisfy the FK.
		file.setUser(userRepository.getReferenceById(principal.getId()));
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
				.map(file -> {
					String filename = request.filename().trim();
					if (!filename.equalsIgnoreCase(file.getFilename())
							&& savedFileRepository.existsByUserIdAndFilenameIgnoreCase(principal.getId(), filename)) {
						return ResponseEntity.status(HttpStatus.CONFLICT)
								.body("A file named '" + filename + "' already exists. Choose another name.");
					}
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
}
