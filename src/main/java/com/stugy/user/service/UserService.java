package com.stugy.user.service;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.stugy.common.file.service.FileStorageService;
import com.stugy.user.domain.User;
import com.stugy.user.dto.request.SignUpRequest;
import com.stugy.user.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final FileStorageService fileStorageService;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public User signUp(SignUpRequest request, String createIp) throws IOException {
		validateUniqueFields(request);

		String normalizedPhoneNumber = request.getPhoneNumber().replace("-", "");
		User user = userRepository.saveAndFlush(new User(
				request.getLoginId(),
				request.getEmail(),
				passwordEncoder.encode(request.getPassword()),
				request.getNickname(),
				request.getRegion(),
				request.getBirthDate(),
				normalizedPhoneNumber,
				request.getIntroduction()));
		fileStorageService.replaceProfileImage(user.getId(), request.getProfileImage(), user.getLoginId(), createIp);
		return user;
	}

	@Transactional
	public void replaceProfileImage(String loginId, MultipartFile image, String createIp) throws IOException {
		User user = findByLoginId(loginId);
		fileStorageService.replaceProfileImage(user.getId(), image, user.getLoginId(), createIp);
	}

	@Transactional
	public void deleteProfileImage(String loginId) throws IOException {
		fileStorageService.deleteProfileImage(findByLoginId(loginId).getId());
	}

	private User findByLoginId(String loginId) {
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	private void validateUniqueFields(SignUpRequest request) {
		if (userRepository.existsByLoginId(request.getLoginId())) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
		}
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}
		if (userRepository.existsByNickname(request.getNickname())) {
			throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
		}
		if (userRepository.existsByPhoneNumber(request.getPhoneNumber().replace("-", ""))) {
			throw new IllegalArgumentException("이미 가입된 휴대폰 번호입니다.");
		}
	}
}
