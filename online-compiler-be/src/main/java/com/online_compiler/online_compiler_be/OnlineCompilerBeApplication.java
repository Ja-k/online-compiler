package com.online_compiler.online_compiler_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Author Javad Khalili
 */

@SpringBootApplication
@EnableScheduling
@EnableCaching
@ConfigurationPropertiesScan
public class OnlineCompilerBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineCompilerBeApplication.class, args);
	}
}
