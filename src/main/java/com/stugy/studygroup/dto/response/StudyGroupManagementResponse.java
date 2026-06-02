package com.stugy.studygroup.dto.response;

import java.util.List;

public record StudyGroupManagementResponse(
		Long id,
		String title,
		int capacity,
		int memberCount,
		boolean ownerView,
		List<StudyGroupMemberResponse> members,
		List<StudyGroupApplicantResponse> applicants,
		List<StudyGroupScheduleResponse> schedules
) {
}
