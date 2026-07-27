package com.online_compiler.online_compiler_be.ratelimit;

record RateLimitResult(boolean allowed, long retryAfterSeconds) {
}
