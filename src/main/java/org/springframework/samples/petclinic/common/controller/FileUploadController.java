package org.springframework.samples.petclinic.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.common.dto.UploadResponse;
import org.springframework.samples.petclinic.common.repository.AttachmentRepository;
import org.springframework.samples.petclinic.common.table.Attachment;
import org.springframework.samples.petclinic.counsel.service.FileStorageService; // 서비스 패키지 경로 확인 필요
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 * Project : spring-petclinic
 * File    : FileUploadController.java
 * Created : 2026-02-04
 * Author  : Jeongmin Lee
 *
 * Description :
 * 공통 파일 업로드 컨트롤러 (에디터 이미지 & 일반 첨부파일 공용)
 * - 위치: common 패키지 (전사 공통 사용)
 * - 역할: 물리적 파일 저장 및 DB 메타데이터 생성, JSON 응답 반환
 * - 보안: Lombok 미사용, 명시적 Getter/Setter 구현
 */
@Controller
@RequestMapping("/api/files")
public class FileUploadController {

	private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

	private final FileStorageService fileStorageService;
	private final AttachmentRepository attachmentRepository;

	public FileUploadController(FileStorageService fileStorageService, AttachmentRepository attachmentRepository) {
		this.fileStorageService = fileStorageService;
		this.attachmentRepository = attachmentRepository;
	}

	/**
	 * 통합 파일 업로드 (AJAX)
	 * @param file 업로드할 파일
	 * @return UploadResponse (JSON) - ID, 파일명, URL, 크기 포함
	 */
	@PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file,
													 @RequestParam("domain") String domain) {
		try {
			// 1. 물리적 파일 저장 (FileStorageService 위임)
			// 반환값: yyyy/MM/filename 형태의 상대 경로
			String storedPath = fileStorageService.storeFile(file, domain);

			// 2. DB 메타데이터 저장 (Attachment 엔티티 - Lombok 미사용)
			Attachment attachment = new Attachment();
			attachment.setOriginalFilename(file.getOriginalFilename());
			attachment.setStoredFilename(storedPath);
			attachment.setFileSize(file.getSize());
			attachment.setContentType(file.getContentType());
			// created_at 등은 @CreationTimestamp에 의해 자동 처리됨

			// 저장 (ID 생성)
			attachment = attachmentRepository.save(attachment);

			log.info("File Uploaded: id={}, name={}", attachment.getId(), storedPath);

			// 3. 응답 DTO 생성 (접근 URL 포함)
			// WebMvcConfig에서 설정한 "/images/uploads/**" 패턴 활용
			String accessUrl = "/images/" + storedPath;

			UploadResponse response = new UploadResponse(
				attachment.getId(),
				attachment.getOriginalFilename(),
				accessUrl,
				attachment.getFileSize()
			);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			log.warn("Upload validation failed: {}", e.getMessage());
			// 클라이언트에게 명시적인 에러 메시지 전달을 위해 null 대신 빈 객체 혹은 에러 메시지 처리가 좋으나,
			// 현재 구조상 Bad Request 상태코드만 반환
			return ResponseEntity.badRequest().build();
		} catch (Exception e) {
			log.error("Server upload error", e);
			return ResponseEntity.internalServerError().build();
		}
	}

}
