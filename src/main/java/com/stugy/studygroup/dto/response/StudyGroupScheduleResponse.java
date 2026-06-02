package com.stugy.studygroup.dto.response;

import java.time.LocalDateTime;

import com.stugy.studygroup.domain.StudyGroupSchedule;

public record StudyGroupScheduleResponse(
		Long id,
		String title,
		String content,
		LocalDateTime scheduledAt
) {

	public static StudyGroupScheduleResponse from(StudyGroupSchedule schedule) {
		return new StudyGroupScheduleResponse(
				schedule.getId(),
				schedule.getTitle(),
				schedule.getContent(),
				schedule.getScheduledAt());
	}
}
