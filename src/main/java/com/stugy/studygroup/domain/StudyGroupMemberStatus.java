package com.stugy.studygroup.domain;

public enum StudyGroupMemberStatus {
	REQUESTED("요청"),
	ACCEPTED("수락"),
	REJECTED("거절"),
	WITHDRAWN("탈퇴");

	private final String label;

	StudyGroupMemberStatus(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}
