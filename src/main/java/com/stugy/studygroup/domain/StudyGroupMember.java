package com.stugy.studygroup.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.stugy.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "study_group_members", uniqueConstraints = {
		@UniqueConstraint(name = "uk_study_group_member", columnNames = { "study_group_id", "user_id" })
})
public class StudyGroupMember {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "study_group_id", nullable = false)
	private StudyGroup studyGroup;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StudyGroupMemberStatus status;

	@Column(name = "notification_read_yn", nullable = false, length = 1)
	private String notificationReadYn;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	protected StudyGroupMember() {
	}

	public StudyGroupMember(StudyGroup studyGroup, User user) {
		this.studyGroup = studyGroup;
		this.user = user;
		this.status = StudyGroupMemberStatus.REQUESTED;
		this.notificationReadYn = "N";
	}

	public void requestAgain() {
		this.status = StudyGroupMemberStatus.REQUESTED;
		this.notificationReadYn = "N";
	}

	public void markNotificationAsRead() {
		this.notificationReadYn = "Y";
	}

	public void accept() {
		this.status = StudyGroupMemberStatus.ACCEPTED;
		this.notificationReadYn = "Y";
	}

	public void reject() {
		this.status = StudyGroupMemberStatus.REJECTED;
		this.notificationReadYn = "Y";
	}

	public StudyGroupMemberStatus getStatus() {
		return status;
	}

	public StudyGroup getStudyGroup() {
		return studyGroup;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
