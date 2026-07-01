package com.stugy.user.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

	@NotBlank
	@Email
	private String email;

	@NotBlank
	@Size(min = 2, max = 30)
	private String nickname;

	@NotBlank
	private String region;

	@NotNull
	@Past
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate birthDate;

	@NotBlank
	@Pattern(regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$", message = "올바른 휴대폰 번호를 입력해 주세요.")
	private String phoneNumber;

	private MultipartFile profileImage;

	@Size(max = 500)
	private String introduction;

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getNickname() { return nickname; }
	public void setNickname(String nickname) { this.nickname = nickname; }
	public String getRegion() { return region; }
	public void setRegion(String region) { this.region = region; }
	public LocalDate getBirthDate() { return birthDate; }
	public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
	public String getPhoneNumber() { return phoneNumber; }
	public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
	public MultipartFile getProfileImage() { return profileImage; }
	public void setProfileImage(MultipartFile profileImage) { this.profileImage = profileImage; }
	public String getIntroduction() { return introduction; }
	public void setIntroduction(String introduction) { this.introduction = introduction; }
}
