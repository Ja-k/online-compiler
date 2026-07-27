package com.online_compiler.online_compiler_be.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Maps to {@code app.rate-limit.*} in application.yaml. Each rule is a
 * token-bucket: {@code capacity} is the burst size (how many requests can
 * fire back-to-back before throttling kicks in), {@code refillPerMinute}
 * is the sustained rate the bucket regenerates at afterwards.
 *
 * <p>Registered via {@code @ConfigurationPropertiesScan} on the main
 * application class rather than plain {@code @Component}: constructor
 * binding for {@code @ConfigurationProperties} needs that dedicated
 * registration path, otherwise Spring tries to satisfy the constructor via
 * normal bean autowiring and fails to find a {@code Rule} bean.
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(Rule run, Rule auth) {

	public record Rule(double capacity, double refillPerMinute) {
	}
}
