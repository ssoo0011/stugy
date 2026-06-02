package com.stugy.common.file.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "files")
public class FileMetadata {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "file_id")
	private Long id;

	@Column(name = "board_id", nullable = false)
	private Long boardId;

	@Column(length = 30)
	private String category;

	@Column(name = "file_type", length = 50)
	private String fileType;

	@Column(name = "file_path", columnDefinition = "text")
	private String filePath;

	@Column(name = "file_original_name", length = 1000)
	private String originalName;

	@Column(name = "file_name", length = 1000)
	private String fileName;

	@Column(name = "file_extension", length = 5)
	private String extension;

	@Column(name = "file_size")
	private Integer fileSize;

	@Column(name = "thumbnail_yn", length = 1)
	private String thumbnailYn;

	@Column(name = "file_order")
	private Integer fileOrder;

	@Column(name = "create_id", length = 15)
	private String createId;

	@CreationTimestamp
	@Column(name = "create_date", nullable = false)
	private LocalDateTime createDate;

	@Column(name = "create_ip", length = 18)
	private String createIp;

	protected FileMetadata() {
	}

	public FileMetadata(Long boardId, String category, String fileType, String filePath, String originalName,
			String fileName, String extension, Integer fileSize, String thumbnailYn, Integer fileOrder,
			String createId, String createIp) {
		this.boardId = boardId;
		this.category = category;
		this.fileType = fileType;
		this.filePath = filePath;
		this.originalName = originalName;
		this.fileName = fileName;
		this.extension = extension;
		this.fileSize = fileSize;
		this.thumbnailYn = thumbnailYn;
		this.fileOrder = fileOrder;
		this.createId = createId;
		this.createIp = createIp;
	}

	public Long getId() { return id; }
	public Long getBoardId() { return boardId; }
	public String getCategory() { return category; }
	public String getFileType() { return fileType; }
	public String getFilePath() { return filePath; }
	public String getOriginalName() { return originalName; }
	public String getFileName() { return fileName; }
	public String getThumbnailYn() { return thumbnailYn; }
}
