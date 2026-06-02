package com.stugy.studygroup.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.stugy.studygroup.dto.request.CreateStudyGroupRequest;
import com.stugy.studygroup.dto.request.CreateStudyGroupScheduleRequest;
import com.stugy.studygroup.dto.response.MyStudyGroupsResponse;
import com.stugy.studygroup.dto.response.StudyGroupApplicationResponse;
import com.stugy.studygroup.dto.response.StudyGroupManagementResponse;
import com.stugy.studygroup.dto.response.StudyGroupNotificationResponse;
import com.stugy.studygroup.dto.response.StudyGroupResponse;
import com.stugy.studygroup.dto.response.StudyGroupScheduleResponse;
import com.stugy.studygroup.service.StudyGroupService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/study-groups")
public class StudyGroupController {

	private final StudyGroupService studyGroupService;

	public StudyGroupController(StudyGroupService studyGroupService) {
		this.studyGroupService = studyGroupService;
	}

	@GetMapping
	public java.util.List<StudyGroupResponse> findAll(Authentication authentication) {
		String loginId = authentication == null || authentication instanceof AnonymousAuthenticationToken
				? null
				: authentication.getName();
		return studyGroupService.findAll(loginId);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public StudyGroupResponse create(@Valid @RequestBody CreateStudyGroupRequest request, Authentication authentication) {
		return studyGroupService.create(authentication.getName(), request);
	}

	@GetMapping("/mine")
	public MyStudyGroupsResponse findMine(Authentication authentication) {
		return studyGroupService.findMine(authentication.getName());
	}

	@GetMapping("/notifications")
	public StudyGroupNotificationResponse findNotifications(Authentication authentication) {
		return studyGroupService.findNotifications(authentication.getName());
	}

	@PostMapping("/notifications/read")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void readNotifications(Authentication authentication) {
		studyGroupService.readNotifications(authentication.getName());
	}

	@PostMapping("/{studyGroupId}/applications")
	@ResponseStatus(HttpStatus.CREATED)
	public StudyGroupApplicationResponse apply(@PathVariable Long studyGroupId, Authentication authentication) {
		return studyGroupService.apply(authentication.getName(), studyGroupId);
	}

	@GetMapping("/{studyGroupId}/management")
	public StudyGroupManagementResponse findManagement(@PathVariable Long studyGroupId, Authentication authentication) {
		return studyGroupService.findManagement(authentication.getName(), studyGroupId);
	}

	@PostMapping("/{studyGroupId}/schedules")
	@ResponseStatus(HttpStatus.CREATED)
	public StudyGroupScheduleResponse createSchedule(@PathVariable Long studyGroupId,
			@Valid @RequestBody CreateStudyGroupScheduleRequest request, Authentication authentication) {
		return studyGroupService.createSchedule(authentication.getName(), studyGroupId, request);
	}

	@PutMapping("/{studyGroupId}/schedules/{scheduleId}")
	public StudyGroupScheduleResponse updateSchedule(@PathVariable Long studyGroupId, @PathVariable Long scheduleId,
			@Valid @RequestBody CreateStudyGroupScheduleRequest request, Authentication authentication) {
		return studyGroupService.updateSchedule(authentication.getName(), studyGroupId, scheduleId, request);
	}

	@DeleteMapping("/{studyGroupId}/schedules/{scheduleId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteSchedule(@PathVariable Long studyGroupId, @PathVariable Long scheduleId,
			Authentication authentication) {
		studyGroupService.deleteSchedule(authentication.getName(), studyGroupId, scheduleId);
	}

	@PostMapping("/{studyGroupId}/applications/{applicationId}/accept")
	public StudyGroupApplicationResponse acceptApplication(@PathVariable Long studyGroupId,
			@PathVariable Long applicationId, Authentication authentication) {
		return studyGroupService.acceptApplication(authentication.getName(), studyGroupId, applicationId);
	}

	@PostMapping("/{studyGroupId}/applications/{applicationId}/reject")
	public StudyGroupApplicationResponse rejectApplication(@PathVariable Long studyGroupId,
			@PathVariable Long applicationId, Authentication authentication) {
		return studyGroupService.rejectApplication(authentication.getName(), studyGroupId, applicationId);
	}
}
