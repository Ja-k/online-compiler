package com.online_compiler.online_compiler_be.ratelimit;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.online_compiler.online_compiler_be.model.dto.ErrorResponseDto;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Throttles two endpoint families before any other processing happens:
 *
 * <ul>
 * <li>{@code /v1/run} - each request spins up a real Docker container to
 * compile/execute untrusted code, making it by far the most expensive
 * (and most attractive to abuse) endpoint in the app.</li>
 * <li>register/login/verify-email/resend-verification - classic
 * brute-force/spam targets, since they accept attacker-supplied
 * credentials or trigger an email send.</li>
 * </ul>
 *
 * <p>Deliberately NOT included: {@code /v1/auth/me} and
 * {@code /v1/auth/logout}. Those are cheap, side-effect-light session
 * checks that the frontend calls on every page load (see
 * {@code AppComponent.ngOnInit} -&gt; {@code restoreSession()}) - sharing a
 * quota with them would silently burn through a legitimate user's login
 * attempts every time they refresh the page, without them ever being told
 * why.</p>
 *
 * <p>Implemented as a plain servlet filter (not a Spring Security
 * mechanism) and given {@link Ordered#HIGHEST_PRECEDENCE} so it runs
 * before Spring Security's filter chain, session handling, or controller
 * dispatch - an abusive client gets rejected as cheaply as possible,
 * without spending any effort on authentication or business logic.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter implements Ordered {

	private static final int HTTP_TOO_MANY_REQUESTS = 429;

	/** The only auth endpoints that accept attacker-supplied input or trigger
	 * an email send - the actual brute-force/spam targets. */
	private static final Set<String> RATE_LIMITED_AUTH_PATHS = Set.of("/v1/auth/register", "/v1/auth/login",
			"/v1/auth/verify-email", "/v1/auth/resend-verification");

	private final RateLimiterService rateLimiterService;
	private final RateLimitProperties properties;
	private final ObjectMapper objectMapper;

	public RateLimitFilter(RateLimiterService rateLimiterService, RateLimitProperties properties,
			ObjectMapper objectMapper) {
		this.rateLimiterService = rateLimiterService;
		this.properties = properties;
		this.objectMapper = objectMapper;
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String path = request.getRequestURI();
		RateLimitProperties.Rule rule = resolveRule(path);

		if (rule != null) {
			String bucketKey = ruleNameFor(path) + ':' + clientKey(request);
			RateLimitResult result = rateLimiterService.tryConsume(bucketKey, rule.capacity(), rule.refillPerMinute());

			if (!result.allowed()) {
				respondTooManyRequests(response, result.retryAfterSeconds());
				return;
			}
		}

		chain.doFilter(request, response);
	}

	private RateLimitProperties.Rule resolveRule(String path) {
		if (path.startsWith("/v1/run")) {
			return properties.run();
		}
		if (RATE_LIMITED_AUTH_PATHS.contains(path)) {
			return properties.auth();
		}
		return null;
	}

	private String ruleNameFor(String path) {
		return path.startsWith("/v1/run") ? "run" : "auth";
	}

	/**
	 * Prefers {@code X-Forwarded-For} (set by the nginx reverse proxy in front
	 * of the production container) so clients aren't all lumped under the
	 * proxy's own IP; falls back to the direct connection's address for local
	 * dev, where there's no proxy in between.
	 */
	private String clientKey(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private void respondTooManyRequests(HttpServletResponse response, long retryAfterSeconds) throws IOException {
		response.setStatus(HTTP_TOO_MANY_REQUESTS);
		response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
		response.setContentType("application/json");
		ErrorResponseDto body = new ErrorResponseDto("RATE_LIMITED",
				"Too many requests. Please try again in " + retryAfterSeconds + " second"
						+ (retryAfterSeconds == 1 ? "" : "s") + ".");
		response.getWriter().write(objectMapper.writeValueAsString(body));
	}
}
