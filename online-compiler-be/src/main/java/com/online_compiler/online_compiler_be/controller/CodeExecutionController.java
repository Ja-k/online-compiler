package com.online_compiler.online_compiler_be.controller;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.online_compiler.online_compiler_be.model.dto.RequestDto;
import com.online_compiler.online_compiler_be.service.LanguageExecutionService;

@RestController
@RequestMapping("/v1/run")
public class CodeExecutionController {

	private final Map<String, LanguageExecutionService> executionServicesByLanguage;

	public CodeExecutionController(List<LanguageExecutionService> executionServices) {
		this.executionServicesByLanguage = executionServices.stream()
				.collect(Collectors.toMap(LanguageExecutionService::getLanguageId, Function.identity()));
	}

	@PostMapping
	public ResponseEntity<String> compileAndRun(@RequestBody @Validated RequestDto requestCode) {
		if (requestCode.language() == null || requestCode.code() == null || requestCode.code().isBlank()) {
			return ResponseEntity.badRequest().body("Both 'language' and 'code' are required.");
		}

		LanguageExecutionService executionService = executionServicesByLanguage.get(requestCode.language());
		if (executionService == null) {
			return ResponseEntity.badRequest().body("Unsupported language: " + requestCode.language());
		}

		try {
			String output = executionService.run(requestCode.code(), requestCode.version());
			return ResponseEntity.ok(output);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
