package org.springframework.samples.petclinic.community.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.common.table.Attachment;
import org.springframework.samples.petclinic.counsel.repository.AttachmentRepository;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.community.table.CommunityPostAttachment;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;
import org.springframework.samples.petclinic.community.mapper.CommunityPostMapper;
import org.springframework.samples.petclinic.community.repository.CommunityPostRepository;
import org.springframework.samples.petclinic.community.repository.CommunityPostAttachmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Project : spring-petclinic
 * File    : CommunityService.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   공지사항 게시판 비즈니스 로직 Service
 *
 * Purpose (만든 이유):
 *   1. 공지사항 게시판의 비즈니스 로직을 중앙 집중화
 *   2. Entity를 직접 노출하지 않고 DTO 변환 (보안 및 캡슐화)
 *   3. 관리자만 작성 가능한 공지사항 관리
 *   4. 이전글/다음글 조회 기능 제공
 *   5. QueryDSL 기반 동적 검색 지원
 *
 * Key Features (주요 기능):
 *   - 게시글 CRUD (생성, 조회, 수정, 삭제)
 *   - 페이징 처리 (PageResponse 반환)
 *   - QueryDSL 기반 동적 검색 (제목, 제목+내용)
 *   - 이전글/다음글 조회 (getPrevPost, getNextPost)
 *   - Entity → DTO 자동 변환 (CommunityPostMapper)
 *
 * Business Rules (비즈니스 규칙):
 *   - Entity는 절대 직접 노출하지 않음 (DTO 변환 필수)
 *   - 공지사항은 관리자만 작성 가능 (Controller에서 권한 검증)
 *   - 게시글 생성 시 자동으로 createdAt 설정
 *   - 모든 조회 결과는 DTO로 변환하여 반환
 *
 * vs CounselService:
 *   - CounselService: 비밀번호, 댓글, 첨부파일, 비공개 기능 포함
 *   - CommunityService: 단순 공지사항 (관리자 전용, 댓글 없음)
 *
 * Usage Examples (사용 예시):
 *   // 공지사항 목록 조회 (페이징)
 *   PageResponse<CommunityPostDto> page = communityService.getPagedPosts(pageable);
 *
 *   // 공지사항 상세 조회
 *   CommunityPostDto post = communityService.getPost(id);
 *
 *   // 공지사항 작성 (관리자)
 *   CommunityPostDto created = communityService.createPost(dto);
 *
 *   // 검색 (QueryDSL)
 *   PageResponse<CommunityPostDto> results = communityService.search("title", "공지", pageable);
 *
 *   // 이전글/다음글 조회
 *   Optional<CommunityPostDto> prev = communityService.getPrevPost(id);
 *   Optional<CommunityPostDto> next = communityService.getNextPost(id);
 *
 * Transaction Management (트랜잭션 관리):
 *   - @Transactional 클래스 레벨 적용
 *   - 모든 public 메서드가 트랜잭션 내에서 실행
 *
 * Dependencies (의존 관계):
 *   - CommunityPostRepository: DB 접근 및 QueryDSL 검색
 *   - CommunityPostMapper: Entity ↔ DTO 변환
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class CommunityService {

	private static final Logger log = LoggerFactory.getLogger(CommunityService.class);

	private final CommunityPostRepository repository;
	private final CommunityPostRepository communityPostRepository;
	private final org.springframework.samples.petclinic.community.repository.CommunityPostLikeRepository likeRepository;
	private final AttachmentRepository attachmentRepository;
	private final CommunityPostAttachmentRepository postAttachmentRepository;

	public CommunityService(CommunityPostRepository repository,
							CommunityPostRepository communityPostRepository,
							org.springframework.samples.petclinic.community.repository.CommunityPostLikeRepository likeRepository,
							AttachmentRepository attachmentRepository,
							CommunityPostAttachmentRepository postAttachmentRepository) {
		this.repository = repository;
		this.communityPostRepository = communityPostRepository;
		this.likeRepository = likeRepository;
		this.attachmentRepository = attachmentRepository;
		this.postAttachmentRepository = postAttachmentRepository;
	}

	// 페이지 조회는 DTO로 매핑하여 반환 (규칙: Entity를 직접 노출 금지)
	public PageResponse<CommunityPostDto> getPagedPosts(Pageable pageable) {
		Page<CommunityPost> entityPage = repository.findAll(pageable);
		List<CommunityPostDto> dtoList = entityPage
			.getContent()
			.stream()
			.map(CommunityPostMapper::toDto)
			.collect(Collectors.toList());
		Page<CommunityPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityPage.getTotalElements());
		return new PageResponse<>(dtoPage);
	}

	/* 전체 게시글 리스트 가져오기 */
	public List<CommunityPostDto> getAllPosts() {
		return repository.findAll()
			.stream()
			.map(CommunityPostMapper::toDto)
			.collect(Collectors.toList());
	}

	public CommunityPostDto getPost(Long id) {
		CommunityPost entity = repository.findById(id).orElseThrow();
		return CommunityPostMapper.toDto(entity);
	}

	public CommunityPostDto createPost(CommunityPostDto dto) {
		CommunityPost entity = CommunityPostMapper.toEntity(dto);
		entity.setCreatedAt(LocalDateTime.now());
		CommunityPost saved = repository.save(entity);
		return CommunityPostMapper.toDto(saved);
	}

	/**
	 * 게시글 수정
	 * - Phase 3: 게시글 첨부파일 관리 기능 완전 구현
	 *
	 * <p><strong>구현 기능:</strong></p>
	 * <ul>
	 *   <li>제목/내용 수정</li>
	 *   <li>기존 첨부파일 삭제 (deletedFileIds)</li>
	 *   <li>새 첨부파일 추가 (attachmentPaths)</li>
	 *   <li>첨부파일 플래그 자동 업데이트</li>
	 *   <li>예외 처리 및 로깅</li>
	 * </ul>
	 */
	public CommunityPostDto updatePost(Long id, CommunityPostDto dto) {
		try {
			CommunityPost entity = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

			// 1. 기본 필드 수정
			entity.setTitle(dto.getTitle());
			entity.setContent(dto.getContent());
			entity.setUpdatedAt(LocalDateTime.now());

			// 2. 기존 첨부파일 삭제 처리
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

						// CommunityPostAttachment 중간 테이블 제거
						entity.getAttachments().removeIf(postAttachment ->
							postAttachment.getAttachment().getId().equals(attachmentId));

						// Attachment Soft Delete (del_flag = true)
						attachment.setDelFlag(true);
						attachment.setDeletedAt(LocalDateTime.now());
						attachmentRepository.save(attachment);

						log.info("✅ Community attachment marked for deletion: postId={}, attachmentId={}, fileName={}",
							id, attachmentId, attachment.getOriginalFilename());
					} catch (NumberFormatException e) {
						log.error("Invalid attachment ID format: {}", idStr);
					}
				}
			}

			// 3. 새 첨부파일 추가 처리
			if (dto.getAttachmentPaths() != null && !dto.getAttachmentPaths().isBlank()) {
				String[] filePaths = dto.getAttachmentPaths().split(",");

				for (String filePath : filePaths) {
					filePath = filePath.trim();
					if (filePath.isEmpty()) continue;

					try {
						// Attachment 엔티티 생성 및 저장
						Attachment attachment = new Attachment();
						attachment.setStoredFilename(filePath);
						attachment.setOriginalFilename(extractFileName(filePath));
						attachment.setFileSize(0L); // 임시 업로드 시 크기 정보 없음
						attachment.setContentType("application/octet-stream");
						attachmentRepository.save(attachment);

						// CommunityPost와 Attachment 연결
						CommunityPostAttachment postAttachment = new CommunityPostAttachment();
						postAttachment.setCommunityPost(entity);
						postAttachment.setAttachment(attachment);
						entity.addAttachment(postAttachment);

						log.info("✅ Community attachment added: postId={}, path={}", id, filePath);
					} catch (Exception e) {
						log.error("❌ Failed to add Community attachment {}: {}", filePath, e.getMessage());
						throw new RuntimeException("첨부파일 추가 중 오류가 발생했습니다.", e);
					}
				}
			}

			// 4. 첨부파일 플래그 업데이트
			entity.setAttachFlag(!entity.getAttachments().isEmpty());

			// 5. 저장 및 반환
			CommunityPost updated = repository.save(entity);
			log.info("✅ Community post updated successfully: id={}, title={}, attachmentCount={}",
				id, updated.getTitle(), updated.getAttachments().size());

			return CommunityPostMapper.toDto(updated);

		} catch (Exception e) {
			log.error("❌ Error updating Community post: postId={}, error={}", id, e.getMessage(), e);
			throw new RuntimeException("게시글 수정 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	/**
	 * 파일 경로에서 파일명 추출
	 */
	private String extractFileName(String path) {
		if (path == null || path.isEmpty()) {
			return "unknown";
		}
		int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
		return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
	}

	// 검색 결과도 DTO 페이지로 매핑
	public PageResponse<CommunityPostDto> search(String type, String keyword, Pageable pageable) {
		PageResponse<CommunityPost> entityResponse = communityPostRepository.search(type, keyword, pageable);
		List<CommunityPostDto> dtoList = entityResponse.getContent()
			.stream()
			.map(CommunityPostMapper::toDto)
			.collect(Collectors.toList());
		Page<CommunityPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityResponse.getTotalElements());
		return new PageResponse<>(dtoPage);
	}

	public Optional<CommunityPostDto> getPrevPost(Long id){
		return communityPostRepository.getPrevPost(id).map(CommunityPostMapper::toDto);
	}

	public Optional<CommunityPostDto> getNextPost(Long id){
		return communityPostRepository.getNextPost(id).map(CommunityPostMapper::toDto);
	}

	// ==================== 좋아요 기능 (ACID 트랜잭션 고도화) ====================

	/**
	 * 게시글 좋아요 토글 (추가/취소) - ACID 속성 적용 고도화
	 *
	 * <p><strong>ACID 트랜잭션 속성 보장:</strong></p>
	 * <ul>
	 *   <li><strong>Atomicity (원자성)</strong>: 좋아요 추가/삭제가 완전히 성공하거나 실패</li>
	 *   <li><strong>Consistency (일관성)</strong>: UNIQUE 제약으로 중복 좋아요 방지</li>
	 *   <li><strong>Isolation (격리성)</strong>: READ_COMMITTED 수준으로 동시성 제어</li>
	 *   <li><strong>Durability (지속성)</strong>: 커밋 후 좋아요 데이터 영구 보존</li>
	 * </ul>
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
	public boolean toggleLike(Long postId, org.springframework.security.core.Authentication authentication) {
		// === 1. 입력 검증 (Consistency) ===
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
		CommunityPost post = repository.findById(postId)
			.orElseThrow(() -> {
				log.error("Post not found: postId={}", postId);
				return new IllegalArgumentException("존재하지 않는 게시글입니다. (ID: " + postId + ")");
			});

		try {
			// === 3. 좋아요 중복 확인 (Isolation) ===
			Optional<org.springframework.samples.petclinic.community.table.CommunityPostLike> existingLike =
				likeRepository.findByPostIdAndUsername(postId, username);

			if (existingLike.isPresent()) {
				// === 4-1. 좋아요 취소 (Atomicity) ===
				org.springframework.samples.petclinic.community.table.CommunityPostLike like = existingLike.get();
				likeRepository.delete(like);
				likeRepository.flush();

				log.info("✅ [ACID-Atomicity] Like removed successfully: postId={}, username={}, likeId={}",
					postId, username, like.getId());

				return false;

			} else {
				// === 4-2. 좋아요 추가 (Atomicity) ===
				org.springframework.samples.petclinic.community.table.CommunityPostLike newLike =
					new org.springframework.samples.petclinic.community.table.CommunityPostLike(post, username);

				org.springframework.samples.petclinic.community.table.CommunityPostLike savedLike =
					likeRepository.save(newLike);
				likeRepository.flush();

				log.info("✅ [ACID-Atomicity] Like added successfully: postId={}, username={}, likeId={}",
					postId, username, savedLike.getId());

				return true;
			}

		} catch (org.springframework.dao.DataIntegrityViolationException e) {
			// === 5. 동시성 제어 - UNIQUE 제약 위반 (Consistency) ===
			log.warn("⚠️ [ACID-Consistency] Duplicate like attempt prevented: postId={}, username={}",
				postId, username);

			boolean alreadyLiked = likeRepository.existsByPostIdAndUsername(postId, username);
			return alreadyLiked;

		} catch (Exception e) {
			// === 6. 예외 처리 - 롤백 보장 (Atomicity) ===
			log.error("❌ [ACID-Atomicity] Like toggle failed - Rolling back: postId={}, username={}, error={}",
				postId, username, e.getMessage(), e);

			throw new RuntimeException("좋아요 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
		}
	}

	/**
	 * 특정 게시글의 좋아요 개수 조회 - ACID 속성 적용 (읽기 전용)
	 *
	 * <p><strong>ACID 트랜잭션 속성:</strong></p>
	 * <ul>
	 *   <li><strong>Consistency</strong>: 커밋된 데이터만 조회</li>
	 *   <li><strong>Isolation</strong>: READ_COMMITTED 수준</li>
	 *   <li><strong>Performance</strong>: readOnly=true로 읽기 최적화</li>
	 * </ul>
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
			return 0L;
		}
	}

	/**
	 * 특정 게시글에 특정 사용자가 좋아요를 눌렀는지 확인 - ACID 속성 적용
	 *
	 * <p><strong>ACID 트랜잭션 속성:</strong></p>
	 * <ul>
	 *   <li><strong>Consistency</strong>: 커밋된 좋아요 상태만 반환</li>
	 *   <li><strong>Isolation</strong>: READ_COMMITTED 수준</li>
	 *   <li><strong>Performance</strong>: readOnly=true로 읽기 최적화</li>
	 * </ul>
	 *
	 * @param postId 게시글 ID
	 * @param authentication Spring Security 인증 객체 (null 가능)
	 * @return 좋아요 여부 (true: 좋아요 누름, false: 안 누름)
	 */
	@Transactional(
		readOnly = true,
		isolation = Isolation.READ_COMMITTED
	)
	public boolean isLikedByUser(Long postId, org.springframework.security.core.Authentication authentication) {
		if (authentication == null) {
			return false;
		}

		String username = authentication.getName();

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
			return false;
		}
	}
}
