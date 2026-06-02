package com.stugy.studygroup.dto.response;

import java.time.LocalDateTime;

import com.stugy.studygroup.domain.StudyGroupMember;

public record StudyGroupApplicantResponse(
		Long applicationId,
		Long userId,
		String nickname,
		LocalDateTime requestedAt
) {

	public static StudyGroupApplicantResponse from(StudyGroupMember member) {
		return new StudyGroupApplicantResponse(
				member.getId(),
				member.getUser().getId(),
				member.getUser().getNickname(),
				member.getCreatedAt());
	}
}
