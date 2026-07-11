package com.online_compiler.online_compiler_be;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author Javad Khalili
 */

@SpringBootApplication
public class OnlineCompilerBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineCompilerBeApplication.class, args);
		Map<String, Integer> testMap = new HashMap<>();
	}
}
