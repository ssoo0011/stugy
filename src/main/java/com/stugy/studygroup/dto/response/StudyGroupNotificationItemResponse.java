package com.stugy.studygroup.dto.response;

import java.time.LocalDateTime;

import com.stugy.studygroup.domain.StudyGroupMember;

public record StudyGroupNotificationItemResponse(
		Long applicationId,
		Long studyGroupId,
		String studyGroupTitle,
		String applicantNickname,
		LocalDateTime requestedAt
) {

	public static StudyGroupNotificationItemResponse from(StudyGroupMember member) {
		return new StudyGroupNotificationItemResponse(
				member.getId(),
				member.getStudyGroup().getId(),
				member.getStudyGroup().getTitle(),
				member.getUser().getNickname(),
				member.getCreatedAt());
	}
}
