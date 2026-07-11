package com.online_compiler.online_compiler_be.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.online_compiler.online_compiler_be.model.dto.RequestDto;
import com.online_compiler.online_compiler_be.service.JavaService;

@RestController
@RequestMapping("/v1/run")
public class JavaController {

	@Autowired
	private JavaService javaService;
	
	@PostMapping
	public ResponseEntity<String> compileAndRun(@RequestBody @Validated RequestDto requestCode) {
		
		String output = "";
		
		
		switch(requestCode.language()) {
			case "java" -> javaService.compileAndRun(requestCode.code());
			//case "cpp" -> cppService.compileAndRun(requestCode.getCode());
		}
		
		return ResponseEntity.ok(output+"execute");
	}
}
