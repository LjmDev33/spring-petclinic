package org.springframework.samples.petclinic.photo.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.photo.repository.PhotoPostLikeRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPostLike;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.photo.dto.PhotoPostDto;
import org.springframework.samples.petclinic.photo.mapper.PhotoPostMapper;
import org.springframework.samples.petclinic.photo.repository.PhotoPostRepository;
import org.springframework.samples.petclinic.photo.repository.PhotoPostAttachmentRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPost;
import org.springframework.samples.petclinic.photo.table.PhotoPostAttachment;
import org.springframework.samples.petclinic.common.table.Attachment;
import org.springframework.samples.petclinic.counsel.repository.AttachmentRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project : spring-petclinic
 * File    : PhotoService.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 비즈니스 로직 Service
 *
 * Purpose (만든 이유):
 *   1. 사진 중심 게시판의 비즈니스 로직 처리
 *   2. 썸네일 자동 추출 기능 제공 (사용자 편의성)
 *   3. HTML 본문에서 첫 번째 이미지를 썸네일로 사용
 *   4. 조회수 자동 증가 (상세 조회 시)
 *   5. Soft Delete 정책 적용
 *
 * Key Features (주요 기능):
 *   - 게시글 CRUD (생성, 조회, 수정, 삭제 - Soft Delete)
 *   - 썸네일 자동 추출 (HTML에서 첫 번째 <img> 태그 파싱)
 *   - 조회수 자동 증가 (getPost 호출 시)
 *   - 페이징 처리 (PageResponse 반환)
 *   - Entity → DTO 자동 변환 (PhotoPostMapper)
 *
 * Business Rules (비즈니스 규칙):
 *   - 썸네일이 없으면 본문에서 첫 번째 이미지 자동 추출
 *   - 게시글 삭제는 Soft Delete (@SQLDelete)
 *   - 상세 조회 시 조회수 자동 +1
 *   - Entity는 절대 직접 노출하지 않음 (DTO 변환 필수)
 *
 * Thumbnail Extraction (썸네일 추출 로직):
 *   1. HTML에서 첫 번째 <img> 태그 찾기
 *   2. src 속성에서 이미지 URL 추출
 *   3. 추출 실패 시 null 반환 (사용자가 수동 설정 가능)
 *   4. 단순 문자열 파싱 방식 (Jsoup 미사용)
 *
 * Usage Examples (사용 예시):
 *   // 포토게시글 목록 조회
 *   PageResponse<PhotoPostDto> page = photoService.getPagedPosts(pageable);
 *
 *   // 포토게시글 상세 조회 (조회수 자동 +1)
 *   PhotoPostDto post = photoService.getPost(id);
 *
 *   // 포토게시글 작성 (썸네일 자동 추출)
 *   PhotoPostDto created = photoService.createPost(dto);
 *
 *   // 포토게시글 수정
 *   PhotoPostDto updated = photoService.updatePost(id, dto);
 *
 *   // 포토게시글 삭제 (Soft Delete)
 *   photoService.deletePost(id);
 *
 * Performance (성능):
 *   - 썸네일 추출은 단순 문자열 파싱으로 빠른 처리
 *   - 조회수 증가는 별도 쿼리로 처리 (트랜잭션 내)
 *
 * Transaction Management (트랜잭션 관리):
 *   - @Transactional 클래스 레벨 적용
 *   - 조회수 증가도 트랜잭션 내에서 처리
 *
 * Dependencies (의존 관계):
 *   - PhotoPostRepository: DB 접근
 *   - PhotoPostMapper: Entity ↔ DTO 변환
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class PhotoService {

	private static final Logger log = LoggerFactory.getLogger(PhotoService.class);

	private final PhotoPostRepository repository;
	private final PhotoPostLikeRepository likeRepository;
	private final AttachmentRepository attachmentRepository;
	private final PhotoPostAttachmentRepository photoPostAttachmentRepository;

	public PhotoService(PhotoPostRepository repository,
						PhotoPostLikeRepository likeRepository,
						AttachmentRepository attachmentRepository,
						PhotoPostAttachmentRepository photoPostAttachmentRepository) {
		this.repository = repository;
		this.likeRepository = likeRepository;
		this.attachmentRepository = attachmentRepository;
		this.photoPostAttachmentRepository = photoPostAttachmentRepository;
	}

	/**
	 * 페이징된 게시글 목록 조회
	 */
	public PageResponse<PhotoPostDto> getPagedPosts(Pageable pageable) {
		Page<PhotoPost> entityPage = repository.findAll(pageable);
		List<PhotoPostDto> dtoList = entityPage.getContent()
			.stream()
			.map(PhotoPostMapper::toDto)
			.collect(Collectors.toList());
		Page<PhotoPostDto> dtoPage = new PageImpl<>(dtoList, pageable, entityPage.getTotalElements());
		return new PageResponse<>(dtoPage);
	}

	/**
	 * 게시글 상세 조회
	 */
	public PhotoPostDto getPost(Long id) {
		PhotoPost entity = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

		// 조회수 증가
		entity.setViewCount(entity.getViewCount() + 1);
		repository.save(entity);

		return PhotoPostMapper.toDto(entity);
	}

	/**
	 * 게시글 작성
	 * - 썸네일이 없으면 content에서 첫 번째 이미지 추출
	 */
	public PhotoPostDto createPost(PhotoPostDto dto) {
		// 썸네일이 없으면 content에서 첫 번째 이미지 추출
		if ((dto.getThumbnailUrl() == null || dto.getThumbnailUrl().isBlank()) && dto.getContent() != null) {
			String extractedThumbnail = extractFirstImageFromHtml(dto.getContent());
			if (extractedThumbnail != null) {
				dto.setThumbnailUrl(extractedThumbnail);
				log.info("썸네일 자동 추출: {}", extractedThumbnail);
			}
		}

		PhotoPost entity = PhotoPostMapper.toEntity(dto);
		PhotoPost saved = repository.save(entity);
		log.info("포토게시글 작성: ID={}, 제목={}", saved.getId(), saved.getTitle());
		return PhotoPostMapper.toDto(saved);
	}

	/**
	 * HTML 내용에서 첫 번째 이미지 URL 추출
	 */
	private String extractFirstImageFromHtml(String html) {
		if (html == null || html.isBlank()) {
			return null;
		}

		// <img src="..." 패턴 찾기
		int imgStart = html.indexOf("<img");
		if (imgStart == -1) {
			return null;
		}

		int srcStart = html.indexOf("src=\"", imgStart);
		if (srcStart == -1) {
			srcStart = html.indexOf("src='", imgStart);
			if (srcStart == -1) {
				return null;
			}
			srcStart += 5; // "src='" 길이
		} else {
			srcStart += 5; // "src=\"" 길이
		}

		int srcEnd = html.indexOf("\"", srcStart);
		if (srcEnd == -1) {
			srcEnd = html.indexOf("'", srcStart);
		}

		if (srcEnd == -1 || srcEnd <= srcStart) {
			return null;
		}

		return html.substring(srcStart, srcEnd);
	}

	/**
	 * 게시글 수정 (Phase 3: 첨부파일 관리 포함)
	 */
	public PhotoPostDto updatePost(Long id, PhotoPostDto dto) {
		try {
			PhotoPost entity = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

			// 1. 기본 필드 수정
			entity.setTitle(dto.getTitle());
			entity.setContent(dto.getContent());
			entity.setThumbnailUrl(dto.getThumbnailUrl());

			// 2. Phase 3: 기존 첨부파일 삭제 처리
			if (dto.getDeletedFileIds() != null && !dto.getDeletedFileIds().isBlank()) {
				String[] deletedIds = dto.getDeletedFileIds().split(",");
				for (String idStr : deletedIds) {
					idStr = idStr.trim();
					if (idStr.isEmpty()) continue;
					try {
						Long attachmentId = Long.parseLong(idStr);
						Attachment attachment = attachmentRepository.findById(attachmentId).orElse(null);
						if (attachment == null) {
							log.warn("Attachment not found: id={}", attachmentId);
							continue;
						}
						entity.getAttachments().removeIf(pa -> pa.getAttachment().getId().equals(attachmentId));
						attachment.setDelFlag(true);
						attachment.setDeletedAt(LocalDateTime.now());
						attachmentRepository.save(attachment);
						log.info("✅ Photo attachment deleted: postId={}, attachmentId={}", id, attachmentId);
					} catch (NumberFormatException e) {
						log.error("Invalid attachment ID: {}", idStr);
					}
				}
			}

			// 3. Phase 3: 새 첨부파일 추가 처리
			if (dto.getAttachmentPaths() != null && !dto.getAttachmentPaths().isBlank()) {
				String[] filePaths = dto.getAttachmentPaths().split(",");
				for (String filePath : filePaths) {
					filePath = filePath.trim();
					if (filePath.isEmpty()) continue;
					try {
						Attachment attachment = new Attachment();
						attachment.setStoredFilename(filePath);
						attachment.setOriginalFilename(extractFileName(filePath));
						attachment.setFileSize(0L);
						attachment.setContentType("application/octet-stream");
						attachmentRepository.save(attachment);
						PhotoPostAttachment postAttachment = new PhotoPostAttachment(entity, attachment);
						photoPostAttachmentRepository.save(postAttachment);
						entity.addAttachment(postAttachment);
						log.info("✅ Photo attachment added: postId={}, path={}", id, filePath);
					} catch (Exception e) {
						log.error("❌ Failed to add attachment {}: {}", filePath, e.getMessage());
					}
				}
			}

			PhotoPost updated = repository.save(entity);
			log.info("✅ 포토게시글 수정 완료: ID={}, 첨부파일 수={}", id, entity.getAttachments().size());
			return PhotoPostMapper.toDto(updated);

		} catch (Exception e) {
			log.error("❌ 포토게시글 수정 실패: ID={}", id, e);
			throw new RuntimeException("게시글 수정 중 오류가 발생했습니다.", e);
		}
	}

	/**
	 * 게시글 삭제 (Soft Delete)
	 */
	public void deletePost(Long id) {
		PhotoPost entity = repository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + id));

		repository.delete(entity); // @SQLDelete로 Soft Delete
		log.info("포토게시글 삭제: ID={}", id);
	}

	/**
	 * 파일 경로에서 파일명 추출
	 */
	private String extractFileName(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return "unknown";
		}
		int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
		return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
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
	public boolean toggleLike(Long postId, Authentication authentication) {
		// === 1. 입력 검증 (Consistency) ===
		if (authentication == null) {
			log.warn("Unauthorized photo like attempt: postId={}, authentication=null", postId);
			throw new IllegalStateException("로그인한 사용자만 좋아요를 누를 수 있습니다.");
		}

		String username = authentication.getName();

		if (username == null || username.trim().isEmpty()) {
			log.error("Invalid username: postId={}, username={}", postId, username);
			throw new IllegalStateException("유효하지 않은 사용자 정보입니다.");
		}

		// === 2. 게시글 존재 확인 (Consistency) ===
		PhotoPost post = repository.findById(postId)
			.orElseThrow(() -> {
				log.error("Photo post not found: postId={}", postId);
				return new IllegalArgumentException("존재하지 않는 포토게시글입니다. (ID: " + postId + ")");
			});

		try {
			// === 3. 좋아요 중복 확인 (Isolation) ===
			java.util.Optional<PhotoPostLike> existingLike = likeRepository.findByPostIdAndUsername(postId, username);

			if (existingLike.isPresent()) {
				// === 4-1. 좋아요 취소 (Atomicity) ===
				PhotoPostLike like = existingLike.get();
				likeRepository.delete(like);
				likeRepository.flush();

				log.info("✅ [ACID-Atomicity] Photo like removed successfully: postId={}, username={}, likeId={}",
					postId, username, like.getId());

				return false;

			} else {
				// === 4-2. 좋아요 추가 (Atomicity) ===
				PhotoPostLike newLike = new PhotoPostLike(post, username);

				PhotoPostLike savedLike = likeRepository.save(newLike);
				likeRepository.flush();

				log.info("✅ [ACID-Atomicity] Photo like added successfully: postId={}, username={}, likeId={}",
					postId, username, savedLike.getId());

				return true;
			}

		} catch (DataIntegrityViolationException e) {
			// === 5. 동시성 제어 - UNIQUE 제약 위반 (Consistency) ===
			log.warn("⚠️ [ACID-Consistency] Duplicate photo like attempt prevented: postId={}, username={}",
				postId, username);

			boolean alreadyLiked = likeRepository.existsByPostIdAndUsername(postId, username);
			return alreadyLiked;

		} catch (Exception e) {
			// === 6. 예외 처리 - 롤백 보장 (Atomicity) ===
			log.error("❌ [ACID-Atomicity] Photo like toggle failed - Rolling back: postId={}, username={}, error={}",
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
			log.debug("✅ [ACID-Consistency] Photo like count retrieved: postId={}, count={}", postId, count);
			return count;

		} catch (Exception e) {
			log.error("❌ [ACID-Error] Failed to get photo like count: postId={}, error={}",
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
	public boolean isLikedByUser(Long postId, Authentication authentication) {
		if (authentication == null) {
			return false;
		}

		String username = authentication.getName();

		if (username == null || username.trim().isEmpty()) {
			log.warn("⚠️ Invalid username for photo like check: postId={}", postId);
			return false;
		}

		try {
			boolean isLiked = likeRepository.existsByPostIdAndUsername(postId, username);
			log.debug("✅ [ACID-Consistency] Photo like status checked: postId={}, username={}, isLiked={}",
				postId, username, isLiked);
			return isLiked;

		} catch (Exception e) {
			log.error("❌ [ACID-Error] Failed to check photo like status: postId={}, username={}, error={}",
				postId, username, e.getMessage(), e);
			return false;
		}
	}
}

