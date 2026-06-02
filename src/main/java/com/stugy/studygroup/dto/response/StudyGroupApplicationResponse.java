package com.stugy.studygroup.dto.response;

import com.stugy.studygroup.domain.StudyGroupMember;

public record StudyGroupApplicationResponse(String status, String statusLabel) {

	public static StudyGroupApplicationResponse from(StudyGroupMember member) {
		return new StudyGroupApplicationResponse(member.getStatus().name(), member.getStatus().getLabel());
	}
}
