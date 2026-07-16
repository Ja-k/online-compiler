package com.online_compiler.online_compiler_be.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.online_compiler.online_compiler_be.model.entity.User;

/** Sends the transactional emails needed for the account verification flow. */
@Service
public class MailService {

	private static final Logger log = LoggerFactory.getLogger(MailService.class);

	private final JavaMailSender mailSender;
	private final String fromAddress;
	private final String frontendBaseUrl;

	public MailService(JavaMailSender mailSender, @Value("${app.mail.from}") String fromAddress,
			@Value("${app.frontend-base-url}") String frontendBaseUrl) {
		this.mailSender = mailSender;
		this.fromAddress = fromAddress;
		this.frontendBaseUrl = frontendBaseUrl;
	}

	public void sendVerificationEmail(User user, String token) {
		String verificationLink = frontendBaseUrl + "/verify-email?token=" + token;

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(user.getEmail());
		message.setSubject("Verify your email for Online Compiler");
		message.setText("Hi " + user.getUsername() + ",\n\n"
				+ "Thanks for signing up for Online Compiler! Please verify your email address by clicking the link below:\n\n"
				+ verificationLink + "\n\n"
				+ "This link will expire in 24 hours.\n\n"
				+ "If you did not create this account, you can safely ignore this email.");

		try {
			mailSender.send(message);
		} catch (Exception e) {
			// Best-effort: a mail-server hiccup shouldn't fail registration/resend
			// outright since the user can always request another verification
			// email once the mail server is reachable again.
			log.warn("Failed to send verification email to {}", user.getEmail(), e);
		}
	}
}
