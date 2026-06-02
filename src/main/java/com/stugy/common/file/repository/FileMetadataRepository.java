package com.stugy.common.file.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.stugy.common.file.domain.FileMetadata;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

	List<FileMetadata> findAllByBoardIdAndCategory(Long boardId, String category);

	Optional<FileMetadata> findFirstByBoardIdAndCategoryAndThumbnailYnOrderByIdDesc(Long boardId, String category,
			String thumbnailYn);
}
