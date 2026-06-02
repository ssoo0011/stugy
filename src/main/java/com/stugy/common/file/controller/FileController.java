package com.stugy.common.file.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stugy.common.file.dto.response.StoredFile;
import com.stugy.common.file.service.FileStorageService;

@RestController
@RequestMapping("/api/files")
public class FileController {

	private final FileStorageService fileStorageService;

	public FileController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@GetMapping("/{fileId}")
	public ResponseEntity<Resource> download(@PathVariable Long fileId) throws IOException {
		StoredFile storedFile = fileStorageService.load(fileId);
		ContentDisposition disposition = ContentDisposition.inline()
				.filename(storedFile.originalName(), StandardCharsets.UTF_8)
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
				.contentType(storedFile.contentType() == null ? MediaType.APPLICATION_OCTET_STREAM
						: MediaType.parseMediaType(storedFile.contentType()))
				.body(storedFile.resource());
	}
}
