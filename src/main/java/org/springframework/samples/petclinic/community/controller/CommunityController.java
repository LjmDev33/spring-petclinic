package org.springframework.samples.petclinic.community.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.community.dto.CommunityPostDto;
import org.springframework.samples.petclinic.community.service.CommunityService;
import org.springframework.samples.petclinic.user.repository.UserRepository;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/*
 * Project : spring-petclinic
 * File    : CommunityController.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 커뮤니티 요청 컨트롤러
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Controller
@RequestMapping("/community")
public class CommunityController {

	private static final Logger log = LoggerFactory.getLogger(CommunityController.class);

	private final CommunityService communityService;
	private final UserRepository userRepository;

	public CommunityController(CommunityService communityService, UserRepository userRepository) {
		this.communityService = communityService;
		this.userRepository = userRepository;
	}

	@GetMapping("/list")
	public String list(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
					   @RequestParam(value = "type", required = false) String type,
					   @RequestParam(value = "keyword", required = false) String keyword,
					   @RequestParam(value = "subject", required = true) String subject,
					   Model model) {

		PageResponse<CommunityPostDto> pageResponse;

		type = (type == null || type.isBlank()) ? "" : type;

		if(keyword != null && !keyword.isBlank()) {
			pageResponse = communityService.search(type,keyword,pageable);
		}else{
			pageResponse = communityService.getPagedPosts(pageable);
		}

		model.addAttribute("page", pageResponse);
		model.addAttribute("posts", pageResponse.getContent());
		model.addAttribute("keyword", keyword);
		model.addAttribute("type", type);
		model.addAttribute("subject", subject);
		if(subject.equalsIgnoreCase("notice")) {
			model.addAttribute("template", "community/noticeList");
		}

		return "fragments/layout";
	}

	// 공지사항 게시글 상세
	@GetMapping("detail/{id}")
	public String detail(@PathVariable("id") Long id,
						 @RequestParam(value = "subject", required = true) String subject,
						 Model model) {
		log.info("### detail called");

		model.addAttribute("post", communityService.getPost(id));
		model.addAttribute("prevPost", communityService.getPrevPost(id).orElse(null));
		model.addAttribute("nextPost", communityService.getNextPost(id).orElse(null));
		model.addAttribute("subject", subject);

		// 좋아요 정보 추가
		long likeCount = communityService.getLikeCount(id);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isLiked = communityService.isLikedByUser(id, authentication);
		List<String> likedUsernames = communityService.getLikedUsernames(id);

		// 좋아요 누른 사용자 정보 조회 (username → User 객체)
		List<User> likedUsers = likedUsernames.isEmpty() ? Collections.emptyList() : userRepository.findByUsernameIn(likedUsernames);

		model.addAttribute("likeCount", likeCount);
		model.addAttribute("isLiked", isLiked);
		model.addAttribute("likedUsers", likedUsers);

		if (subject.equalsIgnoreCase("notice")) {
			model.addAttribute("template", "community/noticeDetail");
		}

		return "fragments/layout";
	}

	/**
	 * 글쓰기 화면 표시
	 * - 관리자만 접근 가능
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/write")
	public String writeForm(@RequestParam(value = "subject", required = false, defaultValue = "notice") String subject,
							Model model) {
		log.info("### write form called: subject={}", subject);

		model.addAttribute("subject", subject);
		if (subject.equalsIgnoreCase("notice")) {
			model.addAttribute("template", "community/noticeWrite");
		}

		return "fragments/layout";
	}

	/**
	 * 글쓰기 저장 처리
	 * - 관리자만 접근 가능
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/write")
	public String create(@ModelAttribute CommunityPostDto postDto,
						 @RequestParam(value = "subject", required = false, defaultValue = "notice") String subject) {
		log.info("### create called: subject={}, title={}", subject, postDto.getTitle());

		communityService.createPost(postDto);

		return "redirect:/community/list?subject=" + subject;
	}

	/**
	 * 게시글 수정 화면 표시
	 * - 관리자만 접근 가능
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable("id") Long id,
						   @RequestParam(value = "subject", required = false, defaultValue = "notice") String subject,
						   Model model) {
		log.info("### edit form called: id={}, subject={}", id, subject);

		model.addAttribute("post", communityService.getPost(id));
		model.addAttribute("subject", subject);

		if (subject.equalsIgnoreCase("notice")) {
			model.addAttribute("template", "community/noticeEdit");
		}

		return "fragments/layout";
	}

	/**
	 * 게시글 수정 처리
	 * - 관리자만 접근 가능
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/edit/{id}")
	public String update(@PathVariable("id") Long id,
						 @ModelAttribute CommunityPostDto postDto,
						 @RequestParam(value = "subject", required = false, defaultValue = "notice") String subject) {
		log.info("### update called: id={}, subject={}, title={}", id, subject, postDto.getTitle());

		communityService.updatePost(id, postDto);

		return "redirect:/community/detail/" + id + "?subject=" + subject;
	}

	/**
	 * 게시글 좋아요 토글 (AJAX) - 사용자 목록 실시간 반환 추가
	 * - 로그인한 사용자만 좋아요를 누를 수 있다.
	 * - 이미 좋아요를 눌렀으면 취소하고, 안 눌렀으면 추가한다.
	 * - JSON 응답으로 좋아요 상태와 개수를 반환한다.
	 */
	@PostMapping("/detail/{id}/like")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> toggleLike(
		@PathVariable Long id, Authentication authentication) {

		Map<String, Object> response = new HashMap<>();

		try {
			// 좋아요 토글
			boolean liked = communityService.toggleLike(id, authentication);

			// 좋아요 개수 조회
			long likeCount = communityService.getLikeCount(id);

			// 3. [추가] 갱신된 좋아요 사용자 목록 조회 및 변환 (PhotoController와 동일 로직)
			List<String> likedUsernames = communityService.getLikedUsernames(id);
			// likedUsernames가 비어있어도 빈 리스트 반환됨
			List<User> users = likedUsernames.isEmpty() ? Collections.emptyList() : userRepository.findByUsernameIn(likedUsernames);

			// User 엔티티 -> 화면용 간소화된 데이터(Map)로 변환
			List<Map<String, Object>> userListDto = users.stream().map(u -> {
				Map<String, Object> map = new HashMap<>();
				map.put("nickname", u.getNickname());
				map.put("hasProfileImage", u.hasProfileImage());
				map.put("profileImageUrl", u.getProfileImageUrl());
				map.put("avatarColor", u.getAvatarColor());
				map.put("initial", u.getInitial());
				return map;
			}).collect(Collectors.toList());

			response.put("success", true);
			response.put("liked", liked);
			response.put("likeCount", likeCount);
			response.put("likedUsers", userListDto); // [핵심] 갱신된 목록 전달
			response.put("message", liked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");

			log.info("Like toggled: postId={}, username={}, liked={}",
				id, authentication != null ? authentication.getName() : "anonymous", liked);

			return ResponseEntity.ok(response);
		} catch (IllegalStateException e) {
			// 비로그인 사용자
			log.warn("Unauthorized like attempt: postId={}", id);
			response.put("success", false);
			response.put("error", e.getMessage());
			return ResponseEntity.status(401).body(response);
		} catch (Exception e) {
			log.error("Error toggling like: postId={}, error={}", id, e.getMessage(), e);
			response.put("success", false);
			response.put("error", "좋아요 처리 중 오류가 발생했습니다.");
			return ResponseEntity.badRequest().body(response);
		}
	}

	/**
	 * 임시 파일 업로드 (Uppy용)
	 * - Phase 3: 게시글 수정 시 첨부파일 업로드
	 * - 관리자만 접근 가능
	 *
	 * <p><strong>처리 흐름:</strong></p>
	 * <ol>
	 *   <li>Uppy에서 파일 전송</li>
	 *   <li>임시 디렉토리에 저장</li>
	 *   <li>저장 경로 반환 (JSON)</li>
	 *   <li>게시글 저장 시 경로를 받아 DB 저장</li>
	 * </ol>
	 */
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	@PostMapping("/upload-temp")
	@ResponseBody
	public ResponseEntity<?> uploadTemp(
		@RequestParam("files") MultipartFile[] files) {

		log.info("### Community temp upload called: fileCount={}", files != null ? files.length : 0);

		try {
			List<Map<String, String>> uploadedFiles = new ArrayList<>();

			if (files != null) {
				for (MultipartFile file : files) {
					if (file.isEmpty()) continue;

					// 파일명 생성 (UUID + 원본 확장자)
					String originalFilename = file.getOriginalFilename();
					String extension = "";
					if (originalFilename != null && originalFilename.contains(".")) {
						extension = originalFilename.substring(originalFilename.lastIndexOf("."));
					}
					String storedFilename = UUID.randomUUID().toString() + extension;

					// 임시 디렉토리에 저장
					Path uploadPath = Paths.get("uploads/temp");
					Files.createDirectories(uploadPath);

					Path filePath = uploadPath.resolve(storedFilename);
					file.transferTo(filePath.toFile());

					// 결과 추가
					Map<String, String> fileInfo = new HashMap<>();
					fileInfo.put("originalFilename", originalFilename);
					fileInfo.put("storedFilename", storedFilename);
					fileInfo.put("path", "uploads/temp/" + storedFilename);
					uploadedFiles.add(fileInfo);

					log.info("✅ Community temp file uploaded: original={}, stored={}", originalFilename, storedFilename);
				}
			}

			return ResponseEntity.ok(uploadedFiles);

		} catch (Exception e) {
			log.error("❌ Community temp upload failed: {}", e.getMessage(), e);
			return ResponseEntity.badRequest()
				.body(Map.of("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage()));
		}
	}
}
