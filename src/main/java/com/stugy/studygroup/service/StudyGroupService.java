package com.stugy.studygroup.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stugy.studygroup.domain.StudyGroup;
import com.stugy.studygroup.domain.StudyGroupMember;
import com.stugy.studygroup.domain.StudyGroupMemberStatus;
import com.stugy.studygroup.domain.StudyGroupSchedule;
import com.stugy.studygroup.dto.request.CreateStudyGroupRequest;
import com.stugy.studygroup.dto.request.CreateStudyGroupScheduleRequest;
import com.stugy.studygroup.dto.response.MyStudyGroupsResponse;
import com.stugy.studygroup.dto.response.StudyGroupApplicantResponse;
import com.stugy.studygroup.dto.response.StudyGroupApplicationResponse;
import com.stugy.studygroup.dto.response.StudyGroupManagementResponse;
import com.stugy.studygroup.dto.response.StudyGroupMemberResponse;
import com.stugy.studygroup.dto.response.StudyGroupNotificationItemResponse;
import com.stugy.studygroup.dto.response.StudyGroupNotificationResponse;
import com.stugy.studygroup.dto.response.StudyGroupPageResponse;
import com.stugy.studygroup.dto.response.StudyGroupResponse;
import com.stugy.studygroup.dto.response.StudyGroupScheduleResponse;
import com.stugy.studygroup.mapper.StudyGroupMapper;
import com.stugy.studygroup.mapper.StudyGroupMemberMapper;
import com.stugy.studygroup.mapper.StudyGroupScheduleMapper;
import com.stugy.user.domain.User;
import com.stugy.user.mapper.UserMapper;

@Service
public class StudyGroupService {

	private final StudyGroupMapper studyGroupMapper;
	private final UserMapper userMapper;
	private final StudyGroupMemberMapper studyGroupMemberMapper;
	private final StudyGroupScheduleMapper studyGroupScheduleMapper;

	public StudyGroupService(StudyGroupMapper studyGroupMapper, UserMapper userMapper,
			StudyGroupMemberMapper studyGroupMemberMapper,
			StudyGroupScheduleMapper studyGroupScheduleMapper) {
		this.studyGroupMapper = studyGroupMapper;
		this.userMapper = userMapper;
		this.studyGroupMemberMapper = studyGroupMemberMapper;
		this.studyGroupScheduleMapper = studyGroupScheduleMapper;
	}

	@Transactional(readOnly = true)
	public StudyGroupPageResponse findAll(String loginId, int page, int size, String category, String query) {
		User currentUser = loginId == null ? null : userMapper.selectByLoginId(loginId).orElse(null);
		Map<Long, StudyGroupMemberStatus> applicationStatuses = currentUser == null
				? Map.of()
				: studyGroupMemberMapper.selectAllByUserId(currentUser.getId()).stream()
						.collect(Collectors.toMap(
								member -> member.getStudyGroup().getId(),
								StudyGroupMember::getStatus));
		Page<StudyGroup> studyGroupPage = studyGroupMapper.selectPage(
				category == null || category.isBlank() ? "전체" : category,
				query == null ? "" : query.trim(),
				PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 30)));
		List<StudyGroupResponse> groups = studyGroupPage.getContent().stream()
				.map(studyGroup -> StudyGroupResponse.from(
						studyGroup,
						applicationStatuses.get(studyGroup.getId()),
						currentUser != null && studyGroup.getOwner().getId().equals(currentUser.getId()),
						countMembers(studyGroup)))
				.toList();
		return new StudyGroupPageResponse(
				groups,
				studyGroupPage.getNumber(),
				studyGroupPage.hasNext(),
				studyGroupPage.getTotalElements());
	}

	@Transactional
	public StudyGroupResponse create(String loginId, CreateStudyGroupRequest request) {
		User owner = userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		StudyGroup studyGroup = new StudyGroup(
				owner,
				request.title(),
				request.category(),
				request.studyMethod(),
				request.region(),
				request.place(),
				request.capacity(),
				request.difficulty(),
				request.meetingDays(),
				request.meetingTime(),
				request.duration(),
				request.goal(),
				request.participationRequirements(),
				request.description(),
				request.contactLink());
		studyGroupMapper.insertStudyGroup(studyGroup);
		return StudyGroupResponse.from(studyGroup, null, true, countMembers(studyGroup));
	}

	@Transactional(readOnly = true)
	public StudyGroupResponse findOne(String loginId, Long studyGroupId) {
		User currentUser = loginId == null ? null : userMapper.selectByLoginId(loginId).orElse(null);
		StudyGroup studyGroup = findStudyGroup(studyGroupId);
		StudyGroupMemberStatus applicationStatus = currentUser == null
				? null
				: studyGroupMemberMapper.selectByStudyGroupIdAndUserId(studyGroupId, currentUser.getId())
						.map(StudyGroupMember::getStatus)
						.orElse(null);
		return StudyGroupResponse.from(
				studyGroup,
				applicationStatus,
				currentUser != null && studyGroup.getOwner().getId().equals(currentUser.getId()),
				countMembers(studyGroup));
	}

	@Transactional(readOnly = true)
	public MyStudyGroupsResponse findMine(String loginId) {
		User user = userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		List<StudyGroupResponse> createdGroups = studyGroupMapper.selectAllByOwnerIdOrderByIdDesc(user.getId()).stream()
				.map(studyGroup -> StudyGroupResponse.from(studyGroup, null, true, countMembers(studyGroup)))
				.toList();
		List<StudyGroupResponse> appliedGroups = studyGroupMemberMapper.selectAllByUserIdOrderByIdDesc(user.getId()).stream()
				.map(member -> StudyGroupResponse.from(
						member.getStudyGroup(),
						member.getStatus(),
						false,
						countMembers(member.getStudyGroup())))
				.toList();
		return new MyStudyGroupsResponse(createdGroups, appliedGroups);
	}

	@Transactional(readOnly = true)
	public StudyGroupNotificationResponse findNotifications(String loginId) {
		User user = userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		List<StudyGroupMemberStatus> resultStatuses = List.of(
				StudyGroupMemberStatus.ACCEPTED,
				StudyGroupMemberStatus.REJECTED);
		long unreadNotificationCount = studyGroupMemberMapper.selectCountByStudyGroupOwnerIdAndStatusAndNotificationReadYn(
				user.getId(), StudyGroupMemberStatus.REQUESTED, "N");
		unreadNotificationCount += studyGroupMemberMapper.selectCountByUserIdAndStatusInAndNotificationReadYn(
				user.getId(), resultStatuses, "N");
		List<StudyGroupNotificationItemResponse> requestedNotifications = studyGroupMemberMapper
				.selectAllByStudyGroupOwnerIdAndStatusOrderByCreatedAtDesc(user.getId(), StudyGroupMemberStatus.REQUESTED)
				.stream()
				.map(StudyGroupNotificationItemResponse::request)
				.toList();
		List<StudyGroupNotificationItemResponse> resultNotifications = studyGroupMemberMapper
				.selectAllByUserIdAndStatusInOrderByUpdatedAtDesc(user.getId(), resultStatuses)
				.stream()
				.map(StudyGroupNotificationItemResponse::result)
				.toList();
		List<StudyGroupNotificationItemResponse> notifications = java.util.stream.Stream
				.concat(requestedNotifications.stream(), resultNotifications.stream())
				.sorted(Comparator.comparing(StudyGroupNotificationItemResponse::notificationAt).reversed())
				.toList();
		return new StudyGroupNotificationResponse(unreadNotificationCount, notifications);
	}

	@Transactional
	public void readNotifications(String loginId) {
		User user = userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		studyGroupMemberMapper.selectAllByStudyGroupOwnerIdAndStatusAndNotificationReadYn(
				user.getId(), StudyGroupMemberStatus.REQUESTED, "N")
				.forEach(member -> {
					member.markNotificationAsRead();
					studyGroupMemberMapper.updateStudyGroupMember(member);
				});
		studyGroupMemberMapper.selectAllByUserIdAndStatusInAndNotificationReadYn(
				user.getId(),
				List.of(StudyGroupMemberStatus.ACCEPTED, StudyGroupMemberStatus.REJECTED),
				"N")
				.forEach(member -> {
					member.markNotificationAsRead();
					studyGroupMemberMapper.updateStudyGroupMember(member);
				});
	}

	@Transactional
	public StudyGroupApplicationResponse apply(String loginId, Long studyGroupId) {
		User user = userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		StudyGroup studyGroup = studyGroupMapper.selectById(studyGroupId)
				.orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));

		if (studyGroup.getOwner().getId().equals(user.getId())) {
			throw new IllegalArgumentException("본인이 만든 스터디에는 참가 신청할 수 없습니다.");
		}
		if (countMembers(studyGroup) >= studyGroup.getCapacity()) {
			throw new IllegalArgumentException("정원이 모두 찬 스터디입니다.");
		}

		StudyGroupMember member = studyGroupMemberMapper.selectByStudyGroupIdAndUserId(studyGroupId, user.getId())
				.map(existingMember -> {
					if (existingMember.getStatus() == StudyGroupMemberStatus.REQUESTED) {
						throw new IllegalArgumentException("이미 참가 신청한 스터디입니다.");
					}
					if (existingMember.getStatus() == StudyGroupMemberStatus.ACCEPTED) {
						throw new IllegalArgumentException("이미 참여 중인 스터디입니다.");
					}
					existingMember.requestAgain();
					return existingMember;
				})
				.orElseGet(() -> new StudyGroupMember(studyGroup, user));

		if (member.getId() == null) {
			studyGroupMemberMapper.insertStudyGroupMember(member);
		} else {
			studyGroupMemberMapper.updateStudyGroupMember(member);
		}
		return StudyGroupApplicationResponse.from(member);
	}

	@Transactional(readOnly = true)
	public StudyGroupManagementResponse findManagement(String loginId, Long studyGroupId) {
		User user = findUser(loginId);
		StudyGroup studyGroup = findStudyGroup(studyGroupId);
		boolean ownerView = studyGroup.getOwner().getId().equals(user.getId());
		boolean acceptedMember = studyGroupMemberMapper.selectByStudyGroupIdAndUserId(studyGroupId, user.getId())
				.map(member -> member.getStatus() == StudyGroupMemberStatus.ACCEPTED)
				.orElse(false);
		if (!ownerView && !acceptedMember) {
			throw new IllegalArgumentException("그룹원만 그룹 페이지를 확인할 수 있습니다.");
		}
		List<StudyGroupMemberResponse> members = new java.util.ArrayList<>();
		members.add(StudyGroupMemberResponse.owner(studyGroup.getOwner()));
		studyGroupMemberMapper.selectAllByStudyGroupIdAndStatusOrderByIdAsc(
				studyGroupId, StudyGroupMemberStatus.ACCEPTED)
				.stream()
				.map(member -> StudyGroupMemberResponse.member(member.getUser()))
				.forEach(members::add);
		List<StudyGroupScheduleResponse> schedules = studyGroupScheduleMapper
				.selectAllByStudyGroupIdOrderByScheduledAtAsc(studyGroupId)
				.stream()
				.map(StudyGroupScheduleResponse::from)
				.toList();
		List<StudyGroupApplicantResponse> applicants = ownerView
				? studyGroupMemberMapper.selectAllByStudyGroupIdAndStatusOrderByIdAsc(
						studyGroupId, StudyGroupMemberStatus.REQUESTED)
						.stream()
						.map(StudyGroupApplicantResponse::from)
						.toList()
				: List.of();
		return new StudyGroupManagementResponse(
				studyGroup.getId(),
				studyGroup.getTitle(),
				studyGroup.getCapacity(),
				members.size(),
				ownerView,
				members,
				applicants,
				schedules);
	}

	@Transactional
	public StudyGroupScheduleResponse createSchedule(String loginId, Long studyGroupId,
			CreateStudyGroupScheduleRequest request) {
		StudyGroup studyGroup = findOwnedStudyGroup(loginId, studyGroupId);
		StudyGroupSchedule schedule = new StudyGroupSchedule(studyGroup, request.title(), request.content(),
				normalizeScheduleTime(request.scheduledAt()));
		studyGroupScheduleMapper.insertStudyGroupSchedule(schedule);
		return StudyGroupScheduleResponse.from(schedule);
	}

	@Transactional
	public StudyGroupScheduleResponse updateSchedule(String loginId, Long studyGroupId, Long scheduleId,
			CreateStudyGroupScheduleRequest request) {
		findOwnedStudyGroup(loginId, studyGroupId);
		StudyGroupSchedule schedule = findSchedule(studyGroupId, scheduleId);
		schedule.update(request.title(), request.content(), normalizeScheduleTime(request.scheduledAt()));
		studyGroupScheduleMapper.updateStudyGroupSchedule(schedule);
		return StudyGroupScheduleResponse.from(schedule);
	}

	@Transactional
	public void deleteSchedule(String loginId, Long studyGroupId, Long scheduleId) {
		findOwnedStudyGroup(loginId, studyGroupId);
		studyGroupScheduleMapper.deleteById(findSchedule(studyGroupId, scheduleId).getId());
	}

	@Transactional
	public StudyGroupApplicationResponse acceptApplication(String loginId, Long studyGroupId, Long applicationId) {
		StudyGroup studyGroup = findOwnedStudyGroup(loginId, studyGroupId);
		if (countMembers(studyGroup) >= studyGroup.getCapacity()) {
			throw new IllegalArgumentException("정원이 모두 찬 스터디입니다.");
		}
		StudyGroupMember member = findRequestedApplication(studyGroupId, applicationId);
		member.accept();
		studyGroupMemberMapper.updateStudyGroupMember(member);
		return StudyGroupApplicationResponse.from(member);
	}

	@Transactional
	public StudyGroupApplicationResponse rejectApplication(String loginId, Long studyGroupId, Long applicationId) {
		findOwnedStudyGroup(loginId, studyGroupId);
		StudyGroupMember member = findRequestedApplication(studyGroupId, applicationId);
		member.reject();
		studyGroupMemberMapper.updateStudyGroupMember(member);
		return StudyGroupApplicationResponse.from(member);
	}

	private int countMembers(StudyGroup studyGroup) {
		return 1 + Math.toIntExact(studyGroupMemberMapper.selectCountByStudyGroupIdAndStatus(
				studyGroup.getId(), StudyGroupMemberStatus.ACCEPTED));
	}

	private StudyGroup findOwnedStudyGroup(String loginId, Long studyGroupId) {
		User user = findUser(loginId);
		StudyGroup studyGroup = findStudyGroup(studyGroupId);
		if (!studyGroup.getOwner().getId().equals(user.getId())) {
			throw new IllegalArgumentException("그룹장만 그룹을 관리할 수 있습니다.");
		}
		return studyGroup;
	}

	private StudyGroupMember findRequestedApplication(Long studyGroupId, Long applicationId) {
		StudyGroupMember member = studyGroupMemberMapper.selectById(applicationId)
				.orElseThrow(() -> new IllegalArgumentException("참가 신청을 찾을 수 없습니다."));
		if (!member.getStudyGroup().getId().equals(studyGroupId)
				|| member.getStatus() != StudyGroupMemberStatus.REQUESTED) {
			throw new IllegalArgumentException("처리할 수 없는 참가 신청입니다.");
		}
		return member;
	}

	private StudyGroupSchedule findSchedule(Long studyGroupId, Long scheduleId) {
		StudyGroupSchedule schedule = studyGroupScheduleMapper.selectById(scheduleId)
				.orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));
		if (!schedule.getStudyGroup().getId().equals(studyGroupId)) {
			throw new IllegalArgumentException("처리할 수 없는 일정입니다.");
		}
		return schedule;
	}

	private LocalDateTime normalizeScheduleTime(LocalDateTime scheduledAt) {
		if (scheduledAt.getMinute() % 10 != 0) {
			throw new IllegalArgumentException("일정 시간은 10분 단위로 선택해 주세요.");
		}
		return scheduledAt.withSecond(0).withNano(0);
	}

	private User findUser(String loginId) {
		return userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	private StudyGroup findStudyGroup(Long studyGroupId) {
		return studyGroupMapper.selectById(studyGroupId)
				.orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
	}
}
