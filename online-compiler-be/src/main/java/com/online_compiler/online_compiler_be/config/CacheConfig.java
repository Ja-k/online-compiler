package com.online_compiler.online_compiler_be.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Spring Boot auto-configures a {@code RedisCacheManager} as soon as
 * spring-boot-starter-data-redis is on the classpath and {@code @EnableCaching}
 * is present - no manual bean wiring is strictly required to get caching
 * working. This class only tweaks that auto-configured manager for the
 * specific "code-execution" cache:
 *
 * <ul>
 * <li>a finite TTL, so cached results (and the memory they use in Redis)
 * don't live forever;</li>
 * <li>a plain string value serializer instead of the default Java
 * serialization, so entries stay human-readable if you inspect them with
 * {@code redis-cli GET code-execution::&lt;key&gt;} - the cached value here
 * is always just the program's output text.</li>
 * </ul>
 */
@Configuration
public class CacheConfig {

	@Bean
	public RedisCacheManagerBuilderCustomizer codeExecutionCacheCustomizer(
			@Value("${app.cache.code-execution.ttl-hours}") long ttlHours) {
		RedisCacheConfiguration codeExecutionConfig = RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(Duration.ofHours(ttlHours))
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

		return builder -> builder.withCacheConfiguration("code-execution", codeExecutionConfig);
	}
}
