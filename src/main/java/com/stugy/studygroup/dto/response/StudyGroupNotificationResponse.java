package com.stugy.studygroup.dto.response;

import java.util.List;

public record StudyGroupNotificationResponse(
		long unreadNotificationCount,
		List<StudyGroupNotificationItemResponse> notifications
) {
}
