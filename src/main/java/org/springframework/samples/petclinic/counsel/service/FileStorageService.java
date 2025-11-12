package org.springframework.samples.petclinic.counsel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.Tika;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeongmin Lee
 * @description 파일 저장 및 관리를 담당하는 서비스 클래스.
 * 이 클래스는 파일 시스템에 파일을 저장하고, 경로를 생성하며, 파일 유효성을 검증합니다.
 * 아직 구현되지 않은 기능:
 * - 파일 삭제 (물리적 파일 삭제)
 * - 파일 다운로드 스트림 제공
 */
@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final Path baseDir;
    private final Tika tika = new Tika();
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public FileStorageService(@Value("${petclinic.counsel.upload-dir:C:/eGovFrameDev-3.9.0-64bit/petclinic/data/counsel/uploads}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir);
    }

    /**
     * MultipartFile을 지정된 경로에 저장합니다.
     * @param file 저장할 MultipartFile
     * @return 저장된 파일의 경로
     * @throws IOException 파일 저장 중 오류 발생 시
     * @throws IllegalArgumentException 유효하지 않은 파일(MIME 타입, 크기 등)일 경우
     */
    public String storeFile(MultipartFile file) {
        // 1. 파일 유효성 검증
        try {
            validateFile(file);
        } catch (IOException e) {
            log.error("File validation failed for file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("File validation failed.", e);
        }

        try {
            // 2. 저장 경로 생성 (yyyy/MM)
            LocalDate today = LocalDate.now();
            String year = today.format(DateTimeFormatter.ofPattern("yyyy"));
            String month = today.format(DateTimeFormatter.ofPattern("MM"));
            Path targetDir = baseDir.resolve(year).resolve(month);

            // 3. 디렉토리 생성 (존재하지 않을 경우)
            Files.createDirectories(targetDir);

            // 4. 파일명 난수화 (UUID) 및 확장자 추출
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String storedFileName = UUID.randomUUID().toString() + extension;

            // 5. 경로 역참조 방지
            Path destination = targetDir.resolve(storedFileName);
            if (!destination.normalize().startsWith(targetDir.normalize())) {
                throw new IllegalArgumentException("Invalid file path.");
            }

            // 6. 파일 저장
            file.transferTo(destination);

            // 7. 상대 경로 반환
            return Paths.get(year, month, storedFileName).toString().replace('\\', '/');
        } catch (IOException e) {
            log.error("Failed to store file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    /**
     * 파일의 유효성을 검증합니다 (MIME 타입, 크기).
     * @param file 검증할 MultipartFile
     * @throws IOException 파일 읽기 중 오류 발생 시
     * @throws IllegalArgumentException 유효성 검증 실패 시
     */
    private void validateFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        // MIME 타입 검증 (try-with-resources로 InputStream 자동 해제)
        try (InputStream inputStream = file.getInputStream()) {
            String mimeType = tika.detect(inputStream);
            if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
                throw new IllegalArgumentException("Invalid MIME type: " + mimeType);
            }
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the limit of " + MAX_FILE_SIZE / (1024 * 1024) + "MB.");
        }
    }

    /**
     * 지정된 경로의 파일을 물리적으로 삭제합니다.
     * @param filePath 삭제할 파일의 상대 경로
     */
    public void deleteFile(String filePath) {
        try {
            Path fileToDelete = baseDir.resolve(filePath).normalize();
            if (Files.exists(fileToDelete)) {
                Files.delete(fileToDelete);
                log.info("Successfully deleted physical file: {}", filePath);
            } else {
                log.warn("Attempted to delete a non-existent file: {}", filePath);
            }
        } catch (IOException e) {
            log.error("Failed to delete physical file {}: {}", filePath, e.getMessage());
            // 물리적 파일 삭제 실패가 전체 트랜잭션을 롤백시키지 않도록 예외를 다시 던지지 않을 수 있습니다.
            // 또는 특정 비즈니스 예외를 던져 처리할 수 있습니다.
        }
    }
}
