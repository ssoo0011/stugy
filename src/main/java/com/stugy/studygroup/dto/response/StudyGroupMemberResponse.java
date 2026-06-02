package com.stugy.studygroup.dto.response;

import com.stugy.user.domain.User;

public record StudyGroupMemberResponse(
		Long userId,
		String nickname,
		boolean owner
) {

	public static StudyGroupMemberResponse owner(User user) {
		return new StudyGroupMemberResponse(user.getId(), user.getNickname(), true);
	}

	public static StudyGroupMemberResponse member(User user) {
		return new StudyGroupMemberResponse(user.getId(), user.getNickname(), false);
	}
}
