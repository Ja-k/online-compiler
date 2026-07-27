package com.online_compiler.online_compiler_be.ratelimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Holds one {@link TokenBucket} per (rule, client) pair in memory.
 *
 * <p>This is intentionally a simple {@code ConcurrentHashMap}, not
 * Redis-backed: the app runs as a single instance, so there's no need for
 * buckets to be shared/synchronized across nodes. If this backend is ever
 * scaled horizontally behind a load balancer, this map would need to move
 * to Redis (e.g. via Lua-scripted INCR+EXPIRE) so every instance agrees on
 * the same counters - but for now this keeps things dependency-free and
 * fast (no network round-trip per request).
 */
@Service
public class RateLimiterService {

	private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

	/** Buckets idle longer than this are assumed abandoned and are evicted, so
	 * memory doesn't grow forever as new client IPs come and go. */
	private static final long STALE_AFTER_MINUTES = 10;

	/** Floor applied to the reported Retry-After: with continuous refill, a
	 * client could otherwise be told "try again in 1 second", which is barely
	 * a deterrent. This makes throttled clients back off meaningfully. */
	private static final long MIN_RETRY_AFTER_SECONDS = 5;

	private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

	RateLimitResult tryConsume(String key, double capacity, double refillTokensPerMinute) {
		TokenBucket bucket = buckets.computeIfAbsent(key,
				k -> new TokenBucket(capacity, refillTokensPerMinute / 60.0));

		if (bucket.tryConsume()) {
			return new RateLimitResult(true, 0);
		}
		long retryAfterSeconds = Math.max(MIN_RETRY_AFTER_SECONDS, bucket.secondsUntilNextToken());
		return new RateLimitResult(false, retryAfterSeconds);
	}

	@Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
	void evictStaleBuckets() {
		long cutoffMillis = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(STALE_AFTER_MINUTES);
		int sizeBefore = buckets.size();
		buckets.values().removeIf(bucket -> bucket.isStale(cutoffMillis));
		int evicted = sizeBefore - buckets.size();
		if (evicted > 0) {
			log.debug("Evicted {} stale rate-limit buckets ({} remaining)", evicted, buckets.size());
		}
	}
}
