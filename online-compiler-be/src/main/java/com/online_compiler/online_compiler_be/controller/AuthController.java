package com.online_compiler.online_compiler_be.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.online_compiler.online_compiler_be.model.dto.AuthUserResponseDto;
import com.online_compiler.online_compiler_be.model.dto.ErrorResponseDto;
import com.online_compiler.online_compiler_be.model.dto.LoginRequestDto;
import com.online_compiler.online_compiler_be.model.dto.MessageResponseDto;
import com.online_compiler.online_compiler_be.model.dto.RegisterRequestDto;
import com.online_compiler.online_compiler_be.model.dto.ResendVerificationRequestDto;
import com.online_compiler.online_compiler_be.model.dto.VerifyEmailRequestDto;
import com.online_compiler.online_compiler_be.model.entity.User;
import com.online_compiler.online_compiler_be.repository.UserRepository;
import com.online_compiler.online_compiler_be.security.AppUserPrincipal;
import com.online_compiler.online_compiler_be.service.MailService;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

	/**
	 * Generic message used for the resend-verification response regardless of
	 * whether the account exists or is already verified, so the endpoint can't
	 * be used to enumerate registered emails/usernames.
	 */
	private static final String RESEND_GENERIC_MESSAGE = "If an account exists for that username/email and needs verification, we've sent a new verification link.";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final MailService mailService;
	private final long verificationTokenValidityHours;
	private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

	public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, MailService mailService,
			@Value("${app.auth.verification-token-validity-hours}") long verificationTokenValidityHours) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.mailService = mailService;
		this.verificationTokenValidityHours = verificationTokenValidityHours;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody @Valid RegisterRequestDto request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		if (userRepository.existsByUsernameIgnoreCase(request.username())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ErrorResponseDto("USERNAME_TAKEN", "Username is already taken."));
		}
		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ErrorResponseDto("EMAIL_TAKEN", "Email is already registered."));
		}

		User user = new User(request.username(), request.email(), passwordEncoder.encode(request.password()));
		assignNewVerificationToken(user);
		try {
			user = userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT)
					.body(new ErrorResponseDto("USERNAME_OR_EMAIL_TAKEN", "Username or email is already taken."));
		}

		mailService.sendVerificationEmail(user, user.getVerificationToken());

		// Registration no longer logs the user in directly: they must verify
		// their email address first (see login()).
		return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponseDto(
				"Account created! We've sent a verification link to " + user.getEmail() + "."));
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		try {
			Authentication authRequest = new UsernamePasswordAuthenticationToken(request.username(),
					request.password());
			Authentication authResult = authenticationManager.authenticate(authRequest);
			AppUserPrincipal principal = (AppUserPrincipal) authResult.getPrincipal();

			if (!principal.getUser().isEmailVerified()) {
				return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponseDto("EMAIL_NOT_VERIFIED",
						"Please verify your email address before logging in."));
			}

			bindSession(principal, httpRequest, httpResponse);
			return ResponseEntity.ok(AuthUserResponseDto.from(principal.getUser()));
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(new ErrorResponseDto("INVALID_CREDENTIALS", "Invalid username or password."));
		}
	}

	@PostMapping("/verify-email")
	public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyEmailRequestDto request) {
		User user = userRepository.findByVerificationToken(request.token()).orElse(null);
		if (user == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ErrorResponseDto("INVALID_TOKEN", "This verification link is invalid."));
		}
		if (user.isEmailVerified()) {
			return ResponseEntity.ok(new MessageResponseDto("Your email is already verified. You can log in now."));
		}
		if (user.getVerificationTokenExpiresAt() == null || user.getVerificationTokenExpiresAt().isBefore(Instant.now())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("TOKEN_EXPIRED",
					"This verification link has expired. Please request a new one."));
		}

		user.setEmailVerified(true);
		user.setVerificationToken(null);
		user.setVerificationTokenExpiresAt(null);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponseDto("Your email has been verified. You can log in now."));
	}

	@PostMapping("/resend-verification")
	public ResponseEntity<MessageResponseDto> resendVerification(
			@RequestBody @Valid ResendVerificationRequestDto request) {
		userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(request.usernameOrEmail(), request.usernameOrEmail())
				.filter(user -> !user.isEmailVerified())
				.ifPresent(user -> {
					assignNewVerificationToken(user);
					userRepository.save(user);
					mailService.sendVerificationEmail(user, user.getVerificationToken());
				});

		// Always return the same generic message, whether or not an account
		// was found/eligible, to avoid leaking account existence.
		return ResponseEntity.ok(new MessageResponseDto(RESEND_GENERIC_MESSAGE));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		SecurityContextHolder.clearContext();
		var session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	public ResponseEntity<AuthUserResponseDto> me(Authentication authentication) {
		AppUserPrincipal principal = (AppUserPrincipal) authentication.getPrincipal();
		return ResponseEntity.ok(AuthUserResponseDto.from(principal.getUser()));
	}

	private void assignNewVerificationToken(User user) {
		user.setVerificationToken(UUID.randomUUID().toString());
		user.setVerificationTokenExpiresAt(Instant.now().plus(verificationTokenValidityHours, ChronoUnit.HOURS));
	}

	private void bindSession(AppUserPrincipal principal, HttpServletRequest request, HttpServletResponse response) {
		Authentication authenticated = new UsernamePasswordAuthenticationToken(principal, null,
				principal.getAuthorities());

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authenticated);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);
	}
}
