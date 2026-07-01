package com.stugy.user.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stugy.user.domain.User;
import com.stugy.user.dto.request.SignUpRequest;
import com.stugy.user.dto.request.UpdateProfileRequest;
import com.stugy.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Map<String, Object> signUp(@Valid @ModelAttribute SignUpRequest request, HttpServletRequest servletRequest) throws IOException {
		User user = userService.signUp(request, servletRequest.getRemoteAddr());
		return Map.of("id", user.getId(), "nickname", user.getNickname());
	}

	@PostMapping("/me/profile-image")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void replaceProfileImage(@RequestParam("profileImage") MultipartFile profileImage,
			Authentication authentication, HttpServletRequest servletRequest) throws IOException {
		userService.replaceProfileImage(authentication.getName(), profileImage, servletRequest.getRemoteAddr());
	}

	@PutMapping("/me")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void updateProfile(@Valid @ModelAttribute UpdateProfileRequest request, Authentication authentication,
			HttpServletRequest servletRequest) throws IOException {
		userService.updateProfile(authentication.getName(), request, servletRequest.getRemoteAddr());
	}

	@DeleteMapping("/me/profile-image")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteProfileImage(Authentication authentication) throws IOException {
		userService.deleteProfileImage(authentication.getName());
	}
}
