package com.stugy.studygroup.dto.response;

import java.util.List;

public record MyStudyGroupsResponse(
		List<StudyGroupResponse> createdGroups,
		List<StudyGroupResponse> appliedGroups
) {
}
