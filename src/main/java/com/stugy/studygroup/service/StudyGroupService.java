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
import com.stugy.studygroup.repository.StudyGroupMemberRepository;
import com.stugy.studygroup.repository.StudyGroupRepository;
import com.stugy.studygroup.repository.StudyGroupScheduleRepository;
import com.stugy.user.domain.User;
import com.stugy.user.repository.UserRepository;

@Service
public class StudyGroupService {

	private final StudyGroupRepository studyGroupRepository;
	private final UserRepository userRepository;
	private final StudyGroupMemberRepository studyGroupMemberRepository;
	private final StudyGroupScheduleRepository studyGroupScheduleRepository;

	public StudyGroupService(StudyGroupRepository studyGroupRepository, UserRepository userRepository,
			StudyGroupMemberRepository studyGroupMemberRepository,
			StudyGroupScheduleRepository studyGroupScheduleRepository) {
		this.studyGroupRepository = studyGroupRepository;
		this.userRepository = userRepository;
		this.studyGroupMemberRepository = studyGroupMemberRepository;
		this.studyGroupScheduleRepository = studyGroupScheduleRepository;
	}

	@Transactional(readOnly = true)
	public StudyGroupPageResponse findAll(String loginId, int page, int size, String category, String query) {
		User currentUser = loginId == null ? null : userRepository.findByLoginId(loginId).orElse(null);
		Map<Long, StudyGroupMemberStatus> applicationStatuses = currentUser == null
				? Map.of()
				: studyGroupMemberRepository.findAllByUserId(currentUser.getId()).stream()
						.collect(Collectors.toMap(
								member -> member.getStudyGroup().getId(),
								StudyGroupMember::getStatus));
		Page<StudyGroup> studyGroupPage = studyGroupRepository.findPage(
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
		User owner = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		StudyGroup studyGroup = studyGroupRepository.save(new StudyGroup(
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
				request.contactLink()));
		return StudyGroupResponse.from(studyGroup, null, true, countMembers(studyGroup));
	}

	@Transactional(readOnly = true)
	public StudyGroupResponse findOne(String loginId, Long studyGroupId) {
		User currentUser = loginId == null ? null : userRepository.findByLoginId(loginId).orElse(null);
		StudyGroup studyGroup = findStudyGroup(studyGroupId);
		StudyGroupMemberStatus applicationStatus = currentUser == null
				? null
				: studyGroupMemberRepository.findByStudyGroupIdAndUserId(studyGroupId, currentUser.getId())
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
		User user = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		List<StudyGroupResponse> createdGroups = studyGroupRepository.findAllByOwnerIdOrderByIdDesc(user.getId()).stream()
				.map(studyGroup -> StudyGroupResponse.from(studyGroup, null, true, countMembers(studyGroup)))
				.toList();
		List<StudyGroupResponse> appliedGroups = studyGroupMemberRepository.findAllByUserIdOrderByIdDesc(user.getId()).stream()
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
		User user = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		List<StudyGroupMemberStatus> resultStatuses = List.of(
				StudyGroupMemberStatus.ACCEPTED,
				StudyGroupMemberStatus.REJECTED);
		long unreadNotificationCount = studyGroupMemberRepository.countByStudyGroupOwnerIdAndStatusAndNotificationReadYn(
				user.getId(), StudyGroupMemberStatus.REQUESTED, "N");
		unreadNotificationCount += studyGroupMemberRepository.countByUserIdAndStatusInAndNotificationReadYn(
				user.getId(), resultStatuses, "N");
		List<StudyGroupNotificationItemResponse> requestedNotifications = studyGroupMemberRepository
				.findAllByStudyGroupOwnerIdAndStatusOrderByCreatedAtDesc(user.getId(), StudyGroupMemberStatus.REQUESTED)
				.stream()
				.map(StudyGroupNotificationItemResponse::request)
				.toList();
		List<StudyGroupNotificationItemResponse> resultNotifications = studyGroupMemberRepository
				.findAllByUserIdAndStatusInOrderByUpdatedAtDesc(user.getId(), resultStatuses)
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
		User user = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		studyGroupMemberRepository.findAllByStudyGroupOwnerIdAndStatusAndNotificationReadYn(
				user.getId(), StudyGroupMemberStatus.REQUESTED, "N")
				.forEach(StudyGroupMember::markNotificationAsRead);
		studyGroupMemberRepository.findAllByUserIdAndStatusInAndNotificationReadYn(
				user.getId(),
				List.of(StudyGroupMemberStatus.ACCEPTED, StudyGroupMemberStatus.REJECTED),
				"N")
				.forEach(StudyGroupMember::markNotificationAsRead);
	}

	@Transactional
	public StudyGroupApplicationResponse apply(String loginId, Long studyGroupId) {
		User user = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
		StudyGroup studyGroup = studyGroupRepository.findById(studyGroupId)
				.orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));

		if (studyGroup.getOwner().getId().equals(user.getId())) {
			throw new IllegalArgumentException("본인이 만든 스터디에는 참가 신청할 수 없습니다.");
		}
		if (countMembers(studyGroup) >= studyGroup.getCapacity()) {
			throw new IllegalArgumentException("정원이 모두 찬 스터디입니다.");
		}

		StudyGroupMember member = studyGroupMemberRepository.findByStudyGroupIdAndUserId(studyGroupId, user.getId())
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

		return StudyGroupApplicationResponse.from(studyGroupMemberRepository.save(member));
	}

	@Transactional(readOnly = true)
	public StudyGroupManagementResponse findManagement(String loginId, Long studyGroupId) {
		User user = findUser(loginId);
		StudyGroup studyGroup = findStudyGroup(studyGroupId);
		boolean ownerView = studyGroup.getOwner().getId().equals(user.getId());
		boolean acceptedMember = studyGroupMemberRepository.findByStudyGroupIdAndUserId(studyGroupId, user.getId())
				.map(member -> member.getStatus() == StudyGroupMemberStatus.ACCEPTED)
				.orElse(false);
		if (!ownerView && !acceptedMember) {
			throw new IllegalArgumentException("그룹원만 그룹 페이지를 확인할 수 있습니다.");
		}
		List<StudyGroupMemberResponse> members = new java.util.ArrayList<>();
		members.add(StudyGroupMemberResponse.owner(studyGroup.getOwner()));
		studyGroupMemberRepository.findAllByStudyGroupIdAndStatusOrderByIdAsc(
				studyGroupId, StudyGroupMemberStatus.ACCEPTED)
				.stream()
				.map(member -> StudyGroupMemberResponse.member(member.getUser()))
				.forEach(members::add);
		List<StudyGroupScheduleResponse> schedules = studyGroupScheduleRepository
				.findAllByStudyGroupIdOrderByScheduledAtAsc(studyGroupId)
				.stream()
				.map(StudyGroupScheduleResponse::from)
				.toList();
		List<StudyGroupApplicantResponse> applicants = ownerView
				? studyGroupMemberRepository.findAllByStudyGroupIdAndStatusOrderByIdAsc(
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
		return StudyGroupScheduleResponse.from(studyGroupScheduleRepository.save(
				new StudyGroupSchedule(studyGroup, request.title(), request.content(), normalizeScheduleTime(request.scheduledAt()))));
	}

	@Transactional
	public StudyGroupScheduleResponse updateSchedule(String loginId, Long studyGroupId, Long scheduleId,
			CreateStudyGroupScheduleRequest request) {
		findOwnedStudyGroup(loginId, studyGroupId);
		StudyGroupSchedule schedule = findSchedule(studyGroupId, scheduleId);
		schedule.update(request.title(), request.content(), normalizeScheduleTime(request.scheduledAt()));
		return StudyGroupScheduleResponse.from(schedule);
	}

	@Transactional
	public void deleteSchedule(String loginId, Long studyGroupId, Long scheduleId) {
		findOwnedStudyGroup(loginId, studyGroupId);
		studyGroupScheduleRepository.delete(findSchedule(studyGroupId, scheduleId));
	}

	@Transactional
	public StudyGroupApplicationResponse acceptApplication(String loginId, Long studyGroupId, Long applicationId) {
		StudyGroup studyGroup = findOwnedStudyGroup(loginId, studyGroupId);
		if (countMembers(studyGroup) >= studyGroup.getCapacity()) {
			throw new IllegalArgumentException("정원이 모두 찬 스터디입니다.");
		}
		StudyGroupMember member = findRequestedApplication(studyGroupId, applicationId);
		member.accept();
		return StudyGroupApplicationResponse.from(member);
	}

	@Transactional
	public StudyGroupApplicationResponse rejectApplication(String loginId, Long studyGroupId, Long applicationId) {
		findOwnedStudyGroup(loginId, studyGroupId);
		StudyGroupMember member = findRequestedApplication(studyGroupId, applicationId);
		member.reject();
		return StudyGroupApplicationResponse.from(member);
	}

	private int countMembers(StudyGroup studyGroup) {
		return 1 + Math.toIntExact(studyGroupMemberRepository.countByStudyGroupIdAndStatus(
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
		StudyGroupMember member = studyGroupMemberRepository.findById(applicationId)
				.orElseThrow(() -> new IllegalArgumentException("참가 신청을 찾을 수 없습니다."));
		if (!member.getStudyGroup().getId().equals(studyGroupId)
				|| member.getStatus() != StudyGroupMemberStatus.REQUESTED) {
			throw new IllegalArgumentException("처리할 수 없는 참가 신청입니다.");
		}
		return member;
	}

	private StudyGroupSchedule findSchedule(Long studyGroupId, Long scheduleId) {
		StudyGroupSchedule schedule = studyGroupScheduleRepository.findById(scheduleId)
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
		return userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
	}

	private StudyGroup findStudyGroup(Long studyGroupId) {
		return studyGroupRepository.findById(studyGroupId)
				.orElseThrow(() -> new IllegalArgumentException("스터디를 찾을 수 없습니다."));
	}
}
