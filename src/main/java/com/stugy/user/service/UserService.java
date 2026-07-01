package com.stugy.user.service;

import java.io.IOException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.stugy.common.file.service.FileStorageService;
import com.stugy.user.domain.User;
import com.stugy.user.dto.request.SignUpRequest;
import com.stugy.user.dto.request.UpdateProfileRequest;
import com.stugy.user.mapper.UserMapper;

@Service
public class UserService {

	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final FileStorageService fileStorageService;

	public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, FileStorageService fileStorageService) {
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
		this.fileStorageService = fileStorageService;
	}

	@Transactional
	public User signUp(SignUpRequest request, String createIp) throws IOException {
		validateUniqueFields(request);

		String normalizedPhoneNumber = request.getPhoneNumber().replace("-", "");
		User user = new User(
				request.getLoginId(),
				request.getEmail(),
				passwordEncoder.encode(request.getPassword()),
				request.getNickname(),
				request.getRegion(),
				request.getBirthDate(),
				normalizedPhoneNumber,
				request.getIntroduction());
		userMapper.insertUser(user);
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

	@Transactional
	public User updateProfile(String loginId, UpdateProfileRequest request, String createIp) throws IOException {
		User user = findByLoginId(loginId);
		String normalizedPhoneNumber = request.getPhoneNumber().replace("-", "");
		validateUniqueProfileFields(user, request, normalizedPhoneNumber);
		user.updateProfile(
				request.getEmail(),
				request.getNickname(),
				request.getRegion(),
				request.getBirthDate(),
				normalizedPhoneNumber,
				request.getIntroduction());
		userMapper.updateUserProfile(user);
		fileStorageService.replaceProfileImage(
				user.getId(), request.getProfileImage(), user.getLoginId(), createIp);
		return user;
	}

	private User findByLoginId(String loginId) {
		return userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	private void validateUniqueFields(SignUpRequest request) {
		if (userMapper.selectExistsByLoginId(request.getLoginId())) {
			throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
		}
		if (userMapper.selectExistsByEmail(request.getEmail())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}
		if (userMapper.selectExistsByNickname(request.getNickname())) {
			throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
		}
		if (userMapper.selectExistsByPhoneNumber(request.getPhoneNumber().replace("-", ""))) {
			throw new IllegalArgumentException("이미 가입된 휴대폰 번호입니다.");
		}
	}

	private void validateUniqueProfileFields(User user, UpdateProfileRequest request, String normalizedPhoneNumber) {
		if (userMapper.selectExistsByEmailAndIdNot(request.getEmail(), user.getId())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
		}
		if (userMapper.selectExistsByNicknameAndIdNot(request.getNickname(), user.getId())) {
			throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
		}
		if (userMapper.selectExistsByPhoneNumberAndIdNot(normalizedPhoneNumber, user.getId())) {
			throw new IllegalArgumentException("이미 가입된 휴대폰 번호입니다.");
		}
	}
}
