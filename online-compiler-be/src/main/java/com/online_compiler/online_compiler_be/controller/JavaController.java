package com.online_compiler.online_compiler_be.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.online_compiler.online_compiler_be.service.JavaService;

@RestController
@RequestMapping("/v1/java")
public class JavaController {

	@Autowired
	private JavaService javaService;
	
	@PostMapping
	public ResponseEntity<Object> compileAndRun(@RequestBody Object input) {
		String output = javaService.compileAndRun(input.toString());
		return ResponseEntity.ok("compiled and ran java code you sent to me!!!");
	}
}
