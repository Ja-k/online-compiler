package com.online_compiler.online_compiler_be.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.online_compiler.online_compiler_be.model.dto.RequestDto;
import com.online_compiler.online_compiler_be.service.CachedCodeExecutionService;

@RestController
@RequestMapping("/v1/run")
public class CodeExecutionController {

	private final CachedCodeExecutionService executionService;

	public CodeExecutionController(CachedCodeExecutionService executionService) {
		this.executionService = executionService;
	}

	@PostMapping
	public ResponseEntity<String> compileAndRun(@RequestBody @Validated RequestDto requestCode) {
		if (requestCode.language() == null || requestCode.code() == null || requestCode.code().isBlank()) {
			return ResponseEntity.badRequest().body("Both 'language' and 'code' are required.");
		}

		if (!executionService.isSupported(requestCode.language())) {
			return ResponseEntity.badRequest().body("Unsupported language: " + requestCode.language());
		}

		try {
			String output = executionService.run(requestCode.language(), requestCode.version(), requestCode.code());
			return ResponseEntity.ok(output);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
