package com.stugy.studygroup.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stugy.studygroup.domain.StudyGroupMember;
import com.stugy.studygroup.domain.StudyGroupMemberStatus;

public interface StudyGroupMemberRepository extends JpaRepository<StudyGroupMember, Long> {

	Optional<StudyGroupMember> findByStudyGroupIdAndUserId(Long studyGroupId, Long userId);

	List<StudyGroupMember> findAllByUserId(Long userId);

	List<StudyGroupMember> findAllByUserIdOrderByIdDesc(Long userId);

	long countByStudyGroupIdAndStatus(Long studyGroupId, StudyGroupMemberStatus status);

	List<StudyGroupMember> findAllByStudyGroupIdAndStatusOrderByIdAsc(Long studyGroupId, StudyGroupMemberStatus status);

	long countByStudyGroupOwnerIdAndStatusAndNotificationReadYn(
			Long ownerId, StudyGroupMemberStatus status, String notificationReadYn);

	List<StudyGroupMember> findAllByStudyGroupOwnerIdAndStatusOrderByCreatedAtDesc(
			Long ownerId, StudyGroupMemberStatus status);

	List<StudyGroupMember> findAllByStudyGroupOwnerIdAndStatusAndNotificationReadYn(
			Long ownerId, StudyGroupMemberStatus status, String notificationReadYn);

	long countByUserIdAndStatusInAndNotificationReadYn(
			Long userId, Collection<StudyGroupMemberStatus> statuses, String notificationReadYn);

	List<StudyGroupMember> findAllByUserIdAndStatusInOrderByUpdatedAtDesc(
			Long userId, Collection<StudyGroupMemberStatus> statuses);

	List<StudyGroupMember> findAllByUserIdAndStatusInAndNotificationReadYn(
			Long userId, Collection<StudyGroupMemberStatus> statuses, String notificationReadYn);
}
