package com.online_compiler.java.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/v1/java")
@RestController
public class JavaController {

	@PostMapping
	public ResponseEntity<Object> compileJavaCode() {
		return ResponseEntity.ok().body("compilation succeeded!!!");
	}

}
