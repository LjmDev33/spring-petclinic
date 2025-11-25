package org.springframework.samples.petclinic.counsel.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.counsel.dto.CounselPostDto;
import org.springframework.samples.petclinic.counsel.dto.CounselPostWriteDto;
import org.springframework.samples.petclinic.counsel.mapper.CounselPostMapper;
import org.springframework.samples.petclinic.counsel.repository.CounselPostRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.samples.petclinic.counsel.dto.CounselCommentDto;
import org.springframework.samples.petclinic.counsel.mapper.CounselCommentMapper;
import org.springframework.samples.petclinic.counsel.repository.CounselCommentRepository;
import org.springframework.samples.petclinic.counsel.table.CounselComment;
import org.springframework.samples.petclinic.counsel.model.Attachment;
import org.springframework.samples.petclinic.counsel.table.CounselPostAttachment;
import org.springframework.samples.petclinic.counsel.repository.AttachmentRepository;
import org.springframework.samples.petclinic.counsel.repository.CounselPostAttachmentRepository;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Project : spring-petclinic
 * File    : CounselService.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 온라인상담 게시판 비즈니스 로직 집약(Service 계층)
 *   - 규칙: Entity 직접 노출 금지 → DTO 매핑 후 반환
 *   - QueryDSL 검색은 RepositoryImpl에서 수행, Service는 흐름/검증/트랜잭션 담당
 *   미구현(후속): 비공개글 비밀번호 검증 API, 댓글 CRUD/트리, 파일 첨부 업로드/서빙
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

	public CounselService(CounselPostRepository repository, CounselContentStorage contentStorage,
						  CounselCommentRepository commentRepository, CounselPostMapper postMapper,
						  FileStorageService fileStorageService, AttachmentRepository attachmentRepository,
						  CounselPostAttachmentRepository postAttachmentRepository) {
		this.repository = repository;
		this.contentStorage = contentStorage;
		this.commentRepository = commentRepository;
		this.postMapper = postMapper;
		this.fileStorageService = fileStorageService;
		this.attachmentRepository = attachmentRepository;
		this.postAttachmentRepository = postAttachmentRepository;
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
	 * 상세 조회 시 contentPath가 존재하면 파일에서 본문을 로드하여 DTO.content에 채웁니다.
	 */
	public CounselPostDto getDetail(Long id) throws IOException {
		CounselPost entity = repository.findById(id).orElseThrow();
		CounselPostDto dto = postMapper.toDto(entity);
		if (dto.getContentPath() != null && !dto.getContentPath().isBlank()) {
			String html = contentStorage.loadHtml(dto.getContentPath());
			dto.setContent(html);
		}
		return dto;
	}

	/**
	 * 신규 글 저장: 본문 파일 저장 → 경로 세팅 → DTO 비밀번호는 BCrypt로 해싱하여 엔티티에 저장합니다.
	 * @return 생성된 게시글 ID
	 */
	public Long saveNew(CounselPostWriteDto dto) {
		// 1. 본문 저장
		String path = "";
		try {
			path = contentStorage.saveHtml(dto.getContent());
		} catch (IOException e) {
			log.error("Failed to save content HTML for new post: {}", dto.getTitle(), e);
			throw new RuntimeException("Error saving post content.", e);
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
					// Attachment 엔티티 생성 및 저장
					Attachment attachment = new Attachment();
					attachment.setFilePath(filePath);
					attachment.setOriginalFileName(extractFileName(filePath)); // 경로에서 파일명 추출
					attachment.setFileSize(0L); // 임시 업로드 시 크기 정보 없음 (추후 개선 가능)
					attachment.setMimeType("application/octet-stream"); // 임시 업로드 시 MIME 타입 정보 없음
					attachment.setCreatedAt(LocalDateTime.now());
					attachmentRepository.save(attachment);

					// CounselPost와 Attachment 연결
					org.springframework.samples.petclinic.counsel.table.CounselPostAttachment postAttachment =
						new org.springframework.samples.petclinic.counsel.table.CounselPostAttachment();
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
					String storedFilePath = fileStorageService.storeFile(file);

					// Attachment 엔티티 생성 및 저장
					Attachment attachment = new Attachment();
					attachment.setFilePath(storedFilePath);
					attachment.setOriginalFileName(file.getOriginalFilename());
					attachment.setFileSize(file.getSize());
					attachment.setMimeType(file.getContentType());
					attachment.setCreatedAt(LocalDateTime.now());
					attachmentRepository.save(attachment);

					// CounselPost와 Attachment 연결
					org.springframework.samples.petclinic.counsel.table.CounselPostAttachment postAttachment =
						new org.springframework.samples.petclinic.counsel.table.CounselPostAttachment();
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
	 * 게시글을 수정합니다. 비밀번호 검증 후 진행합니다.
	 * @param postId 게시글 ID
	 * @param dto 수정할 내용
	 * @param password 비밀번호 (비공개글인 경우)
	 * @return 수정 성공 여부
	 */
	public boolean updatePost(Long postId, CounselPostWriteDto dto, String password) {
		try {
			CounselPost entity = repository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));

			// 비밀번호 검증 (비공개글인 경우)
			if (entity.isSecret() && !verifyPassword(postId, password)) {
				log.warn("Failed password verification for updating post ID: {}", postId);
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
						Integer attachmentId = Integer.parseInt(idStr);

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
							attachmentId, attachment.getOriginalFileName());
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
						// Attachment 엔티티 생성 및 저장
						Attachment attachment = new Attachment();
						attachment.setFilePath(filePath);
						attachment.setOriginalFileName(extractFileName(filePath));
						attachment.setFileSize(0L); // 임시 업로드 시 크기 정보 없음
						attachment.setMimeType("application/octet-stream");
						attachment.setCreatedAt(LocalDateTime.now());
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
	 * 게시글을 삭제합니다 (Soft Delete). 비밀번호 검증 후 진행합니다.
	 * @param postId 게시글 ID
	 * @param password 비밀번호 (비공개글인 경우)
	 * @return 삭제 성공 여부
	 */
	public boolean deletePost(Long postId, String password) {
		try {
			CounselPost entity = repository.findById(postId)
				.orElseThrow(() -> new IllegalArgumentException("Invalid post ID: " + postId));

			// 비밀번호 검증 (비공개글인 경우)
			if (entity.isSecret() && !verifyPassword(postId, password)) {
				log.warn("Failed password verification for deleting post ID: {}", postId);
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
			String filePath = fileStorageService.storeFile(file);

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
}
