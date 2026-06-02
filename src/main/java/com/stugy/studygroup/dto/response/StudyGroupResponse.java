package com.stugy.studygroup.dto.response;

import java.util.List;

import com.stugy.studygroup.domain.StudyGroup;
import com.stugy.studygroup.domain.StudyGroupMemberStatus;

public record StudyGroupResponse(
		Long id,
		String title,
		String category,
		String location,
		String place,
		String schedule,
		int memberCount,
		int capacity,
		List<String> tags,
		String description,
		String studyMethod,
		String recruitmentStatus,
		String difficulty,
		String duration,
		String goal,
		String participationRequirements,
		String ownerNickname,
		String applicationStatus,
		boolean ownedByCurrentUser
) {

	public static StudyGroupResponse from(StudyGroup studyGroup) {
		return from(studyGroup, null, false, studyGroup.getCurrentMembers());
	}

	public static StudyGroupResponse from(StudyGroup studyGroup, StudyGroupMemberStatus applicationStatus) {
		return from(studyGroup, applicationStatus, false, studyGroup.getCurrentMembers());
	}

	public static StudyGroupResponse from(StudyGroup studyGroup, StudyGroupMemberStatus applicationStatus,
			boolean ownedByCurrentUser, int memberCount) {
		String location = studyGroup.getStudyMethod().equals("온라인") ? "온라인" : studyGroup.getRegion();
		String schedule = studyGroup.getMeetingDays() + " " + studyGroup.getMeetingTime();
		return new StudyGroupResponse(
				studyGroup.getId(),
				studyGroup.getTitle(),
				studyGroup.getCategory(),
				location,
				studyGroup.getPlace(),
				schedule,
				memberCount,
				studyGroup.getCapacity(),
				List.of(studyGroup.getCategory(), studyGroup.getStudyMethod(), studyGroup.getDifficulty()),
				studyGroup.getDescription(),
				studyGroup.getStudyMethod(),
				studyGroup.getRecruitmentStatus(),
				studyGroup.getDifficulty(),
				studyGroup.getDuration(),
				studyGroup.getGoal(),
				studyGroup.getParticipationRequirements(),
				studyGroup.getOwner().getNickname(),
				applicationStatus == null ? null : applicationStatus.name(),
				ownedByCurrentUser);
	}
}
