package com.stugy.studygroup.dto.response;

import java.time.LocalDateTime;

import com.stugy.studygroup.domain.StudyGroupMember;

public record StudyGroupNotificationItemResponse(
		Long applicationId,
		Long studyGroupId,
		String studyGroupTitle,
		String actorNickname,
		String notificationType,
		String applicationStatus,
		LocalDateTime notificationAt
) {

	public static StudyGroupNotificationItemResponse request(StudyGroupMember member) {
		return new StudyGroupNotificationItemResponse(
				member.getId(),
				member.getStudyGroup().getId(),
				member.getStudyGroup().getTitle(),
				member.getUser().getNickname(),
				"APPLICATION_REQUESTED",
				member.getStatus().name(),
				member.getCreatedAt());
	}

	public static StudyGroupNotificationItemResponse result(StudyGroupMember member) {
		return new StudyGroupNotificationItemResponse(
				member.getId(),
				member.getStudyGroup().getId(),
				member.getStudyGroup().getTitle(),
				member.getStudyGroup().getOwner().getNickname(),
				"APPLICATION_RESULT",
				member.getStatus().name(),
				member.getUpdatedAt());
	}
}
