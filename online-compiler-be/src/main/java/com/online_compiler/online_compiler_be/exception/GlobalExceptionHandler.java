package com.online_compiler.online_compiler_be.exception;

import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.online_compiler.online_compiler_be.model.dto.ErrorResponseDto;

/**
 * Turns Spring's default {@link MethodArgumentNotValidException} handling
 * (a verbose body with a full stack trace, meant for API exploration) into
 * the same clean {@code {code, message}} shape the rest of the API uses,
 * for every {@code @Valid}-annotated request body across all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponseDto> handleValidationException(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.sorted(Comparator.comparing(FieldError::getField))
				.map(error -> capitalize(error.getField()) + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));

		if (message.isBlank()) {
			message = "Invalid request.";
		}

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("VALIDATION_ERROR", message));
	}

	private static String capitalize(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
