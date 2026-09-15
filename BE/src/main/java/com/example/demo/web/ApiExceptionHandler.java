package com.example.demo.web;

import com.example.demo.dto.ApiError;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ConflictException;
import com.example.demo.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException exception) {
		return build(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException exception) {
		return build(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(BadRequestException exception) {
		return build(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.reduce((a, b) -> a + "; " + b)
				.orElse("Richiesta non valida");

		return build(HttpStatus.BAD_REQUEST, message);
	}

	private ResponseEntity<ApiError> build(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(ApiError.of(status.value(), status.getReasonPhrase(), message));
	}
}
