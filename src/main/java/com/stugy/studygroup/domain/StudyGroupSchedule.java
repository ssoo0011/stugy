package com.stugy.studygroup.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "study_group_schedules")
public class StudyGroupSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "study_group_id", nullable = false)
	private StudyGroup studyGroup;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, length = 1000)
	private String content;

	@Column(name = "scheduled_at", nullable = false)
	private LocalDateTime scheduledAt;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected StudyGroupSchedule() {
	}

	public StudyGroupSchedule(StudyGroup studyGroup, String title, String content, LocalDateTime scheduledAt) {
		this.studyGroup = studyGroup;
		this.title = title;
		this.content = content;
		this.scheduledAt = scheduledAt;
	}

	public void update(String title, String content, LocalDateTime scheduledAt) {
		this.title = title;
		this.content = content;
		this.scheduledAt = scheduledAt;
	}

	public Long getId() { return id; }
	public StudyGroup getStudyGroup() { return studyGroup; }
	public String getTitle() { return title; }
	public String getContent() { return content; }
	public LocalDateTime getScheduledAt() { return scheduledAt; }
}
