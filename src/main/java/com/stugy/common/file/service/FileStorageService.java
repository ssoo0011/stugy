package com.stugy.common.file.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.stugy.common.file.domain.FileMetadata;
import com.stugy.common.file.dto.response.StoredFile;
import com.stugy.common.file.repository.FileMetadataRepository;

@Service
public class FileStorageService {

	public static final String PROFILE_IMAGE = "PROFILE_IMAGE";
	private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
	private static final int THUMBNAIL_MAX_SIZE = 320;

	private final FileMetadataRepository fileRepository;
	private final Path storageRoot;

	public FileStorageService(FileMetadataRepository fileRepository, @Value("${app.file.storage-root}") String storageRoot) {
		this.fileRepository = fileRepository;
		this.storageRoot = Path.of(storageRoot).toAbsolutePath().normalize();
	}

	@Transactional
	public void replaceProfileImage(Long userId, MultipartFile file, String createId, String createIp) throws IOException {
		if (file == null || file.isEmpty()) {
			return;
		}
		validateImage(file);
		deleteByReference(userId, PROFILE_IMAGE);

		Path directory = storageRoot.resolve("profile").resolve(LocalDate.now().toString()).normalize();
		Files.createDirectories(directory);
		String extension = getExtension(file.getOriginalFilename());
		String originalFileName = UUID.randomUUID() + "." + extension;
		Path originalPath = directory.resolve(originalFileName);
		Path thumbnailPath = directory.resolve(UUID.randomUUID() + ".jpg");
		try {
			Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);
			createThumbnail(originalPath, thumbnailPath);
			fileRepository.save(toMetadata(userId, file, toRelativePath(originalPath), originalFileName, extension, "N",
					createId, createIp));
			fileRepository.save(new FileMetadata(userId, PROFILE_IMAGE, "image/jpeg", toRelativePath(thumbnailPath),
					file.getOriginalFilename(), thumbnailPath.getFileName().toString(), "jpg",
					Math.toIntExact(Files.size(thumbnailPath)), "Y", 0, limit(createId, 15), limit(createIp, 18)));
		} catch (RuntimeException | IOException exception) {
			Files.deleteIfExists(originalPath);
			Files.deleteIfExists(thumbnailPath);
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public StoredFile load(Long fileId) throws IOException {
		FileMetadata metadata = fileRepository.findById(fileId)
				.orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));
		Path path = resolveStoredPath(metadata.getFilePath());
		Resource resource = new UrlResource(path.toUri());
		if (!resource.exists() || !resource.isReadable()) {
			throw new IllegalArgumentException("파일을 찾을 수 없습니다.");
		}
		return new StoredFile(resource, metadata.getFileType(), metadata.getOriginalName());
	}

	@Transactional(readOnly = true)
	public Long findProfileThumbnailId(Long userId) {
		return fileRepository.findFirstByBoardIdAndCategoryAndThumbnailYnOrderByIdDesc(userId, PROFILE_IMAGE, "Y")
				.map(FileMetadata::getId)
				.orElse(null);
	}

	@Transactional
	public void deleteProfileImage(Long userId) throws IOException {
		deleteByReference(userId, PROFILE_IMAGE);
	}

	private void deleteByReference(Long boardId, String category) throws IOException {
		List<FileMetadata> files = fileRepository.findAllByBoardIdAndCategory(boardId, category);
		for (FileMetadata file : files) {
			Files.deleteIfExists(resolveStoredPath(file.getFilePath()));
		}
		fileRepository.deleteAll(files);
	}

	private FileMetadata toMetadata(Long userId, MultipartFile file, String relativePath, String storedName,
			String extension, String thumbnailYn, String createId, String createIp) throws IOException {
		return new FileMetadata(userId, PROFILE_IMAGE, file.getContentType(), relativePath, file.getOriginalFilename(),
				storedName, extension, Math.toIntExact(file.getSize()), thumbnailYn, 0, limit(createId, 15), limit(createIp, 18));
	}

	private void validateImage(MultipartFile file) {
		String extension = getExtension(file.getOriginalFilename());
		if (file.getContentType() == null || !file.getContentType().startsWith("image/") || !IMAGE_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException("프로필 이미지는 jpg, png, gif 파일만 업로드할 수 있습니다.");
		}
	}

	private String getExtension(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			throw new IllegalArgumentException("파일 확장자를 확인해 주세요.");
		}
		return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
	}

	private void createThumbnail(Path source, Path target) throws IOException {
		BufferedImage original = ImageIO.read(source.toFile());
		if (original == null) {
			throw new IllegalArgumentException("이미지 파일을 읽을 수 없습니다.");
		}
		double scale = Math.min(1.0, (double) THUMBNAIL_MAX_SIZE / Math.max(original.getWidth(), original.getHeight()));
		int width = Math.max(1, (int) Math.round(original.getWidth() * scale));
		int height = Math.max(1, (int) Math.round(original.getHeight() * scale));
		long originalSize = Files.size(source);

		for (float quality : new float[] { .7f, .5f, .3f, .15f }) {
			writeJpegThumbnail(original, target, width, height, quality);
			if (Files.size(target) < originalSize) {
				return;
			}
		}

		while (width > 48 && height > 48) {
			width = Math.max(1, width / 2);
			height = Math.max(1, height / 2);
			writeJpegThumbnail(original, target, width, height, .3f);
			if (Files.size(target) < originalSize) {
				return;
			}
		}
	}

	private void writeJpegThumbnail(BufferedImage original, Path target, int width, int height, float quality) throws IOException {
		BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = thumbnail.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		graphics.drawImage(original, 0, 0, width, height, null);
		graphics.dispose();
		ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
		ImageWriteParam params = writer.getDefaultWriteParam();
		params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		params.setCompressionQuality(quality);
		Files.deleteIfExists(target);
		try (FileImageOutputStream output = new FileImageOutputStream(target.toFile())) {
			writer.setOutput(output);
			writer.write(null, new javax.imageio.IIOImage(thumbnail, null, null), params);
		} finally {
			writer.dispose();
		}
	}

	private String toRelativePath(Path path) {
		return storageRoot.relativize(path.toAbsolutePath().normalize()).toString().replace('\\', '/');
	}

	private Path resolveStoredPath(String relativePath) {
		Path resolvedPath = storageRoot.resolve(relativePath).normalize();
		if (!resolvedPath.startsWith(storageRoot)) {
			throw new IllegalArgumentException("올바르지 않은 파일 경로입니다.");
		}
		return resolvedPath;
	}

	private String limit(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}
}
