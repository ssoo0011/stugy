package com.stugy.studygroup.dto.response;

import java.util.List;

public record StudyGroupPageResponse(
		List<StudyGroupResponse> groups,
		int page,
		boolean hasNext,
		long totalElements
) {
}
