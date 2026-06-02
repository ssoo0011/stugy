package com.stugy.studygroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stugy.studygroup.domain.StudyGroup;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

	List<StudyGroup> findAllByOrderByIdDesc();

	List<StudyGroup> findAllByOwnerIdOrderByIdDesc(Long ownerId);
}
