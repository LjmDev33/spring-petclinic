package org.springframework.samples.petclinic.counsel.controller;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.common.table.Attachment;
import org.springframework.samples.petclinic.counsel.repository.AttachmentRepository;
import org.springframework.samples.petclinic.counsel.repository.CounselPostRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Project : spring-petclinic
 * File    : FileDownloadController.java
 * Created : 2025-11-06
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 온라인상담 첨부파일 다운로드 처리 및 권한 검증
 *   연관 기능:
 *     - UTF-8 파일명 인코딩
 *     - MIME 타입 전송
 *     - 비공개 게시글 파일 다운로드 권한 검증 (세션 + 관리자 권한)
 *
 *   권한 검증 로직:
 *     1. 파일이 속한 게시글 조회
 *     2. 공개 게시글: 모든 사용자 다운로드 가능
 *     3. 비공개 게시글 + 관리자(ROLE_ADMIN): 무조건 다운로드 가능
 *     4. 비공개 게시글 + 일반 사용자: 세션에 unlock된 게시글 ID가 있어야 다운로드 가능
 *     5. 권한 없으면 403 Forbidden 반환
 *
 *   개선 이력:
 *     - 2025-11-26: 관리자 권한 검증 추가 (Phase 1: 보안 강화)
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/counsel")
public class FileDownloadController {

	private static final Logger log = LoggerFactory.getLogger(FileDownloadController.class);

	// 세션 속성 키: 비밀번호 검증 완료된 게시글 ID 목록
	private static final String UNLOCKED_POSTS_SESSION_KEY = "counselUnlocked";

	private final Path baseDir;
	private final AttachmentRepository attachmentRepository;
	private final CounselPostRepository counselPostRepository;

	/**
	 * 생성자
	 * @param uploadDir 파일 업로드 디렉토리 경로
	 * @param attachmentRepository 첨부파일 저장소
	 * @param counselPostRepository 게시글 저장소
	 */
	public FileDownloadController(
		@Value("${petclinic.counsel.upload-dir:C:/eGovFrameDev-3.9.0-64bit/petclinic/data/counsel/uploads}") String uploadDir,
		AttachmentRepository attachmentRepository,
		CounselPostRepository counselPostRepository) {
		this.baseDir = Paths.get(uploadDir);
		this.attachmentRepository = attachmentRepository;
		this.counselPostRepository = counselPostRepository;
	}

	/**
	 * 파일 다운로드 처리
	 *
	 * <p>권한 검증 로직:</p>
	 * <ul>
	 *   <li>공개 게시글: 모든 사용자 다운로드 가능</li>
	 *   <li>비공개 게시글 + 관리자(ROLE_ADMIN): 무조건 다운로드 가능</li>
	 *   <li>비공개 게시글 + 일반 사용자: 세션에 unlock된 게시글 ID가 있어야 다운로드 가능</li>
	 *   <li>권한 없음: 403 Forbidden 반환</li>
	 * </ul>
	 *
	 * @param fileId 다운로드할 파일의 ID
	 * @param session HTTP 세션 (권한 검증용)
	 * @param authentication Spring Security 인증 객체 (관리자 권한 확인용, null 가능)
	 * @return 다운로드할 파일의 ResponseEntity 또는 403 에러
	 * @throws MalformedURLException 파일 경로가 잘못된 경우
	 */
	@GetMapping("/download/{fileId}")
	public ResponseEntity<Resource> downloadFile(
		@PathVariable Long fileId,
		HttpSession session,
		Authentication authentication)
		throws MalformedURLException {

		// NPE 방지: fileId null 체크
		if (fileId == null || fileId <= 0) {
			log.error("Invalid file ID: fileId={}", fileId);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}

		// NPE 방지: session null 체크
		if (session == null) {
			log.error("Session is null for file download: fileId={}", fileId);
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}

		log.info("File download request: fileId={}, sessionId={}, authenticated={}",
			fileId, session.getId(), authentication != null);

		// 1. 첨부파일 조회
		Attachment attachment = attachmentRepository.findById(fileId)
			.orElseThrow(() -> {
				log.error("File not found: fileId={}", fileId);
				return new IllegalArgumentException("Invalid file ID: " + fileId);
			});

		// 2. 파일이 속한 게시글 조회 (권한 검증용)
		CounselPost post = findPostByAttachment(attachment);
		if (post == null) {
			log.error("Post not found for attachment: fileId={}", fileId);
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}

		// 3. 권한 검증: 비공개 게시글인 경우
		if (post.isSecret()) {
			// 3-1. 관리자인 경우 무조건 허용
			if (isAdmin(authentication)) {
				log.info("Admin file download granted: fileId={}, postId={}, admin=true", fileId, post.getId());
			}
			// 3-2. 일반 사용자는 세션에 unlock된 게시글인지 확인
			else if (!isPostUnlocked(session, post.getId())) {
				log.warn("Unauthorized file download attempt: fileId={}, postId={}, secret=true, unlocked=false",
					fileId, post.getId());
				return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(null); // 권한 없음
			}
		}

		// 4. 파일 리소스 로드
		Path filePath = baseDir.resolve(attachment.getStoredFilename()).normalize();
		Resource resource = new UrlResource(filePath.toUri());

		if (!resource.exists() || !resource.isReadable()) {
			log.error("File not readable: fileId={}, filePath={}", fileId, filePath);
			throw new IllegalArgumentException("File not found or not readable: " + fileId);
		}

		// 5. 원본 파일명을 UTF-8로 인코딩하여 Content-Disposition 헤더에 설정
		String contentDisposition = "attachment; filename*=UTF-8''" +
			java.net.URLEncoder.encode(attachment.getOriginalFilename(), java.nio.charset.StandardCharsets.UTF_8)
				.replace("+", "%20");

		log.info("File download success: fileId={}, fileName={}, postId={}", fileId,
			attachment.getOriginalFilename(), post.getId());

		// 6. 파일 다운로드 응답
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
			.header(HttpHeaders.CONTENT_TYPE, attachment.getContentType())
			.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(attachment.getFileSize()))
			.body(resource);
	}

	/**
	 * 첨부파일이 속한 게시글 조회
	 *
	 * <p>게시글-첨부파일 관계는 중간 테이블(counsel_post_attachments)로 연결되어 있으므로,
	 * 게시글의 attachments 컬렉션을 순회하여 파일 ID가 일치하는 게시글을 찾습니다.</p>
	 *
	 * @param attachment 첨부파일 엔티티
	 * @return 게시글 엔티티 (없으면 null)
	 */
	private CounselPost findPostByAttachment(Attachment attachment) {
		return counselPostRepository.findAll().stream()
			.filter(post -> post.getAttachments() != null &&
				post.getAttachments().stream()
					.anyMatch(postAttachment ->
						postAttachment.getAttachment() != null &&
						postAttachment.getAttachment().getId().equals(attachment.getId())))
			.findFirst()
			.orElse(null);
	}

	/**
	 * 세션에서 게시글이 unlock되었는지 확인
	 *
	 * <p>비공개 게시글 상세 조회 시 비밀번호 검증에 성공하면,
	 * 세션에 해당 게시글 ID를 저장합니다. 이 메서드는 세션에 ID가 있는지 확인합니다.</p>
	 *
	 * @param session HTTP 세션
	 * @param postId 게시글 ID
	 * @return unlock 여부 (true: 권한 있음, false: 권한 없음)
	 */
	@SuppressWarnings("unchecked")
	private boolean isPostUnlocked(HttpSession session, Long postId) {
		// 세션에서 unlock된 게시글 ID 목록 조회
		Set<Long> unlockedPosts = (Set<Long>) session.getAttribute(UNLOCKED_POSTS_SESSION_KEY);

		// 세션에 unlock 정보가 없으면 빈 Set 생성
		if (unlockedPosts == null) {
			unlockedPosts = new HashSet<>();
			session.setAttribute(UNLOCKED_POSTS_SESSION_KEY, unlockedPosts);
		}

		// 게시글 ID가 unlock 목록에 있는지 확인
		return unlockedPosts.contains(postId);
	}

	/**
	 * 사용자가 관리자 권한을 가지고 있는지 확인
	 *
	 * <p>Spring Security의 Authentication 객체에서 권한(GrantedAuthority)을 조회하여,
	 * ROLE_ADMIN 권한이 있는지 확인합니다.</p>
	 *
	 * @param authentication Spring Security 인증 객체 (null 가능)
	 * @return 관리자 여부 (true: 관리자, false: 일반 사용자 또는 비로그인)
	 */
	private boolean isAdmin(Authentication authentication) {
		if (authentication == null) {
			return false;
		}

		return authentication.getAuthorities().stream()
			.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
	}

}

