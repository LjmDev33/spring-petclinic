package org.springframework.samples.petclinic.counsel.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.common.exception.EntityNotFoundException;
import org.springframework.samples.petclinic.common.exception.ErrorCode;
import org.springframework.samples.petclinic.common.exception.FileException;
import org.springframework.samples.petclinic.common.repository.AttachmentRepository;
import org.springframework.samples.petclinic.counsel.CounselStatus;
import org.springframework.samples.petclinic.counsel.repository.CounselPostLikeRepository;
import org.springframework.samples.petclinic.user.repository.UserRepository;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.dto.CounselPostDto;
import org.springframework.samples.petclinic.counsel.dto.CounselPostWriteDto;
import org.springframework.samples.petclinic.counsel.mapper.CounselPostMapper;
import org.springframework.samples.petclinic.counsel.repository.CounselPostRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.samples.petclinic.counsel.dto.CounselCommentDto;
import org.springframework.samples.petclinic.counsel.mapper.CounselCommentMapper;
import org.springframework.samples.petclinic.counsel.repository.CounselCommentRepository;
import org.springframework.samples.petclinic.counsel.table.CounselComment;
import org.springframework.samples.petclinic.common.table.Attachment;
import org.springframework.samples.petclinic.counsel.table.CounselPostAttachment;
import org.springframework.samples.petclinic.counsel.table.CounselPostLike;
import org.springframework.samples.petclinic.counsel.repository.CounselPostAttachmentRepository;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project : spring-petclinic
 * File    : CounselService.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   온라인상담 게시판 비즈니스 로직 Service
 *
 * Purpose (만든 이유):
 *   1. 온라인상담 게시판의 모든 비즈니스 로직을 중앙 집중화
 *   2. Entity를 직접 노출하지 않고 DTO 변환하여 Controller와 연동
 *   3. 트랜잭션 관리 및 데이터 검증 담당
 *   4. 비공개글 비밀번호 보안 처리 (BCrypt)
 *   5. 댓글 트리 구조 생성 및 관리 (무제한 depth)
 *   6. 첨부파일 업로드/삭제 관리 (Uppy 연동)
 *
 * Key Features (주요 기능):
 *   - 게시글 CRUD (생성, 조회, 수정, 삭제 - Soft Delete)
 *   - 댓글 CRUD (생성, 조회, 삭제 - 트리 구조 지원)
 *   - 비공개글 비밀번호 검증 (BCrypt)
 *   - QueryDSL 기반 동적 검색 (제목, 제목+내용)
 *   - 페이징 처리 (PageResponse 반환)
 *   - 첨부파일 업로드/삭제 (Uppy, MultipartFile 모두 지원)
 *   - 본문 HTML 파일 저장 (CounselContentStorage 연동)
 *   - 조회수 증가 (중복 방지는 Controller에서 처리)
 *   - 댓글 트리 구조 생성 (무제한 depth, 부모-자식 관계)
 *
 * Business Rules (비즈니스 규칙):
 *   - Entity는 절대 직접 노출하지 않음 (DTO 변환 필수)
 *   - 비공개글은 BCrypt로 비밀번호 해싱
 *   - 게시글 삭제는 Soft Delete (@SQLDelete)
 *   - 첨부파일도 Soft Delete (del_flag = true, 2주 후 물리 삭제)
 *   - 댓글 삭제 시 자식 댓글이 있으면 삭제 불가
 *   - 운영자 댓글은 사용자가 삭제 불가
 *
 * Transaction Management (트랜잭션 관리):
 *   - @Transactional 클래스 레벨 적용 (모든 public 메서드)
 *   - 조회 메서드도 트랜잭션 내에서 실행 (지연 로딩 지원)
 *   - 예외 발생 시 자동 롤백
 *
 * Usage Examples (사용 예시):
 *   // 게시글 목록 조회
 *   PageResponse<CounselPostDto> page = counselService.getPagedPosts(pageable);
 *
 *   // 게시글 상세 조회 (파일에서 본문 로드)
 *   CounselPostDto post = counselService.getDetail(postId);
 *
 *   // 게시글 작성 (파일 업로드 포함)
 *   Long postId = counselService.saveNew(writeDto);
 *
 *   // 비밀번호 검증
 *   boolean valid = counselService.verifyPassword(postId, password);
 *
 *   // 댓글 트리 조회 (부모-자식 관계)
 *   List<CounselCommentDto> comments = counselService.getCommentsForPost(postId);
 *
 * Dependencies (의존 관계):
 *   - CounselPostRepository: 게시글 DB 접근
 *   - CounselCommentRepository: 댓글 DB 접근
 *   - AttachmentRepository: 첨부파일 DB 접근
 *   - CounselContentStorage: HTML 본문 파일 I/O
 *   - FileStorageService: 첨부파일 저장/삭제
 *   - BCrypt: 비밀번호 해싱
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class CounselService {

	private static final Logger log = LoggerFactory.getLogger(CounselService.class);
	private final CounselPostRepository repository;
	private final CounselContentStorage contentStorage;
	private final CounselCommentRepository commentRepository;
	private final CounselPostMapper postMapper;
	private final FileStorageService fileStorageService;
	private final AttachmentRepository attachmentRepository;
	private final CounselPostAttachmentRepository postAttachmentRepository;
	private final CounselPostLikeRepository likeRepository;
	private final UserRepository userRepository;

	public CounselService(CounselPostRepository repository, CounselContentStorage contentStorage,
						  CounselCommentRepository commentRepository, CounselPostMapper postMapper,
						  FileStorageService fileStorageService, AttachmentRepository attachmentRepository,
						  CounselPostAttachmentRepository postAttachmentRepository,
						  CounselPostLikeRepository likeRepository,
						  UserRepository userRepository) {
		this.repository = repository;
		this.contentStorage = contentStorage;
		this.commentRepository = commentRepository;
		this.likeRepository = likeRepository;
		this.postMapper = postMapper;
		this.fileStorageService = fileStorageService;
		this.attachmentRepository = attachmentRepository;
		this.postAttachmentRepository = postAttachmentRepository;
		this.userRepository = userRepository;
	}

	/**
	 * 페이징 목록을 DTO로 변환하여 반환합니다.
	 * @param pageable 페이지 요청 정보
	 * @return PageResponse<CounselPostDto>
	 */
	public PageResponse<CounselPostDto> getPagedPosts(Pageable pageable){
		Page<CounselPost> entityPage = repository.findAll(pageable);
		List<CounselPostDto> dtoList = entityPage.getContent().stream().map(postMapper::toDto).collect(Collectors.toList());
		// 최근 댓글 요약 주입
		for (CounselPostDto d : dtoList) {
			commentRepository.findTopByPost_IdOrderByCreatedAtDesc(d.getId()).ifPresent(c -> {
				d.setLastCommentTitle("댓글");
				d.setLastCommentAuthor(c.getAuthorName());
				d.setLastCommentCreatedAt(c.getCreatedAt());
			});
		}
		Page<CounselPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityPage.getTotalElements());
		return new PageResponse<>(dtoPage);
	}

	/**
	 * QueryDSL 기반 검색 결과를 DTO 페이지로 변환합니다.
	 */
	public PageResponse<CounselPostDto> search(String type, String keyword, Pageable pageable) {
		PageResponse<CounselPost> entityResponse = repository.search(type,keyword,pageable);
		List<CounselPostDto> dtoList = entityResponse.getContent().stream().map(postMapper::toDto).collect(Collectors.toList());
		for (CounselPostDto d : dtoList) {
			commentRepository.findTopByPost_IdOrderByCreatedAtDesc(d.getId()).ifPresent(c -> {
				d.setLastCommentTitle("댓글");
				d.setLastCommentAuthor(c.getAuthorName());
				d.setLastCommentCreatedAt(c.getCreatedAt());
			});
		}
		Page<CounselPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityResponse.getTotalElements());
		return new PageResponse<>(dtoPage);
	}

	/**
	 * 고급 검색 (Phase 7: 검색 기능 강화)
	 * - 날짜 범위, 상태별 필터링 추가
	 *
	 * @param type 검색 타입 (title, content, author, 전체)
	 * @param keyword 검색 키워드
	 * @param status 상태 필터 (WAIT, COMPLETE, END, null=전체)
	 * @param startDateStr 시작 날짜 문자열 (yyyy-MM-dd)
	 * @param endDateStr 종료 날짜 문자열 (yyyy-MM-dd)
	 * @param pageable 페이징 정보
	 * @return 검색 결과 페이지
	 */
	public PageResponse<CounselPostDto> advancedSearch(
		String type,
		String keyword,
		String status,
		String startDateStr,
		String endDateStr,
		Pageable pageable) {

		// 문자열 날짜를 LocalDateTime으로 변환
		LocalDateTime startDate = null;
		LocalDateTime endDate = null;

		try {
			if (startDateStr != null && !startDateStr.isBlank()) {
				startDate = LocalDate.parse(startDateStr).atStartOfDay();
			}
			if (endDateStr != null && !endDateStr.isBlank()) {
				endDate = LocalDate.parse(endDateStr).atStartOfDay();
			}
		} catch (DateTimeParseException e) {
			log.error("Invalid date format: startDate={}, endDate={}", startDateStr, endDateStr);
			// 날짜 파싱 실패 시 null로 유지
		}

		// Repository 호출
		PageResponse<CounselPost> entityResponse = repository.advancedSearch(
			type, keyword, status, startDate, endDate, pageable);

		// Entity -> DTO 변환
		List<CounselPostDto> dtoList = entityResponse.getContent().stream()
			.map(postMapper::toDto)
			.collect(Collectors.toList());

		// 최근 댓글 요약 주입
		for (CounselPostDto d : dtoList) {
			commentRepository.findTopByPost_IdOrderByCreatedAtDesc(d.getId()).ifPresent(c -> {
				d.setLastCommentTitle("댓글");
				d.setLastCommentAuthor(c.getAuthorName());
				d.setLastCommentCreatedAt(c.getCreatedAt());
			});
		}

		Page<CounselPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityResponse.getTotalElements());
		return new PageResponse<>(dtoPage);
	}

	/**
	 * 상세 조회 시 contentPath가 존재하면 파일에서 본문을 로드하여 DTO.content에 채웁니다.
	 * GlobalExceptionHandler 적용: Custom Exception 사용
	 */
	public CounselPostDto getDetail(Long id) {
		// EntityNotFoundException 적용
		CounselPost entity = repository.findById(id)
			.orElseThrow(() -> EntityNotFoundException.of("CounselPost", id));

		CounselPostDto dto = postMapper.toDto(entity);

		// FileException 적용
		if (dto.getContentPath() != null && !dto.getContentPath().isBlank()) {
			try {
				String html = contentStorage.loadHtml(dto.getContentPath());
				dto.setContent(html);
			} catch (IOException e) {
				throw new FileException(ErrorCode.FILE_READ_ERROR, e);
			}
		}
		return dto;
	}

	/**
	 * 신규 글 저장: 본문 파일 저장 → 경로 세팅 → DTO 비밀번호는 BCrypt로 해싱하여 엔티티에 저장합니다.
	 * GlobalExceptionHandler 적용: FileException 사용
	 * @return 생성된 게시글 ID
	 */
	public Long saveNew(CounselPostWriteDto dto) {
		// 1. 본문 저장 (FileException 적용)
		String path;
		try {
			path = contentStorage.saveHtml(dto.getContent());
		} catch (IOException e) {
			log.error("Failed to save content HTML for new post: {}", dto.getTitle(), e);
			throw new FileException(ErrorCode.FILE_WRITE_ERROR, e);
		}

		// 2. 엔티티 생성 및 기본 정보 설정
		CounselPost entity = new CounselPost();
		entity.setTitle(dto.getTitle());
		entity.setAuthorName(dto.getAuthorName());
		entity.setSecret(dto.isSecret());
		entity.setContentPath(path);
		entity.setContent("[stored]"); // 본문은 파일에 저장되었으므로 대체 텍스트 사용

		// 3. 비밀번호 해싱
		if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
			entity.setPasswordHash(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt()));
		}

		// 4. 첨부파일 처리 (Uppy 업로드된 파일 경로)
		if (dto.getAttachmentPaths() != null && !dto.getAttachmentPaths().isBlank()) {
			String[] filePaths = dto.getAttachmentPaths().split(",");

			for (String filePath : filePaths) {
				filePath = filePath.trim();
				if (filePath.isEmpty()) continue;

				try {
					// Attachment 엔티티 생성 및 저장 (common.table.Attachment)
					Attachment attachment = new Attachment();
					attachment.setStoredFilename(filePath); // 저장된 파일명
					attachment.setOriginalFilename(extractFileName(filePath)); // 원본 파일명
					attachment.setFileSize(0L); // 임시 업로드 시 크기 정보 없음
					attachment.setContentType("application/octet-stream"); // MIME 타입
					attachmentRepository.save(attachment);

					// CounselPost와 Attachment 연결
					CounselPostAttachment postAttachment = new CounselPostAttachment();
					postAttachment.setCounselPost(entity);
					postAttachment.setAttachment(attachment);
					entity.addAttachment(postAttachment);

					log.info("Attached file to post: path={}", filePath);
				} catch (Exception e) {
					log.error("Failed to attach file {}: {}", filePath, e.getMessage());
					throw new RuntimeException("Error attaching file.", e);
				}
			}
			entity.setAttachFlag(true); // 첨부파일 플래그 설정
		}

		// 5. 기존 MultipartFile 방식 첨부파일 처리 (하위 호환성 유지)
		if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
			for (MultipartFile file : dto.getAttachments()) {
				if (file.isEmpty()) continue;

				try {
					// 파일 저장
					String storedFilePath = fileStorageService.storeFile(file, "counsel");

					// Attachment 엔티티 생성 및 저장 (common.table.Attachment)
					Attachment attachment = new Attachment();
					attachment.setStoredFilename(storedFilePath); // 저장된 파일명
					attachment.setOriginalFilename(file.getOriginalFilename()); // 원본 파일명
					attachment.setFileSize(file.getSize());
					attachment.setContentType(file.getContentType()); // MIME 타입
					attachmentRepository.save(attachment);

					// CounselPost와 Attachment 연결
					CounselPostAttachment postAttachment = new CounselPostAttachment();
					postAttachment.setCounselPost(entity);
					postAttachment.setAttachment(attachment);
					entity.addAttachment(postAttachment);
				} catch (Exception e) {
					log.error("Failed to process attachment file {}: {}", file.getOriginalFilename(), e.getMessage());
					throw new RuntimeException("Error processing attachment.", e);
				}
			}
			entity.setAttachFlag(true); // 첨부파일 플래그 설정
		}

		// 5. 게시글 저장
		CounselPost saved = repository.save(entity);
		return saved.getId();
	}

	/**
	 * 비공개글의 비밀번호를 검증합니다. 저장된 BCrypt 해시와 비교합니다.
	 */
	public boolean verifyPassword(Long postId, String rawPassword){
		CounselPost entity = repository.findById(postId).orElse(null);
		if (entity == null) return false;
		if (entity.getPasswordHash() == null || entity.getPasswordHash().isBlank()) return false;
		return BCrypt.checkpw(rawPassword == null ? "" : rawPassword, entity.getPasswordHash());
	}

	/**
	 * 게시글의 댓글 목록을 DTO로 반환합니다.
	 */
	public List<CounselCommentDto> getCommentsForPost(Long postId){
		List<CounselComment> allComments = commentRepository.findByPost_IdOrderByCreatedAtAsc(postId);

		// Tree 구조로 변환
		Map<Long, CounselCommentDto> commentMap = new HashMap<>();
		List<CounselCommentDto> rootComments = new ArrayList<>();

		// 1단계: 모든 댓글을 DTO로 변환하고 Map에 저장
		for (CounselComment comment : allComments) {
			CounselCommentDto dto = CounselCommentMapper.toDto(comment);

			// 부모 댓글 작성자 이름 설정
			if (comment.getParent() != null) {
				dto.setParentAuthorName(comment.getParent().getAuthorName());
				dto.setParentId(comment.getParent().getId());
			}

			commentMap.put(dto.getId(), dto);
		}

		// 2단계: Tree 구조 생성 (부모-자식 관계 설정)
		for (CounselComment comment : allComments) {
			CounselCommentDto dto = commentMap.get(comment.getId());

			if (dto == null) {
				// 예상치 못한 상황: Map에 없는 댓글
				log.warn("Comment ID {} not found in commentMap", comment.getId());
				continue;
			}

			if (comment.getParent() == null) {
				// 최상위 댓글
				dto.setDepth(0);
				rootComments.add(dto);
			} else {
				// 대댓글: 부모의 children에 추가
				CounselCommentDto parent = commentMap.get(comment.getParent().getId());
				if (parent != null) {
					parent.getChildren().add(dto);
					dto.setDepth(parent.getDepth() + 1);
				} else {
					// 부모를 찾을 수 없는 경우 (orphaned comment) 최상위로 처리
					log.warn("Orphaned comment detected: Comment ID {} has non-existent parent ID {}",
							 comment.getId(), comment.getParent().getId());
					dto.setDepth(0);
					dto.setParentId(null); // orphaned이므로 parentId 제거
					dto.setParentAuthorName(null);
					rootComments.add(dto);
				}
			}
		}

		return rootComments;
	}

	/**
	 * 댓글을 생성합니다.
	 * @param postId 게시글 ID
	 * @param commentDto 댓글 DTO
	 * @return 생성된 댓글 DTO
	 */
	public CounselCommentDto createComment(Long postId, CounselCommentDto commentDto) {
		CounselPost post = repository.findById(postId)
			.orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));

		CounselComment comment = new CounselComment();
		comment.setPost(post);
		comment.setAuthorName(commentDto.getAuthorName());
		comment.setAuthorEmail(commentDto.getAuthorEmail());
		comment.setContent(commentDto.getContent()); // Jsoup sanitize는 컨트롤러에서 처리
		comment.setStaffReply(false); // 사용자 댓글은 항상 false

		if (commentDto.getPassword() != null && !commentDto.getPassword().isBlank()) {
			comment.setPasswordHash(BCrypt.hashpw(commentDto.getPassword(), BCrypt.gensalt()));
		}

		// 대댓글 처리 (1-depth)
		if (commentDto.getParentId() != null) {
			CounselComment parentComment = commentRepository.findById(commentDto.getParentId())
				.orElseThrow(() -> new IllegalArgumentException("Invalid parent comment ID: " + commentDto.getParentId()));
			comment.setParent(parentComment);
		}

		CounselComment savedComment = commentRepository.save(comment);
		return CounselCommentMapper.toDto(savedComment);
	}

	/**
	 * 댓글을 삭제합니다. 비밀번호가 있는 경우 검증합니다.
	 * @param commentId 댓글 ID
	 * @param password 비밀번호 (nullable)
	 * @return 삭제 성공 여부
	 */
	public boolean deleteComment(Long commentId, String password) {
		try {
			CounselComment comment = commentRepository.findById(commentId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid comment ID: " + commentId));

			// 운영자 댓글은 삭제 불가 (추후 권한 관리 기능 추가 시 변경)
			if (comment.isStaffReply()) {
				log.warn("Attempt to delete a staff comment (ID: {}) by a user.", commentId);
				throw new IllegalStateException("운영자 댓글은 삭제할 수 없습니다.");
			}

			// 비밀번호가 있는 댓글인 경우, 비밀번호 검증
			if (comment.getPasswordHash() != null && !comment.getPasswordHash().isBlank()) {
				if (!BCrypt.checkpw(password == null ? "" : password, comment.getPasswordHash())) {
					log.warn("Failed password verification for deleting comment ID: {}", commentId);
					throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
				}
			}

			// 자식 댓글(답글)이 있는지 확인
			List<CounselComment> children = commentRepository.findByParent_Id(commentId);
			if (!children.isEmpty()) {
				log.warn("Attempt to delete comment ID {} which has {} child comments", commentId, children.size());
				throw new IllegalStateException("답글이 있는 댓글은 삭제할 수 없습니다. 먼저 답글을 삭제해주세요.");
			}

			commentRepository.delete(comment);
			log.info("Successfully deleted comment with ID: {}", commentId);
			return true;
		} catch (IllegalStateException | IllegalArgumentException e) {
			// 비즈니스 로직 예외는 그대로 던져서 Controller에서 처리
			throw e;
		} catch (Exception e) {
			log.error("Error occurred while deleting comment ID {}: {}", commentId, e.getMessage(), e);
			throw new RuntimeException("댓글 삭제 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	/**
	 * 게시글을 수정합니다. 권한 검증 후 진행합니다.
	 *
	 * <p>권한 검증 우선순위:</p>
	 * <ol>
	 *   <li>관리자(ROLE_ADMIN): 무조건 허용</li>
	 *   <li>작성자 본인(로그인 사용자): 비밀번호 없이 허용</li>
	 *   <li>비공개 게시글 + 타인: 비밀번호 필요</li>
	 *   <li>공개 게시글: 비밀번호 없이 허용</li>
	 * </ol>
	 *
	 * @param postId 게시글 ID
	 * @param dto 수정할 내용
	 * @param password 비밀번호 (비공개글인 경우)
	 * @param authentication Spring Security 인증 객체 (작성자 확인용, null 가능)
	 * @return 수정 성공 여부
	 */
	public boolean updatePost(Long postId, CounselPostWriteDto dto, String password, Authentication authentication) {
		try {
			CounselPost entity = repository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));

			// 권한 검증
			if (!canModifyPost(entity, password, authentication)) {
				log.warn("Unauthorized attempt to update post ID: {}", postId);
				return false;
			}

			// 제목, 작성자 수정
			entity.setTitle(dto.getTitle());
			entity.setAuthorName(dto.getAuthorName());

			// 본문 수정 (기존 파일 삭제 후 새로 저장)
			if (dto.getContent() != null && !dto.getContent().isBlank()) {
				try {
					// 기존 본문 파일 삭제
					if (entity.getContentPath() != null && !entity.getContentPath().isBlank()) {
						contentStorage.deleteHtml(entity.getContentPath());
					}
					// 새 본문 저장
					String newPath = contentStorage.saveHtml(dto.getContent());
					entity.setContentPath(newPath);
					entity.setContent("[stored]");
				} catch (IOException e) {
					log.error("Failed to update content file for post ID: {}", postId, e);
					throw new RuntimeException("Error updating post content.", e);
				}
			}

			// 첨부파일 삭제 처리 (deletedFileIds)
			if (dto.getDeletedFileIds() != null && !dto.getDeletedFileIds().isBlank()) {
				String[] deletedIds = dto.getDeletedFileIds().split(",");

				for (String idStr : deletedIds) {
					idStr = idStr.trim();
					if (idStr.isEmpty()) continue;

					try {
						Long attachmentId = Long.parseLong(idStr);

						// Attachment 조회
						Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
						if (attachment == null) {
							log.warn("Attachment not found for deletion: id={}", attachmentId);
							continue;
						}

						// CounselPostAttachment 중간 테이블 삭제
						entity.getAttachments().removeIf(postAttachment ->
							postAttachment.getAttachment().getId().equals(attachmentId));

						// Attachment Soft Delete (del_flag = true)
						attachment.setDelFlag(true);
						attachment.setDeletedAt(LocalDateTime.now());
						attachmentRepository.save(attachment);

						log.info("Attachment marked for deletion: id={}, fileName={}",
							attachmentId, attachment.getOriginalFilename());
					} catch (NumberFormatException e) {
						log.error("Invalid attachment ID format: {}", idStr);
					}
				}
			}

			// 새 첨부파일 추가 처리 (Uppy 업로드된 파일 경로)
			if (dto.getAttachmentPaths() != null && !dto.getAttachmentPaths().isBlank()) {
				String[] filePaths = dto.getAttachmentPaths().split(",");

				for (String filePath : filePaths) {
					filePath = filePath.trim();
					if (filePath.isEmpty()) continue;

					try {
						// Attachment 엔티티 생성 및 저장 (common.table.Attachment)
						Attachment attachment = new Attachment();
						attachment.setStoredFilename(filePath); // 저장된 파일명
						attachment.setOriginalFilename(extractFileName(filePath)); // 원본 파일명
						attachment.setFileSize(0L); // 임시 업로드 시 크기 정보 없음
						attachment.setContentType("application/octet-stream"); // MIME 타입
						attachmentRepository.save(attachment);

						// CounselPost와 Attachment 연결
						CounselPostAttachment postAttachment = new CounselPostAttachment();
						postAttachment.setCounselPost(entity);
						postAttachment.setAttachment(attachment);
						entity.addAttachment(postAttachment);

						log.info("New attachment added to post: postId={}, path={}", postId, filePath);
					} catch (Exception e) {
						log.error("Failed to add attachment {}: {}", filePath, e.getMessage());
						throw new RuntimeException("Error adding attachment.", e);
					}
				}
			}

			// 첨부파일 플래그 업데이트
			entity.setAttachFlag(!entity.getAttachments().isEmpty());

			repository.save(entity);
			log.info("Successfully updated post with ID: {} (attachments: {})",
				postId, entity.getAttachments().size());
			return true;
		} catch (Exception e) {
			log.error("Error occurred while updating post ID {}: {}", postId, e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 게시글을 삭제합니다 (Soft Delete). 권한 검증 후 진행합니다.
	 *
	 * <p>권한 검증 우선순위는 updatePost와 동일합니다:</p>
	 * <ol>
	 *   <li>관리자(ROLE_ADMIN): 무조건 허용</li>
	 *   <li>작성자 본인(로그인 사용자): 비밀번호 없이 허용</li>
	 *   <li>비공개 게시글 + 타인: 비밀번호 필요</li>
	 *   <li>공개 게시글: 비밀번호 없이 허용</li>
	 * </ol>
	 *
	 * @param postId 게시글 ID
	 * @param password 비밀번호 (비공개글인 경우)
	 * @param authentication Spring Security 인증 객체 (작성자 확인용, null 가능)
	 * @return 삭제 성공 여부
	 */
	public boolean deletePost(Long postId, String password, Authentication authentication) {
		try {
			CounselPost entity = repository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));

			// 권한 검증
			if (!canModifyPost(entity, password, authentication)) {
				log.warn("Unauthorized attempt to delete post ID: {}", postId);
				return false;
			}

			// Soft Delete 실행 (@SQLDelete 어노테이션으로 처리)
			repository.delete(entity);
			log.info("Successfully soft-deleted post with ID: {} (title: {})", postId, entity.getTitle());
			return true;
		} catch (Exception e) {
			log.error("Error occurred while deleting post ID {}: {}", postId, e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 게시글 조회수를 증가시킵니다.
	 * @param postId 게시글 ID
	 */
	public void incrementViewCount(Long postId) {
		try {
			CounselPost entity = repository.findById(postId).orElse(null);
			if (entity != null) {
				entity.setViewCount(entity.getViewCount() + 1);
				repository.save(entity);
			}
		} catch (Exception e) {
			log.error("Error incrementing view count for post ID {}: {}", postId, e.getMessage());
			// 조회수 증가 실패는 치명적이지 않으므로 예외를 던지지 않음
		}
	}

	/**
	 * Uppy를 통한 임시 파일 저장 (게시글 작성 전 미리 업로드)
	 * - 파일을 임시 저장하고 파일 경로를 반환
	 * - 실제 게시글 저장 시 attachmentIds로 파일 경로를 전달받아 연결
	 *
	 * @param file 업로드된 파일
	 * @return 저장된 파일 경로
	 */
	public String storeFileTemp(MultipartFile file) {
		try {
			// FileStorageService를 통해 파일 저장
			String filePath = fileStorageService.storeFile(file , "counsel");

			log.info("Temp file stored: originalName={}, storedPath={}, size={}",
				file.getOriginalFilename(), filePath, file.getSize());

			return filePath;
		} catch (Exception e) {
			log.error("Failed to store temp file {}: {}", file.getOriginalFilename(), e.getMessage(), e);
			throw new RuntimeException("임시 파일 저장 중 오류가 발생했습니다.", e);
		}
	}

	/**
	 * 파일 경로에서 파일명 추출 (UUID 파일명)
	 * 예: "2025/11/abc123.jpg" → "abc123.jpg"
	 *
	 * @param filePath 파일 경로
	 * @return 파일명
	 */
	private String extractFileName(String filePath) {
		if (filePath == null || filePath.isBlank()) {
			return "unknown";
		}

		// Windows/Linux 경로 구분자 모두 처리
		String normalizedPath = filePath.replace('\\', '/');
		int lastSlash = normalizedPath.lastIndexOf('/');

		return lastSlash >= 0 ? normalizedPath.substring(lastSlash + 1) : normalizedPath;
	}

	/**
	 * 게시글 수정/삭제 권한 확인
	 *
	 * <p>권한 체크 우선순위:</p>
	 * <ol>
	 *   <li>관리자(ROLE_ADMIN): 무조건 허용</li>
	 *   <li>작성자 본인(로그인 사용자 닉네임 = 게시글 작성자): 비밀번호 없이 허용</li>
	 *   <li>비공개 게시글: 비밀번호 검증 필요</li>
	 *   <li>공개 게시글: 비밀번호 없이 허용</li>
	 * </ol>
	 *
	 * @param post 게시글 엔티티
	 * @param password 비밀번호 (null 가능)
	 * @param authentication Spring Security 인증 객체 (null 가능)
	 * @return 권한 여부 (true: 허용, false: 거부)
	 */
	/**
	 * 게시글 수정/삭제 권한 검증
	 *
	 * <p>권한 검증 우선순위:</p>
	 * <ol>
	 *   <li>관리자(ROLE_ADMIN): 모든 게시글 수정/삭제 가능 (비밀번호 불필요)</li>
	 *   <li>로그인 사용자 = 작성자: 본인 게시글 수정/삭제 가능 (비밀번호 불필요)</li>
	 *   <li>비공개 게시글: 비밀번호 검증 필요</li>
	 *   <li>공개 게시글: 비밀번호 없이 허용</li>
	 * </ol>
	 *
	 * <p>Phase 4-2: 작성자 권한 검증 강화 (2025-11-27)</p>
	 * <ul>
	 *   <li>UserRepository 연동하여 User.nickname과 post.authorName 비교</li>
	 *   <li>로그인 사용자가 작성자이면 비밀번호 입력 불필요</li>
	 * </ul>
	 *
	 * @param post 게시글 엔티티
	 * @param password 비밀번호 (비공개 게시글인 경우)
	 * @param authentication Spring Security 인증 객체 (null 가능)
	 * @return 권한 여부 (true: 수정/삭제 가능, false: 권한 없음)
	 */
	private boolean canModifyPost(CounselPost post, String password, Authentication authentication) {
		// 1. 관리자는 무조건 허용
		if (isAdmin(authentication)) {
			log.info("Admin authorized to modify post ID: {}", post.getId());
			return true;
		}

		// 2. 로그인 사용자가 작성자 본인인지 확인 (Phase 4-2: 강화)
		if (authentication != null) {
			String username = authentication.getName();

			// User 정보 조회하여 닉네임 비교
			try {
				User user = userRepository.findByUsername(username).orElse(null);
				if (user != null && post.getAuthorName() != null && post.getAuthorName().equals(user.getNickname())) {
					log.info("Author authorized to modify post ID: {} (author nickname={}, username={})",
						post.getId(), user.getNickname(), username);
					return true; // 작성자는 비밀번호 불필요
				}
			} catch (Exception e) {
				log.error("Error checking author for post ID: {}, error: {}", post.getId(), e.getMessage());
			}
		}

		// 3. 비공개 게시글인 경우 비밀번호 검증
		if (post.isSecret()) {
			if (password == null || password.isBlank()) {
				log.warn("Password required for modifying secret post ID: {}", post.getId());
				return false;
			}

			// 비밀번호 검증
			if (post.getPasswordHash() != null && BCrypt.checkpw(password, post.getPasswordHash())) {
				log.info("Password verified for modifying post ID: {}", post.getId());
				return true;
			} else {
				log.warn("Password verification failed for post ID: {}", post.getId());
				return false;
			}
		}

		// 4. 공개 게시글은 비밀번호 없이 허용
		log.info("Public post authorized to modify ID: {}", post.getId());
		return true;
	}

	/**
	 * 사용자가 관리자 권한을 가지고 있는지 확인
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

	/**
	 * 게시글 상태 변경 (관리자 전용)
	 *
	 * <p>관리자만 게시글의 상태(WAIT/COMPLETE/END)를 변경할 수 있습니다.</p>
	 *
	 * @param postId 게시글 ID
	 * @param newStatus 새 상태값 (WAIT/COMPLETE/END)
	 * @param authentication Spring Security 인증 객체 (관리자 확인용)
	 * @return 변경 성공 여부
	 * @throws IllegalStateException 관리자가 아닌 경우
	 * @throws IllegalArgumentException 잘못된 상태값
	 */
	@Transactional
	public boolean updatePostStatus(Long postId, String newStatus, Authentication authentication) {
		// 관리자 권한 확인
		if (!isAdmin(authentication)) {
			log.warn("Non-admin attempted to change post status: postId={}", postId);
			throw new IllegalStateException("관리자만 게시글 상태를 변경할 수 있습니다.");
		}

		try {
			CounselPost entity = repository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));

			// 상태값 검증 및 변환
			CounselStatus status;
			try {
				status = CounselStatus.valueOf(newStatus.toUpperCase());
			} catch (IllegalArgumentException e) {
				log.error("Invalid status value: {}", newStatus);
				throw new IllegalArgumentException("유효하지 않은 상태값입니다. (WAIT/COMPLETE/END만 가능)");
			}

			// 상태 변경
			entity.setStatus(status);
			repository.save(entity);

			log.info("Post status updated by admin: postId={}, oldStatus={}, newStatus={}, admin={}",
				postId, entity.getStatus(), status, authentication.getName());

			return true;
		} catch (Exception e) {
			log.error("Error updating post status: postId={}, error={}", postId, e.getMessage(), e);
			return false;
		}
	}

	/**
	 * 게시글 작성자 아이디 조회
	 *
	 * @param postId 게시글 ID
	 * @return username 작성자 ID
	 */
	public String getBoardOnwerId(Long postId) {
		try {
			return repository.getBoardOnwerId(postId);

		} catch (Exception e) {
			log.error("❌ Failed to get board ownerId : postId={}, error={}", postId, e.getMessage(), e);
			return "";
		}
	}

	// ==================== 좋아요 기능 ====================

	/**
	 * 게시글 좋아요 토글 (추가/취소)
	 *
	 * <p>로그인한 사용자만 좋아요를 누를 수 있습니다.</p>
	 * <p>이미 좋아요를 눌렀으면 취소하고, 안 눌렀으면 추가합니다.</p>
	 *
	 * @param postId 게시글 ID
	 * @param authentication Spring Security 인증 객체 (null 불가)
	 * @return 좋아요 상태 (true: 좋아요 추가됨, false: 좋아요 취소됨)
	 * @throws IllegalStateException 비로그인 사용자인 경우
	 */
	/**
	 * 게시글 좋아요 토글 (추가/취소) - ACID 속성 적용 고도화
	 *
	 * <p><strong>ACID 트랜잭션 속성 보장:</strong></p>
	 * <ul>
	 *   <li><strong>Atomicity (원자성)</strong>: 좋아요 추가/삭제가 완전히 성공하거나 실패 (All or Nothing)</li>
	 *   <li><strong>Consistency (일관성)</strong>: UNIQUE 제약으로 중복 좋아요 방지, 데이터 무결성 유지</li>
	 *   <li><strong>Isolation (격리성)</strong>: READ_COMMITTED 수준으로 동시성 제어, 더티 리드 방지</li>
	 *   <li><strong>Durability (지속성)</strong>: 커밋 후 좋아요 데이터 영구 보존</li>
	 * </ul>
	 *
	 * <p><strong>동시성 시나리오:</strong></p>
	 * <pre>
	 * 시나리오 1: 동일 사용자가 동시에 2번 좋아요 클릭
	 *   → UNIQUE 제약으로 1개만 저장됨 (Consistency 보장)
	 *
	 * 시나리오 2: 여러 사용자가 동시에 좋아요
	 *   → READ_COMMITTED로 각각 독립적으로 처리 (Isolation 보장)
	 *
	 * 시나리오 3: 좋아요 추가 중 서버 장애
	 *   → 롤백으로 이전 상태 복구 (Atomicity 보장)
	 * </pre>
	 *
	 * @param postId 게시글 ID
	 * @param authentication Spring Security 인증 객체 (null 불가)
	 * @return 좋아요 상태 (true: 좋아요 추가됨, false: 좋아요 취소됨)
	 * @throws IllegalStateException 비로그인 사용자인 경우
	 * @throws IllegalArgumentException 존재하지 않는 게시글 ID인 경우
	 */
	@Transactional(
		isolation = Isolation.READ_COMMITTED,
		rollbackFor = Exception.class
	)
	public boolean toggleLike(Long postId, Authentication authentication) {
		// === 1. 입력 검증 (Consistency - 일관성) ===
		if (authentication == null) {
			log.warn("Unauthorized like attempt: postId={}, authentication=null", postId);
			throw new IllegalStateException("로그인한 사용자만 좋아요를 누를 수 있습니다.");
		}

		String username = authentication.getName();

		if (username == null || username.trim().isEmpty()) {
			log.error("Invalid username: postId={}, username={}", postId, username);
			throw new IllegalStateException("유효하지 않은 사용자 정보입니다.");
		}

		// === 2. 게시글 존재 확인 (Consistency) ===
		CounselPost post = repository.findById(postId)
			.orElseThrow(() -> {
				log.error("Post not found: postId={}", postId);
				return new IllegalArgumentException("존재하지 않는 게시글입니다. (ID: " + postId + ")");
			});

		try {
			// === 3. 좋아요 중복 확인 (Isolation - READ_COMMITTED) ===
			Optional<CounselPostLike> existingLike = likeRepository.findByPostIdAndUsername(postId, username);

			if (existingLike.isPresent()) {
				// === 4-1. 좋아요 취소 (Atomicity - 완전히 삭제되거나 실패) ===
				CounselPostLike like = existingLike.get();
				likeRepository.delete(like);
				likeRepository.flush(); // 즉시 DB 반영 (Durability 보장)

				log.info("✅ [ACID-Atomicity] Like removed successfully: postId={}, username={}, likeId={}",
					postId, username, like.getId());

				return false; // 좋아요 취소됨

			} else {
				// === 4-2. 좋아요 추가 (Atomicity - 완전히 저장되거나 실패) ===
				CounselPostLike newLike = new CounselPostLike(post, username);

				CounselPostLike savedLike = likeRepository.save(newLike);
				likeRepository.flush(); // 즉시 DB 반영 (Durability 보장)

				log.info("✅ [ACID-Atomicity] Like added successfully: postId={}, username={}, likeId={}",
					postId, username, savedLike.getId());

				return true; // 좋아요 추가됨
			}

		} catch (DataIntegrityViolationException e) {
			// === 5. 동시성 제어 - UNIQUE 제약 위반 (Consistency) ===
			log.warn("⚠️ [ACID-Consistency] Duplicate like attempt prevented: postId={}, username={}, error={}",
				postId, username, e.getMessage());

			// 이미 좋아요가 존재하므로 좋아요 상태 반환 (중복 방지)
			boolean alreadyLiked = likeRepository.existsByPostIdAndUsername(postId, username);
			return alreadyLiked;

		} catch (Exception e) {
			// === 6. 예외 처리 - 롤백 보장 (Atomicity) ===
			log.error("❌ [ACID-Atomicity] Like toggle failed - Rolling back: postId={}, username={}, error={}",
				postId, username, e.getMessage(), e);

			// 예외를 다시 던져서 트랜잭션 롤백 발생
			throw new RuntimeException("좋아요 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	/**
	 * 특정 게시글의 좋아요 개수 조회 - ACID 속성 적용 (읽기 전용)
	 *
	 * <p><strong>ACID 트랜잭션 속성:</strong></p>
	 * <ul>
	 *   <li><strong>Consistency (일관성)</strong>: 커밋된 데이터만 조회 (더티 리드 방지)</li>
	 *   <li><strong>Isolation (격리성)</strong>: READ_COMMITTED 수준으로 최신 커밋 데이터 조회</li>
	 *   <li><strong>Performance</strong>: readOnly=true로 읽기 최적화 (쓰기 락 불필요)</li>
	 * </ul>
	 *
	 * <p><strong>동시성 시나리오:</strong></p>
	 * <pre>
	 * 시나리오 1: 좋아요 추가 중에 개수 조회
	 *   → READ_COMMITTED로 커밋된 개수만 반환 (일관성 보장)
	 *
	 * 시나리오 2: 여러 사용자가 동시에 개수 조회
	 *   → readOnly=true로 성능 최적화, 락 경합 없음
	 * </pre>
	 *
	 * @param postId 게시글 ID
	 * @return 좋아요 개수 (0 이상의 정수)
	 */
	@Transactional(
		readOnly = true,
		isolation = Isolation.READ_COMMITTED
	)
	public long getLikeCount(Long postId) {
		try {
			long count = likeRepository.countByPostId(postId);
			log.debug("✅ [ACID-Consistency] Like count retrieved: postId={}, count={}", postId, count);
			return count;

		} catch (Exception e) {
			log.error("❌ [ACID-Error] Failed to get like count: postId={}, error={}",
				postId, e.getMessage(), e);

			// 조회 실패 시 0 반환 (기본값)
			return 0L;
		}
	}

	/**
	 * 특정 게시글에 특정 사용자가 좋아요를 눌렀는지 확인 - ACID 속성 적용
	 *
	 * <p><strong>ACID 트랜잭션 속성:</strong></p>
	 * <ul>
	 *   <li><strong>Consistency (일관성)</strong>: 커밋된 좋아요 상태만 반환</li>
	 *   <li><strong>Isolation (격리성)</strong>: READ_COMMITTED 수준으로 최신 상태 확인</li>
	 *   <li><strong>Performance</strong>: readOnly=true로 읽기 최적화</li>
	 * </ul>
	 *
	 * <p><strong>사용 목적:</strong></p>
	 * <pre>
	 * - UI에서 하트 아이콘 색상 결정 (빨강 vs 회색)
	 * - 사용자별 좋아요 상태 표시
	 * - 비로그인 사용자는 항상 false 반환
	 * </pre>
	 *
	 * @param postId 게시글 ID
	 * @param authentication Spring Security 인증 객체 (null 가능)
	 * @return 좋아요 여부 (true: 좋아요 누름, false: 안 누름)
	 */
	@Transactional(
		readOnly = true,
		isolation = Isolation.READ_COMMITTED
	)
	public boolean isLikedByUser(Long postId, Authentication authentication) {
		// 비로그인 사용자는 좋아요 불가능
		if (authentication == null) {
			return false;
		}

		String username = authentication.getName();

		// username이 null이거나 빈 문자열인 경우 false 반환
		if (username == null || username.trim().isEmpty()) {
			log.warn("⚠️ Invalid username for like check: postId={}", postId);
			return false;
		}

		try {
			boolean isLiked = likeRepository.existsByPostIdAndUsername(postId, username);
			log.debug("✅ [ACID-Consistency] Like status checked: postId={}, username={}, isLiked={}",
				postId, username, isLiked);
			return isLiked;

		} catch (Exception e) {
			log.error("❌ [ACID-Error] Failed to check like status: postId={}, username={}, error={}",
				postId, username, e.getMessage(), e);

			// 조회 실패 시 false 반환 (안전한 기본값)
			return false;
		}
	}

	/**
	 * 특정 게시글에 좋아요를 누른 사용자 목록 조회 (좋아요 패널용)
	 *
	 * <p><strong>기능:</strong></p>
	 * <ul>
	 *   <li>좋아요를 누른 사용자의 username 목록 반환</li>
	 *   <li>생성일시(좋아요 누른 순서) 기준 오름차순 정렬</li>
	 *   <li>UI에서 User 정보(닉네임, 프로필 이미지)와 조인하여 표시</li>
	 * </ul>
	 *
	 * @param postId 게시글 ID
	 * @return 좋아요 누른 사용자의 username 리스트
	 */
	@Transactional(
		readOnly = true,
		isolation = Isolation.READ_COMMITTED
	)
	public List<String> getLikedUsernames(Long postId) {
		try {
			List<CounselPostLike> likes = likeRepository.findAllByPostIdOrderByCreatedAtAsc(postId);

			List<String> usernames = likes.stream()
				.map(CounselPostLike::getUsername)
				.collect(Collectors.toList());

			log.debug("✅ [Counsel] Liked usernames retrieved: postId={}, count={}", postId, usernames.size());
			return usernames;

		} catch (Exception e) {
			log.error("❌ [Counsel] Failed to get liked usernames: postId={}, error={}",
				postId, e.getMessage(), e);
			return Collections.emptyList();
		}
	}
}

