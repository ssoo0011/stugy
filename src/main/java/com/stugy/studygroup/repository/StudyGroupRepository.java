package com.stugy.studygroup.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.stugy.studygroup.domain.StudyGroup;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

	List<StudyGroup> findAllByOrderByIdDesc();

	List<StudyGroup> findAllByOwnerIdOrderByIdDesc(Long ownerId);

	@Query("""
			select studyGroup
			from StudyGroup studyGroup
			where (:category = '전체' or studyGroup.category = :category)
			  and (
			    :query = ''
			    or lower(studyGroup.title) like lower(concat('%', :query, '%'))
			    or lower(studyGroup.description) like lower(concat('%', :query, '%'))
			    or lower(studyGroup.category) like lower(concat('%', :query, '%'))
			    or lower(studyGroup.studyMethod) like lower(concat('%', :query, '%'))
			    or lower(studyGroup.difficulty) like lower(concat('%', :query, '%'))
			  )
			order by studyGroup.id desc
			""")
	Page<StudyGroup> findPage(@Param("category") String category, @Param("query") String query, Pageable pageable);
}
