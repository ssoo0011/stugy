package com.stugy.studygroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stugy.studygroup.domain.StudyGroupSchedule;

public interface StudyGroupScheduleRepository extends JpaRepository<StudyGroupSchedule, Long> {

	List<StudyGroupSchedule> findAllByStudyGroupIdOrderByScheduledAtAsc(Long studyGroupId);
}
