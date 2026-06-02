package com.stugy.studygroup.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.stugy.user.domain.User;

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
@Table(name = "study_groups")
public class StudyGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private User owner;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, length = 30)
	private String category;

	@Column(name = "study_method", nullable = false, length = 20)
	private String studyMethod;

	@Column(nullable = false, length = 30)
	private String region;

	@Column(length = 100)
	private String place;

	@Column(nullable = false)
	private Integer capacity;

	@Column(name = "current_members", nullable = false)
	private Integer currentMembers;

	@Column(name = "recruitment_status", nullable = false, length = 20)
	private String recruitmentStatus;

	@Column(nullable = false, length = 20)
	private String difficulty;

	@Column(name = "meeting_days", nullable = false, length = 50)
	private String meetingDays;

	@Column(name = "meeting_time", nullable = false, length = 30)
	private String meetingTime;

	@Column(nullable = false, length = 50)
	private String duration;

	@Column(nullable = false, length = 300)
	private String goal;

	@Column(name = "participation_requirements", length = 500)
	private String participationRequirements;

	@Column(nullable = false, columnDefinition = "text")
	private String description;

	@Column(name = "contact_link", length = 500)
	private String contactLink;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	protected StudyGroup() {
	}

	public StudyGroup(User owner, String title, String category, String studyMethod, String region, String place,
			Integer capacity, String difficulty, String meetingDays, String meetingTime, String duration, String goal,
			String participationRequirements, String description, String contactLink) {
		this.owner = owner;
		this.title = title;
		this.category = category;
		this.studyMethod = studyMethod;
		this.region = region;
		this.place = place;
		this.capacity = capacity;
		this.currentMembers = 1;
		this.recruitmentStatus = "모집중";
		this.difficulty = difficulty;
		this.meetingDays = meetingDays;
		this.meetingTime = meetingTime;
		this.duration = duration;
		this.goal = goal;
		this.participationRequirements = participationRequirements;
		this.description = description;
		this.contactLink = contactLink;
	}

	public Long getId() { return id; }
	public User getOwner() { return owner; }
	public String getTitle() { return title; }
	public String getCategory() { return category; }
	public String getStudyMethod() { return studyMethod; }
	public String getRegion() { return region; }
	public String getPlace() { return place; }
	public Integer getCapacity() { return capacity; }
	public Integer getCurrentMembers() { return currentMembers; }
	public String getRecruitmentStatus() { return recruitmentStatus; }
	public String getDifficulty() { return difficulty; }
	public String getMeetingDays() { return meetingDays; }
	public String getMeetingTime() { return meetingTime; }
	public String getDuration() { return duration; }
	public String getGoal() { return goal; }
	public String getParticipationRequirements() { return participationRequirements; }
	public String getDescription() { return description; }
	public String getContactLink() { return contactLink; }
}
