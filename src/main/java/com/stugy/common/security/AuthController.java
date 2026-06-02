package com.stugy.common.security;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stugy.user.domain.User;
import com.stugy.user.repository.UserRepository;
import com.stugy.common.file.service.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final FileStorageService fileStorageService;
	private final TokenBasedRememberMeServices rememberMeServices;

	public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
			FileStorageService fileStorageService, TokenBasedRememberMeServices rememberMeServices) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.fileStorageService = fileStorageService;
		this.rememberMeServices = rememberMeServices;
	}

	@PostMapping("/login")
	public Map<String, Object> login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest,
			HttpServletResponse servletResponse) {
		Authentication authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken.unauthenticated(request.loginId(), request.password()));
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		servletRequest.getSession(true).setAttribute(
				HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
		if (request.rememberMe()) {
			rememberMeServices.loginSuccess(servletRequest, servletResponse, authentication);
		}
		return currentUser(authentication);
	}

	@GetMapping("/me")
	public Map<String, Object> me(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
			return Map.of("authenticated", false);
		}
		return currentUser(authentication);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		rememberMeServices.logout(request, response, authentication);
		if (authentication != null) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.invalidate();
			}
			SecurityContextHolder.clearContext();
		}
	}

	private Map<String, Object> currentUser(Authentication authentication) {
		User user = userRepository.findByLoginId(authentication.getName()).orElseThrow();
		Long thumbnailId = fileStorageService.findProfileThumbnailId(user.getId());
		return Map.of("authenticated", true, "id", user.getId(), "loginId", user.getLoginId(), "nickname", user.getNickname(),
				"profileThumbnailUrl", thumbnailId == null ? "" : "/api/files/" + thumbnailId);
	}
}
