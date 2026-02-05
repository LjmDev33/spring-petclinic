package org.springframework.samples.petclinic.counsel.service;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

	private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
	private final Path baseDir;
	private final Tika tika = new Tika();

	private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
		"image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp",
		"application/pdf", "application/msword",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
		"application/vnd.ms-excel",
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
		"application/x-hwp", "application/haansofthwp", "text/plain",
		"application/zip", "application/x-zip-compressed", "application/x-rar-compressed"
	);

	private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

	// 생성자: yml의 base-dir 하나만 주입받음
	public FileStorageService(@Value("${petclinic.file.base-dir}") String baseDirPath) {
		this.baseDir = Paths.get(baseDirPath);
	}

	/**
	 * 동적 경로 저장 (유지보수 용이성 강화)
	 * @param file 업로드 파일
	 * @param domain 도메인명 (counsel, photo, notice 등)
	 * @return 저장된 상대 경로
	 */
	public String storeFile(MultipartFile file, String domain) {
		try {
			validateFile(file);

			// 1. 보안 검증: 도메인명에 특수문자 포함 여부 확인 (경로 조작 방지)
			if (domain == null || !domain.matches("^[a-zA-Z0-9]+$")) {
				throw new IllegalArgumentException("Invalid domain name: " + domain);
			}

			// 2. 동적 경로 생성: {base}/{domain}/uploads
			// 예: C:/.../data/ + photo + /uploads
			Path domainPath = baseDir.resolve(domain).resolve("uploads");

			// 3. 날짜 폴더 생성 (yyyy/MM)
			LocalDate today = LocalDate.now();
			String year = today.format(DateTimeFormatter.ofPattern("yyyy"));
			String month = today.format(DateTimeFormatter.ofPattern("MM"));

			Path targetDir = domainPath.resolve(year).resolve(month);

			// 4. 폴더 없으면 자동 생성
			Files.createDirectories(targetDir);

			// 5. 파일 저장
			String extension = getExtension(file.getOriginalFilename()); // [수정] 아래 헬퍼 함수 사용
			String storedFileName = UUID.randomUUID().toString() + extension;

			Path destination = targetDir.resolve(storedFileName);

			// 경로 조작 방지 (Normalization check)
			if (!destination.normalize().startsWith(targetDir.normalize())) {
				throw new IllegalArgumentException("Invalid file path composition");
			}

			file.transferTo(destination);

			// 6. DB 저장용 상대 경로 반환: {domain}/uploads/{year}/{month}/{filename}
			// 예: photo/uploads/2026/02/uuid.jpg
			return Paths.get(domain, "uploads", year, month, storedFileName).toString().replace('\\', '/');

		} catch (IOException e) {
			log.error("Failed to store file {}: {}", file.getOriginalFilename(), e.getMessage());
			throw new RuntimeException("Failed to store file", e);
		}
	}

	/**
	 * 파일 유효성 검증
	 */
	private void validateFile(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("File is empty.");
		}

		try (InputStream inputStream = file.getInputStream()) {
			String mimeType = tika.detect(inputStream);
			if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
				log.warn("MIME type validation failed: {}", mimeType);
				throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + mimeType);
			}
		}

		if (file.getSize() > MAX_FILE_SIZE) {
			throw new IllegalArgumentException("파일 크기 초과 (최대 10MB)");
		}
	}

	/**
	 * [추가된 함수] 확장자 추출 헬퍼 메서드
	 */
	private String getExtension(String filename) {
		if (filename == null || !filename.contains(".")) {
			return "";
		}
		return filename.substring(filename.lastIndexOf("."));
	}

	/**
	 * 물리 파일 삭제
	 * @return true: 삭제 성공, false: 파일이 없거나 삭제 실패
	 */
	public boolean deleteFile(String filePath) {
		try {
			if (filePath == null || filePath.isBlank()) {
				return false;
			}

			Path fileToDelete = baseDir.resolve(filePath).normalize();

			// 파일이 존재하면 삭제 수행
			if (Files.exists(fileToDelete)) {
				Files.delete(fileToDelete);
				return true; // 삭제 성공
			} else {
				// 파일이 이미 없음 (Task 1 등에 의해 삭제됨)
				return false;
			}
		} catch (IOException e) {
			log.error("Failed to delete file: {}", filePath, e);
			return false; // 에러 발생
		}
	}
}
