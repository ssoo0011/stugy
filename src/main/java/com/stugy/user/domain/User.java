package com.stugy.user.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 30)
	private String loginId;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, length = 100)
	private String passwordHash;

	@Column(nullable = false, unique = true, length = 30)
	private String nickname;

	@Column(nullable = false, length = 30)
	private String region;

	@Column(nullable = false)
	private LocalDate birthDate;

	@Column(nullable = false, unique = true, length = 20)
	private String phoneNumber;

	@Column(length = 500)
	private String introduction;

	@CreationTimestamp
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected User() {
	}

	public User(String loginId, String email, String passwordHash, String nickname, String region, LocalDate birthDate,
			String phoneNumber, String introduction) {
		this.loginId = loginId;
		this.email = email;
		this.passwordHash = passwordHash;
		this.nickname = nickname;
		this.region = region;
		this.birthDate = birthDate;
		this.phoneNumber = phoneNumber;
		this.introduction = introduction;
	}

	public Long getId() {
		return id;
	}

	public String getNickname() {
		return nickname;
	}

	public String getLoginId() {
		return loginId;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getEmail() { return email; }
	public String getRegion() { return region; }
	public LocalDate getBirthDate() { return birthDate; }
	public String getPhoneNumber() { return phoneNumber; }
	public String getIntroduction() { return introduction; }

	public void updateProfile(String email, String nickname, String region, LocalDate birthDate,
			String phoneNumber, String introduction) {
		this.email = email;
		this.nickname = nickname;
		this.region = region;
		this.birthDate = birthDate;
		this.phoneNumber = phoneNumber;
		this.introduction = introduction;
	}
}
