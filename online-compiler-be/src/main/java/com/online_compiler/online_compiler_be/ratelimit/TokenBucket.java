package com.online_compiler.online_compiler_be.ratelimit;

/**
 * Classic token-bucket rate limiter for a single client/rule pair.
 *
 * <p>Picture a bucket that holds up to {@code capacity} tokens. It starts
 * full. Every request must take one token to proceed; if the bucket is
 * empty, the request is rejected. Tokens trickle back in continuously at
 * {@code refillTokensPerSecond}, so a client that bursts through its whole
 * capacity has to wait for tokens to regenerate before it can make more
 * requests - but a client that spaces requests out never gets blocked at
 * all. This is nicer than a naive "N requests per fixed minute window"
 * counter, which allows a full burst right at the boundary of two windows
 * (2N requests in a few seconds).
 *
 * <p>Refilling is computed lazily on each access (elapsed time since the
 * last refill * rate), rather than via a background thread per bucket, so
 * this stays cheap even with many distinct clients.
 */
class TokenBucket {

	private final double capacity;
	private final double refillTokensPerNano;

	private double availableTokens;
	private long lastRefillNanos;
	private volatile long lastAccessMillis;

	TokenBucket(double capacity, double refillTokensPerSecond) {
		this.capacity = capacity;
		this.refillTokensPerNano = refillTokensPerSecond / 1_000_000_000.0;
		this.availableTokens = capacity;
		this.lastRefillNanos = System.nanoTime();
		this.lastAccessMillis = System.currentTimeMillis();
	}

	/** Attempts to take one token. Returns whether the request may proceed. */
	synchronized boolean tryConsume() {
		lastAccessMillis = System.currentTimeMillis();
		refill();
		if (availableTokens >= 1.0) {
			availableTokens -= 1.0;
			return true;
		}
		return false;
	}

	/** How long the caller should wait before the next token becomes available. */
	synchronized long secondsUntilNextToken() {
		refill();
		if (availableTokens >= 1.0) {
			return 0;
		}
		double missingTokens = 1.0 - availableTokens;
		double secondsNeeded = missingTokens / (refillTokensPerNano * 1_000_000_000.0);
		return (long) Math.ceil(secondsNeeded);
	}

	/** Used by {@link RateLimiterService} to garbage-collect buckets for clients that went away. */
	boolean isStale(long cutoffMillis) {
		return lastAccessMillis < cutoffMillis;
	}

	private void refill() {
		long now = System.nanoTime();
		long elapsedNanos = now - lastRefillNanos;
		if (elapsedNanos <= 0) {
			return;
		}
		availableTokens = Math.min(capacity, availableTokens + elapsedNanos * refillTokensPerNano);
		lastRefillNanos = now;
	}
}
