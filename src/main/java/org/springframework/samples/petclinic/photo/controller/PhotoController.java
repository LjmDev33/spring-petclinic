package org.springframework.samples.petclinic.photo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.samples.petclinic.common.dto.PageResponse;
import org.springframework.samples.petclinic.photo.dto.PhotoPostDto;
import org.springframework.samples.petclinic.photo.service.PhotoService;
import org.springframework.samples.petclinic.user.repository.UserRepository;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Project : spring-petclinic
 * File    : PhotoController.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 컨트롤러
 */
@Controller
@RequestMapping("/photo")
public class PhotoController {

	private static final Logger log = LoggerFactory.getLogger(PhotoController.class);

	private final PhotoService photoService;
	private final UserRepository userRepository;

	public PhotoController(PhotoService photoService, UserRepository userRepository) {
		this.photoService = photoService;
		this.userRepository = userRepository;
	}

	/**
	 * 포토게시판 목록 (갤러리형)
	 */
	@GetMapping("/list")
	public String list(@PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
					   Model model) {
		log.info("포토게시판 목록 조회");

		PageResponse<PhotoPostDto> pageResponse = photoService.getPagedPosts(pageable);

		model.addAttribute("page", pageResponse);
		model.addAttribute("posts", pageResponse.getContent());
		model.addAttribute("template", "photo/photoList");

		return "fragments/layout";
	}

	/**
	 * 포토게시글 상세
	 */
	@GetMapping("/detail/{id}")
	public String detail(@PathVariable("id") Long id, Model model) {
		log.info("포토게시글 상세 조회: ID={}", id);

		PhotoPostDto post = photoService.getPost(id);

		// 좋아요 정보 추가
		long likeCount = photoService.getLikeCount(id);
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		boolean isLiked = photoService.isLikedByUser(id, authentication);

		// [추가] 좋아요 누른 사용자 정보 조회 (username → User 객체)
		// 전제: PhotoService에 getLikedUsernames(id) 메소드가 구현되어 있다고 가정
		List<String> likedUsernames = photoService.getLikedUsernames(id);
		List<User> likedUsers = likedUsernames.isEmpty() ? Collections.emptyList() : userRepository.findByUsernameIn(likedUsernames);

		// 작성자 및 관리자 검증 추가
		List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
		String userName = authentication.getName();
		String ownerUserId = photoService.getBoardOnwerId(id);
		boolean ownerYN = userName.equals(ownerUserId) || roles.contains("ROLE_ADMIN");

		model.addAttribute("post", post);
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("isLiked", isLiked);
		model.addAttribute("likedUsers", likedUsers);
		model.addAttribute("template", "photo/photoDetail");
		model.addAttribute("ownerYN", ownerYN);

		return "fragments/layout";
	}

	/**
	 * 글쓰기 화면
	 */
	@GetMapping("/write")
	public String writeForm(Model model) {
		log.info("포토게시글 작성 화면");

		model.addAttribute("template", "photo/photoWrite");

		return "fragments/layout";
	}

	/**
	 * 글쓰기 저장
	 */
	@PostMapping("/write")
	public String write(@ModelAttribute PhotoPostDto postDto) {
		log.info("포토게시글 작성: 제목={}", postDto.getTitle());

		photoService.createPost(postDto);

		return "redirect:/photo/list";
	}

	/**
	 * 수정 화면
	 */
	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable("id") Long id, Model model) {
		log.info("포토게시글 수정 화면: ID={}", id);

		PhotoPostDto post = photoService.getPost(id);

		model.addAttribute("post", post);
		model.addAttribute("template", "photo/photoEdit");

		return "fragments/layout";
	}

	/**
	 * 수정 저장
	 */
	@PostMapping("/edit/{id}")
	public String edit(@PathVariable("id") Long id, @ModelAttribute PhotoPostDto postDto) {
		log.info("포토게시글 수정: ID={}", id);

		photoService.updatePost(id, postDto);

		return "redirect:/photo/detail/" + id;
	}

	/**
	 * 게시글 삭제
	 */
	@PostMapping("/delete/{id}")
	public String delete(@PathVariable("id") Long id) {
		log.info("포토게시글 삭제: ID={}", id);

		photoService.deletePost(id);

		return "redirect:/photo/list";
	}

	/**
	 * Phase 3: 임시 파일 업로드 (Uppy용)
	 */
	@PostMapping("/upload-temp")
	@ResponseBody
	public ResponseEntity<?> uploadTemp(@RequestParam("files") MultipartFile[] files) {
		try {
			List<Map<String, String>> uploadedFiles = new java.util.ArrayList<>();
			Path tempDir = Paths.get("uploads/temp");
			if (!Files.exists(tempDir)) {
				Files.createDirectories(tempDir);
			}

			for (MultipartFile file : files) {
				String originalFilename = file.getOriginalFilename();
				String extension = originalFilename != null && originalFilename.contains(".")
					? originalFilename.substring(originalFilename.lastIndexOf("."))
					: "";
				String storedFilename = java.util.UUID.randomUUID().toString() + extension;
				Path filePath = tempDir.resolve(storedFilename);
				file.transferTo(filePath.toFile());

				Map<String, String> fileInfo = new HashMap<>();
				fileInfo.put("path", "uploads/temp/" + storedFilename);
				fileInfo.put("originalName", originalFilename);
				uploadedFiles.add(fileInfo);

				log.info("✅ Photo temp file uploaded: {}", storedFilename);
			}

			return ResponseEntity.ok(uploadedFiles);
		} catch (Exception e) {
			log.error("❌ Photo temp upload failed: {}", e.getMessage(), e);
			return ResponseEntity.badRequest()
				.body(java.util.Map.of("error", "파일 업로드 중 오류가 발생했습니다: " + e.getMessage()));
		}
	}

	/**
	 * 게시글 좋아요 토글 (AJAX API)
	 */
	@PostMapping("/detail/{id}/like")
	@ResponseBody
	public ResponseEntity<java.util.Map<String, Object>> toggleLike(
		@PathVariable Long id, Authentication authentication) {

		java.util.Map<String, Object> response = new java.util.HashMap<>();

		try {
			// 좋아요 토글
			boolean liked = photoService.toggleLike(id, authentication);

			// 좋아요 개수 조회
			long likeCount = photoService.getLikeCount(id);

			List<String> likedUsernames = photoService.getLikedUsernames(id);
			List<User> users = userRepository.findByUsernameIn(likedUsernames);

			// User 엔티티 -> 화면용 간소화된 데이터(Map)로 변환 (보안 및 전송 최적화)
			List<Map<String, Object>> userListDto = users.stream().map(u -> {
				Map<String, Object> map = new HashMap<>();
				map.put("nickname", u.getNickname());
				// User 클래스에 해당 메소드들이 있다고 가정 (없으면 getter 사용)
				map.put("hasProfileImage", u.hasProfileImage());
				map.put("profileImageUrl", u.getProfileImageUrl());
				map.put("avatarColor", u.getAvatarColor());
				map.put("initial", u.getInitial());
				return map;
			}).collect(Collectors.toList());

			response.put("success", true);
			response.put("liked", liked);
			response.put("likeCount", likeCount);
			response.put("likedUsers", userListDto);
			response.put("message", liked ? "좋아요를 눌렀습니다." : "좋아요를 취소했습니다.");

			log.info("Photo Like toggled: postId={}, username={}, liked={}",
				id, authentication != null ? authentication.getName() : "anonymous", liked);

			return ResponseEntity.ok(response);
		} catch (IllegalStateException e) {
			// 비로그인 사용자
			log.warn("Unauthorized photo like attempt: postId={}", id);
			response.put("success", false);
			response.put("error", e.getMessage());
			return ResponseEntity.status(401).body(response);
		} catch (Exception e) {
			log.error("Error toggling photo like: postId={}, error={}", id, e.getMessage(), e);
			response.put("success", false);
			response.put("error", "좋아요 처리 중 오류가 발생했습니다.");
			return ResponseEntity.badRequest().body(response);
		}
	}
}
