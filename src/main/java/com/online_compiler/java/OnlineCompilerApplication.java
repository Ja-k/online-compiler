package com.online_compiler.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class OnlineCompilerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineCompilerApplication.class, args);
	}

}
