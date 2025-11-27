package org.springframework.samples.petclinic.common.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.community.repository.CommunityPostRepository;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.counsel.CounselStatus;
import org.springframework.samples.petclinic.photo.repository.PhotoPostRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPost;
import org.springframework.samples.petclinic.faq.repository.FaqPostRepository;
import org.springframework.samples.petclinic.faq.table.FaqPost;
import org.springframework.samples.petclinic.counsel.repository.CounselCommentRepository;
import org.springframework.samples.petclinic.counsel.repository.CounselPostRepository;
import org.springframework.samples.petclinic.counsel.service.CounselContentStorage;
import org.springframework.samples.petclinic.counsel.table.CounselComment;
import org.springframework.samples.petclinic.counsel.table.CounselPost;
import org.springframework.samples.petclinic.system.repository.SystemConfigRepository;
import org.springframework.samples.petclinic.system.table.SystemConfig;
import org.springframework.samples.petclinic.user.repository.UserRepository;
import org.springframework.samples.petclinic.user.table.User;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/*
 * Project : spring-petclinic
 * File    : DataInit.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   프로젝트 모든 게시판 및 유저정보들 초기 데이터 삽입
 *   - 온라인상담: 총 112건 랜덤 생성(공개/비공개, 상태 WAIT/COMPLETE/END)
 *   - COMPLETE 는 댓글 1개 보장, END 는 댓글 유/무 랜덤, WAIT 는 댓글 없음
 *   - 공개글(secret=false)은 passwordHash=null, 비공개(secret=true)는 BCrypt 해시 저장
 *   - 시스템 설정 및 관리자 계정 초기화 추가
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Configuration
public class DataInit {

	private static final String STAFF = "솔 동물의료센터";

	@Bean
	CommandLineRunner initCommunityData(CommunityPostRepository communityPostRepo,
										CounselPostRepository counselPostRepo,
										CounselCommentRepository counselCommentRepo,
										CounselContentStorage contentStorage,
										SystemConfigRepository systemConfigRepo,
										UserRepository userRepo,
										PasswordEncoder passwordEncoder,
										PhotoPostRepository photoPostRepo,
										FaqPostRepository faqPostRepo,
										org.springframework.samples.petclinic.community.repository.CommunityPostLikeRepository communityLikeRepo,
										org.springframework.samples.petclinic.counsel.repository.CounselPostLikeRepository counselLikeRepo,
										org.springframework.samples.petclinic.photo.repository.PhotoPostLikeRepository photoLikeRepo){
		return args -> {
			// 시스템 설정 초기화
			if (systemConfigRepo.count() == 0) {
				initSystemConfig(systemConfigRepo);
			}

			// 관리자 계정 초기화
			if (userRepo.count() == 0) {
				initAdminUser(userRepo, passwordEncoder);
			}

			// 커뮤니티 데이터 초기화
 			if(communityPostRepo.count() == 0){
				initCommunityPosts(communityPostRepo);
			}

			// 커뮤니티 좋아요 초기 데이터
			if(communityLikeRepo.count() == 0 && communityPostRepo.count() > 0){
				initCommunityLikes(communityPostRepo, communityLikeRepo, userRepo);
			}

			// 온라인상담 데이터 초기화
			long postCount = counselPostRepo.count();
			long commentCount = counselCommentRepo.count();
			if(postCount == 0){
				initCounselDataRandom(counselPostRepo, counselCommentRepo, contentStorage);
			} else if (postCount > 0 && commentCount == 0) {
				generateCommentsForExistingPosts(counselPostRepo, counselCommentRepo);
			}

			// 포토게시판 데이터 초기화
			if(photoPostRepo.count() == 0){
				initPhotoData(photoPostRepo);
			}

			// 포토게시판 좋아요 초기 데이터
			if(photoLikeRepo.count() == 0 && photoPostRepo.count() > 0){
				initPhotoLikes(photoPostRepo, photoLikeRepo, userRepo);
			}

			// FAQ 게시판 데이터 초기화
			if(faqPostRepo.count() == 0){
				initFaqData(faqPostRepo);
			}
		};
	}

	/**
	 * 시스템 설정 초기화
	 */
	private void initSystemConfig(SystemConfigRepository repo) {
		List<SystemConfig> configs = new ArrayList<>();

		// 멀티로그인 허용 설정
		SystemConfig multiLogin = new SystemConfig();
		multiLogin.setPropertyKey("multiLoginEnabled");
		multiLogin.setPropertyValue("true");
		multiLogin.setDescription("멀티로그인 허용 여부 (최대 5개 기기). true: 멀티로그인 허용, false: 단일 로그인만 허용");
		multiLogin.setActive(true);
		multiLogin.setUpdatedBy("SYSTEM");
		configs.add(multiLogin);

		// 파일 업로드 허용 설정
		SystemConfig fileUpload = new SystemConfig();
		fileUpload.setPropertyKey("fileUploadEnabled");
		fileUpload.setPropertyValue("true");
		fileUpload.setDescription("파일 업로드 기능 활성화 여부");
		fileUpload.setActive(true);
		fileUpload.setUpdatedBy("SYSTEM");
		configs.add(fileUpload);

		// 최대 파일 크기 설정
		SystemConfig maxFileSize = new SystemConfig();
		maxFileSize.setPropertyKey("maxFileSize");
		maxFileSize.setPropertyValue("5242880");
		maxFileSize.setDescription("최대 파일 크기 (bytes). 기본값: 5MB");
		maxFileSize.setActive(true);
		maxFileSize.setUpdatedBy("SYSTEM");
		configs.add(maxFileSize);

		repo.saveAll(configs);
		System.out.println("✅ 시스템 설정 초기화 완료: " + configs.size() + "개");
	}

	/**
	 * 관리자 계정 초기화
	 */
	private void initAdminUser(UserRepository repo, PasswordEncoder passwordEncoder) {
		// 관리자 계정
		User admin = new User();
		admin.setUsername("admin");
		admin.setPassword(passwordEncoder.encode("admin1234"));
		admin.setEmail("admin@petclinic.com");
		admin.setName("관리자");
		admin.setNickname("관리자"); // 닉네임 추가
		admin.setPhone("010-0000-0000");
		admin.setEnabled(true);

		Set<String> adminRoles = new HashSet<>();
		adminRoles.add("ROLE_ADMIN");
		adminRoles.add("ROLE_USER");
		admin.setRoles(adminRoles);

		repo.save(admin);

		// 테스트 사용자 계정
		User user = new User();
		user.setUsername("user");
		user.setPassword(passwordEncoder.encode("user1234"));
		user.setEmail("user@petclinic.com");
		user.setName("일반사용자");
		user.setNickname("테스트유저"); // 닉네임 추가
		user.setPhone("010-1111-1111");
		user.setEnabled(true);

		Set<String> userRoles = new HashSet<>();
		userRoles.add("ROLE_USER");
		user.setRoles(userRoles);

		repo.save(user);

		System.out.println("✅ 사용자 계정 초기화 완료:");
		System.out.println("   - 관리자: admin / admin1234 (닉네임: 관리자)");
		System.out.println("   - 일반사용자: user / user1234 (닉네임: 테스트유저)");
	}

	private void generateCommentsForExistingPosts(CounselPostRepository postRepo, CounselCommentRepository commentRepo) {
		List<CounselPost> posts = postRepo.findAll();
		List<CounselComment> comments = new ArrayList<>();
		for (CounselPost p : posts) {
			// COMPLETE는 반드시 1개, END는 50% 확률, WAIT는 없음
			switch (p.getStatus()) {
				case COMPLETE -> {
					comments.add(buildStaffReply(p, p.getUpdatedAt() != null ? p.getUpdatedAt().toLocalDate() : p.getCreatedAt().toLocalDate()));
					p.setCommentCount(1);
				}
				case END -> {
					if (ThreadLocalRandom.current().nextBoolean()) {
						comments.add(buildStaffReply(p, p.getUpdatedAt() != null ? p.getUpdatedAt().toLocalDate() : p.getCreatedAt().toLocalDate()));
						p.setCommentCount(1);
					}
				}
				default -> p.setCommentCount(0);
			}
		}
		if (!comments.isEmpty()) {
			commentRepo.saveAll(comments);
		}
		postRepo.saveAll(posts);
	}

	/**
	 * 커뮤니티 게시판 초기 데이터 생성
	 * - 공지사항 3개 + 더미 데이터 103개 (총 106개)
	 * - 더미 데이터는 다양한 주제로 생성
	 */
	private void initCommunityPosts(CommunityPostRepository communityPostRepo) {
		LocalDateTime now = LocalDateTime.now();
		List<CommunityPost> allPosts = new ArrayList<>();

		// 공지사항 3개
		CommunityPost post1 = new CommunityPost();
		post1.setTitle("📢 공지사항");
		post1.setContent("이 커뮤니티는 개발자들이 자유롭게 의견을 나누는 공간입니다.");
		post1.setAuthor("관리자");
		post1.setCreatedAt(now.minusDays(100));
		post1.setViewCount(199);
		post1.setLikeCount(0);
		post1.setAttachFlag(false);
		post1.setDelFlag(false);
		post1.setDeletedBy(null);
		allPosts.add(post1);

		CommunityPost post2 = new CommunityPost();
		post2.setTitle("💬 자유게시판 안내");
		post2.setContent("잡담, 질문, 공유하고 싶은 자료를 자유롭게 올려주세요.");
		post2.setAuthor("운영팀");
		post2.setCreatedAt(now.minusDays(90));
		post2.setViewCount(240);
		post2.setLikeCount(1);
		post2.setAttachFlag(false);
		post2.setDelFlag(false);
		post2.setDeletedBy(null);
		allPosts.add(post2);

		CommunityPost post3 = new CommunityPost();
		post3.setTitle("🎉 첫 이벤트 안내");
		post3.setContent("다음 달에 열리는 개발자 밋업 이벤트에 많은 참여 바랍니다!");
		post3.setAuthor("운영팀");
		post3.setCreatedAt(now.minusDays(80));
		post3.setViewCount(278);
		post3.setLikeCount(1);
		post3.setAttachFlag(false);
		post3.setDelFlag(false);
		post3.setDeletedBy(null);
		allPosts.add(post3);

		// 더미 데이터 103개 (다양한 주제)
		String[] categories = {"🔧 기술", "💡 팁", "🎓 학습", "🔥 핫이슈", "🎮 잡담"};
		String[] topics = {
			"프로젝트 구조 설계",
			"코드 리뷰 요청",
			"버그 수정 후기",
			"성능 최적화 팁",
			"라이브러리 추천",
			"개발 환경 설정",
			"테스트 코드 작성법",
			"디자인 패턴 적용",
			"알고리즘 풀이",
			"커리어 고민"
		};

		for (int i = 0; i < 103; i++) {
			CommunityPost dummyPost = new CommunityPost();
			String category = categories[i % categories.length];
			String topic = topics[i % topics.length];
			dummyPost.setTitle(category + " " + topic + " #" + (i + 1));
			dummyPost.setContent("게시글 내용입니다. " + topic + "에 대한 내용을 공유합니다.");
			dummyPost.setAuthor("회원" + (i % 20 + 1));
			dummyPost.setCreatedAt(now.minusDays(70 - (i % 70)));
			dummyPost.setViewCount(ThreadLocalRandom.current().nextInt(1, 500));
			dummyPost.setLikeCount(ThreadLocalRandom.current().nextInt(0, 50));
			dummyPost.setAttachFlag(i % 10 == 0); // 10%는 첨부파일 있음
			dummyPost.setDelFlag(false);
			dummyPost.setDeletedBy(null);
			allPosts.add(dummyPost);
		}

		communityPostRepo.saveAll(allPosts);
		System.out.println("✅ 커뮤니티 게시판 초기 데이터 생성 완료: " + allPosts.size() + "개");
	}

	/**
	 * 온라인상담 초기 데이터(112건) 랜덤 생성
	 * - 총 112개 게시글 생성 (페이지당 10개 기준, 약 11.2페이지 분량)
	 * - 상태(WAIT/COMPLETE/END)는 완전 랜덤으로 분배
	 * - 공개/비공개도 랜덤으로 분배
	 * - COMPLETE(답변완료) 상태 게시글은 댓글 1개 보장
	 * - WAIT(답변대기), END(상담종료) 상태는 댓글 없음
	 * - 공개글(secret=false)은 passwordHash=null, 비공개글(secret=true)은 BCrypt 해시 저장
	 */
	private void initCounselDataRandom(CounselPostRepository postRepo,
										CounselCommentRepository commentRepo,
										CounselContentStorage contentStorage) throws Exception {
		List<CounselPost> posts = new ArrayList<>();
		int total = 112;

		// 1단계: 모든 게시글 생성 (상태 랜덤 분배)
		for (int i = 0; i < total; i++) {
			// WAIT, COMPLETE, END 중 랜덤 선택
			CounselStatus status = randomStatus();

			// 공개/비공개 랜덤
			boolean secret = ThreadLocalRandom.current().nextBoolean();

			// 생성일 랜덤 (2025년 6~10월)
			LocalDate created = LocalDate.of(
				2025,
				ThreadLocalRandom.current().nextInt(6, 11),
				ThreadLocalRandom.current().nextInt(1, 28)
			);
			int views = ThreadLocalRandom.current().nextInt(0, 250);

			CounselPost p = buildPost("온라인 상담 #" + (i+1), "사용자" + (i+1), created, views, secret, status, contentStorage);
			posts.add(p);
		}

		// 게시글 먼저 저장 (ID 생성 필요)
		postRepo.saveAll(posts);

		// 2단계: COMPLETE(답변완료) 상태 게시글에 트리 구조 댓글 생성
		List<CounselComment> comments = new ArrayList<>();
		for (CounselPost p : posts) {
			if (p.getStatus() == CounselStatus.COMPLETE) {
				// 최상위 댓글 (운영자 답변)
				CounselComment rootComment = buildStaffReply(p, p.getCreatedAt().toLocalDate());
				comments.add(rootComment);

				// 일부 게시글에 트리 구조 댓글 추가 (30% 확률)
				if (ThreadLocalRandom.current().nextInt(100) < 30) {
					// rootComment는 아직 ID가 없으므로 저장 후 처리해야 함
					// 임시로 표시만 하고 나중에 처리
					rootComment.setContent(rootComment.getContent() + " [TREE]");
				}

				p.setCommentCount(1); // 기본 1개, 대댓글은 나중에 추가
			} else {
				p.setCommentCount(0);
			}
		}

		// 댓글 저장 (1차: 최상위 댓글만)
		if (!comments.isEmpty()) {
			commentRepo.saveAll(comments);
		}

		// 3단계: 트리 구조 댓글 생성 (대댓글, 대대댓글)
		List<CounselComment> treeComments = new ArrayList<>();
		for (CounselComment rootComment : comments) {
			if (rootComment.getContent().contains("[TREE]")) {
				// [TREE] 마커 제거
				rootComment.setContent(rootComment.getContent().replace(" [TREE]", ""));

				CounselPost p = rootComment.getPost();
				LocalDate commentDate = rootComment.getCreatedAt().toLocalDate();

				// 대댓글 1: 사용자 질문
				CounselComment reply1 = new CounselComment();
				reply1.setPost(p);
				reply1.setParent(rootComment);
				reply1.setContent("추가 질문이 있습니다. 더 자세히 설명해주실 수 있나요?");
				reply1.setAuthorName("사용자" + ThreadLocalRandom.current().nextInt(1, 100));
				// 비밀번호 해시 (테스트용: "1234")
				reply1.setPasswordHash(BCrypt.hashpw("1234", BCrypt.gensalt()));
				reply1.setStaffReply(false);
				reply1.setCreatedAt(commentDate.plusDays(1).atStartOfDay());
				reply1.setUpdatedAt(reply1.getCreatedAt());
				treeComments.add(reply1);

				// 대대댓글 1-1: 운영자 재답변
				CounselComment reply1_1 = new CounselComment();
				reply1_1.setPost(p);
				reply1_1.setParent(reply1);
				reply1_1.setContent("네, 자세히 설명드리겠습니다. 추가 정보는 다음과 같습니다...");
				reply1_1.setAuthorName(STAFF);
				reply1_1.setPasswordHash(null);
				reply1_1.setStaffReply(true);
				reply1_1.setCreatedAt(commentDate.plusDays(2).atStartOfDay());
				reply1_1.setUpdatedAt(reply1_1.getCreatedAt());
				treeComments.add(reply1_1);

				// 50% 확률로 대대대댓글 추가
				if (ThreadLocalRandom.current().nextBoolean()) {
					CounselComment reply1_1_1 = new CounselComment();
					reply1_1_1.setPost(p);
					reply1_1_1.setParent(reply1_1);
					reply1_1_1.setContent("감사합니다! 이해가 잘 되었습니다.");
					reply1_1_1.setAuthorName("사용자" + ThreadLocalRandom.current().nextInt(1, 100));
					// 비밀번호 해시 (테스트용: "1234")
					reply1_1_1.setPasswordHash(BCrypt.hashpw("1234", BCrypt.gensalt()));
					reply1_1_1.setStaffReply(false);
					reply1_1_1.setCreatedAt(commentDate.plusDays(3).atStartOfDay());
					reply1_1_1.setUpdatedAt(reply1_1_1.getCreatedAt());
					treeComments.add(reply1_1_1);
				}

				// commentCount 업데이트
				p.setCommentCount(p.getCommentCount() + treeComments.size());
			}
		}

		// 트리 댓글 저장
		if (!treeComments.isEmpty()) {
			commentRepo.saveAll(treeComments);
		}

		// rootComment 업데이트 (마커 제거)
		commentRepo.saveAll(comments);

		// commentCount 업데이트를 위해 게시글 다시 저장
		postRepo.saveAll(posts);
	}

	private CounselStatus randomStatus() {
		CounselStatus[] values = CounselStatus.values();
		int idx = ThreadLocalRandom.current().nextInt(values.length);
		return values[idx];
	}

	/** 게시글 생성 헬퍼: 공개/비공개, 상태에 따른 필드/비밀번호 처리 포함 */
	private CounselPost buildPost(String title, String author,
								 LocalDate createdDate, int views,
								 boolean secret, CounselStatus status,
								 CounselContentStorage contentStorage) throws Exception {
		CounselPost p = new CounselPost();
		p.setTitle(title);
		p.setAuthorName(author);
		p.setAuthorEmail(null);
		p.setSecret(secret);
		if (secret) {
			// 비밀번호 해시 (테스트용: "1234")
			p.setPasswordHash(BCrypt.hashpw("1234", BCrypt.gensalt()));
		} else {
			p.setPasswordHash(null);
		}
		p.setViewCount(views);
		p.setStatus(status);
		LocalDateTime created = createdDate.atStartOfDay();
		p.setCreatedAt(created);
		p.setUpdatedAt(created);
		// HTML을 전체 문서 형태로 저장하여 Tika가 text/html로 확실히 인식하도록 함
		String html = "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body><p>" + title + " 내용입니다.</p></body></html>";
		String path = contentStorage.saveHtml(html);
		p.setContent("[stored]");
		p.setContentPath(path);
		p.setAttachFlag(false);
		p.setDelFlag(false);
		p.setDeletedBy(null);
		return p;
	}

	/** 운영자(staff) 댓글 1개 생성 */
	private CounselComment buildStaffReply(CounselPost p, LocalDate replyDate) {
		CounselComment c = new CounselComment();
		c.setPost(p);
		c.setParent(null);
		c.setContent("[답변] " + p.getTitle() + " 에 대한 답변입니다.");
		c.setAuthorName(STAFF);
		c.setAuthorEmail(null);
		c.setPasswordHash(null);
		c.setStaffReply(true);
		LocalDateTime replyAt = replyDate.atStartOfDay();
		c.setCreatedAt(replyAt);
		c.setUpdatedAt(replyAt);
		return c;
	}

	/**
	 * 포토게시판 초기 데이터 생성
	 * - 총 15개 게시글 생성
	 * - 썸네일은 /images/sample/ 경로의 샘플 이미지 사용
	 * - 본문에는 Quill 에디터 포맷으로 이미지와 텍스트 포함
	 */
	private void initPhotoData(PhotoPostRepository photoPostRepo) {
		LocalDateTime now = LocalDateTime.now();
		List<PhotoPost> posts = new ArrayList<>();

		// 샘플 이미지 URL (실제 프로젝트에 포함된 이미지 또는 외부 URL)
		String[] sampleImages = {
			"/images/sample/dog1.jpg",
			"/images/sample/cat1.jpg",
			"/images/sample/dog2.jpg",
			"/images/sample/cat2.jpg",
			"/images/sample/pet1.jpg"
		};

		String[] titles = {
			"우리 강아지 산책 일상 📷",
			"고양이 집사의 하루 🐱",
			"반려견 목욕시키기 🛁",
			"새로 입양한 아기 고양이",
			"강아지 미용 비포 애프터",
			"고양이 장난감 만들기",
			"반려동물 건강검진 후기",
			"강아지와 함께한 여행",
			"고양이 간식 레시피",
			"펫카페 방문 후기",
			"우리 집 반려동물 소개",
			"강아지 훈련 성공기",
			"고양이 발톱 관리 팁",
			"반려동물 사진 잘 찍는 법",
			"펫 용품 추천 리스트"
		};

		String[] authors = {
			"강아지러버", "고양이집사", "펫마스터", "동물사랑", "펫케어",
			"멍멍이맘", "냥냥이아빠", "펫그램", "반려일상", "동물친구"
		};

		for (int i = 0; i < 15; i++) {
			PhotoPost post = new PhotoPost();
			post.setTitle(titles[i]);

			// 썸네일 URL (5개 이미지 순환)
			String thumbnailUrl = sampleImages[i % sampleImages.length];
			post.setThumbnailUrl(thumbnailUrl);

			// Quill 에디터 포맷으로 본문 작성
			String content = String.format(
				"<h2>%s</h2>" +
				"<p>안녕하세요! 오늘은 정말 즐거운 하루였어요. 😊</p>" +
				"<p><img src=\"%s\" alt=\"사진\" style=\"max-width: 100%%; height: auto;\"></p>" +
				"<p><strong>반려동물</strong>과 함께한 시간은 언제나 행복합니다.</p>" +
				"<ul>" +
				"<li>사진 찍기 좋은 날씨</li>" +
				"<li>건강한 모습</li>" +
				"<li>즐거운 시간</li>" +
				"</ul>" +
				"<p>여러분도 좋은 하루 보내세요! 💕</p>",
				titles[i], thumbnailUrl
			);
			post.setContent(content);

			post.setAuthor(authors[i % authors.length]);
			post.setCreatedAt(now.minusDays(15 - i)); // 최신순으로 정렬되도록
			post.setViewCount(ThreadLocalRandom.current().nextInt(10, 300));
			post.setLikeCount(ThreadLocalRandom.current().nextInt(0, 50));
			post.setDelFlag(false);

			posts.add(post);
		}

		photoPostRepo.saveAll(posts);
		System.out.println("✅ 포토게시판 초기 데이터 생성 완료: " + posts.size() + "개");
	}

	/**
	 * FAQ 게시판 초기 데이터 생성
	 * - 총 15개 게시글 생성 (카테고리별 균등 분배)
	 * - 카테고리: 일반(3), 진료(3), 예약(3), 수술(3), 기타(3)
	 * - displayOrder로 정렬 순서 관리
	 */
	private void initFaqData(FaqPostRepository faqPostRepo) {
		LocalDateTime now = LocalDateTime.now();
		List<FaqPost> faqs = new ArrayList<>();

		// 일반 카테고리 (3개)
		faqs.add(createFaq("반려동물 등록은 어떻게 하나요?",
			"<p>반려동물 등록은 <strong>동물병원</strong> 또는 <strong>시·군·구청</strong>에서 가능합니다.</p>" +
			"<p>필요 서류:</p>" +
			"<ul><li>신분증</li><li>광견병 예방접종 증명서</li><li>등록비 (약 3,000원)</li></ul>",
			"일반", 1, now.minusDays(100)));

		faqs.add(createFaq("진료 기록은 어떻게 확인하나요?",
			"<p>진료 기록은 <strong>마이페이지 &gt; 진료 내역</strong>에서 확인하실 수 있습니다.</p>" +
			"<p>최근 1년간의 진료 기록이 자동으로 저장됩니다.</p>",
			"일반", 2, now.minusDays(95)));

		faqs.add(createFaq("회원가입은 필수인가요?",
			"<p>회원가입 없이도 <strong>온라인상담</strong>과 <strong>FAQ</strong>는 이용 가능합니다.</p>" +
			"<p>단, <strong>예약</strong> 및 <strong>진료 기록 조회</strong>는 회원가입이 필요합니다.</p>",
			"일반", 3, now.minusDays(90)));

		// 진료 카테고리 (3개)
		faqs.add(createFaq("진료 가능한 시간은 언제인가요?",
			"<p>진료 시간: <strong>평일 09:00 ~ 19:00</strong></p>" +
			"<p>점심시간: <strong>12:00 ~ 13:00</strong></p>" +
			"<p>토요일: <strong>09:00 ~ 15:00</strong> (점심시간 없음)</p>" +
			"<p><em>일요일 및 공휴일은 휴진입니다.</em></p>",
			"진료", 4, now.minusDays(85)));

		faqs.add(createFaq("야간 진료도 가능한가요?",
			"<p><strong>야간 진료</strong>는 <strong>응급 상황</strong>에 한해 가능합니다.</p>" +
			"<p>야간 응급 진료 시간: <strong>19:00 ~ 22:00</strong></p>" +
			"<p>사전 전화 연락 필수: <strong>02-1234-5678</strong></p>",
			"진료", 5, now.minusDays(80)));

		faqs.add(createFaq("처음 방문 시 준비물이 있나요?",
			"<p>초진 방문 시 준비물:</p>" +
			"<ul>" +
			"<li><strong>신분증</strong> (보호자)</li>" +
			"<li><strong>동물 등록증</strong> (있는 경우)</li>" +
			"<li><strong>예방접종 기록</strong> (있는 경우)</li>" +
			"<li><strong>이전 병원 진료 기록</strong> (있는 경우)</li>" +
			"</ul>",
			"진료", 6, now.minusDays(75)));

		// 예약 카테고리 (3개)
		faqs.add(createFaq("예약은 어떻게 하나요?",
			"<p>예약 방법:</p>" +
			"<ol>" +
			"<li><strong>온라인 예약</strong>: 홈페이지 로그인 후 예약 메뉴</li>" +
			"<li><strong>전화 예약</strong>: 02-1234-5678</li>" +
			"<li><strong>방문 예약</strong>: 병원 직접 방문</li>" +
			"</ol>" +
			"<p><em>온라인 예약은 24시간 가능합니다.</em></p>",
			"예약", 7, now.minusDays(70)));

		faqs.add(createFaq("예약 취소는 언제까지 가능한가요?",
			"<p>예약 취소는 <strong>예약 시간 2시간 전</strong>까지 가능합니다.</p>" +
			"<p>취소 방법:</p>" +
			"<ul>" +
			"<li>마이페이지 &gt; 예약 내역에서 직접 취소</li>" +
			"<li>전화 취소: 02-1234-5678</li>" +
			"</ul>" +
			"<p><strong style='color: red;'>무단 노쇼 3회 시 예약 제한될 수 있습니다.</strong></p>",
			"예약", 8, now.minusDays(65)));

		faqs.add(createFaq("예약 없이 방문 가능한가요?",
			"<p><strong>예약 없이도 방문 가능</strong>하나, 대기 시간이 길어질 수 있습니다.</p>" +
			"<p>혼잡 시간대 (10:00 ~ 12:00, 14:00 ~ 18:00)는 예약을 권장합니다.</p>",
			"예약", 9, now.minusDays(60)));

		// 수술 카테고리 (3개)
		faqs.add(createFaq("중성화 수술 비용은 얼마인가요?",
			"<p>중성화 수술 비용:</p>" +
			"<ul>" +
			"<li><strong>수컷 (거세)</strong>: 150,000 ~ 200,000원</li>" +
			"<li><strong>암컷 (난소 적출)</strong>: 200,000 ~ 300,000원</li>" +
			"</ul>" +
			"<p>체중 및 건강 상태에 따라 금액이 달라질 수 있습니다.</p>" +
			"<p><em>정확한 비용은 진료 후 안내해 드립니다.</em></p>",
			"수술", 10, now.minusDays(55)));

		faqs.add(createFaq("수술 전 금식이 필요한가요?",
			"<p><strong>수술 전 금식</strong>은 필수입니다.</p>" +
			"<ul>" +
			"<li>음식: <strong>수술 12시간 전</strong>부터 금식</li>" +
			"<li>물: <strong>수술 6시간 전</strong>부터 금수</li>" +
			"</ul>" +
			"<p>금식하지 않을 경우 <strong>마취 중 구토</strong>로 인한 위험이 있습니다.</p>",
			"수술", 11, now.minusDays(50)));

		faqs.add(createFaq("수술 후 입원이 필요한가요?",
			"<p>수술 종류에 따라 다릅니다:</p>" +
			"<ul>" +
			"<li><strong>중성화 수술</strong>: 당일 퇴원 (회복 후 4~6시간)</li>" +
			"<li><strong>복강경 수술</strong>: 1박 2일 입원 권장</li>" +
			"<li><strong>응급 수술</strong>: 상태에 따라 2~5일 입원</li>" +
			"</ul>",
			"수술", 12, now.minusDays(45)));

		// 기타 카테고리 (3개)
		faqs.add(createFaq("주차는 가능한가요?",
			"<p>병원 건물 지하에 <strong>무료 주차장</strong>이 있습니다.</p>" +
			"<p>주차 공간: <strong>총 15대</strong></p>" +
			"<p>만차 시 인근 공영주차장 (도보 3분) 이용 부탁드립니다.</p>",
			"기타", 13, now.minusDays(40)));

		faqs.add(createFaq("반려동물 동반 입장 시 주의사항은?",
			"<p>다른 반려동물과의 접촉을 최소화하기 위해:</p>" +
			"<ul>" +
			"<li><strong>목줄 착용 필수</strong> (강아지)</li>" +
			"<li><strong>이동장 사용 권장</strong> (고양이, 소형견)</li>" +
			"<li>공격성 있는 반려동물은 <strong>입마개 착용</strong></li>" +
			"</ul>",
			"기타", 14, now.minusDays(35)));

		faqs.add(createFaq("진료비 카드 결제 가능한가요?",
			"<p><strong>모든 카드 결제 가능</strong>합니다.</p>" +
			"<p>지원 결제 수단:</p>" +
			"<ul>" +
			"<li>신용카드 / 체크카드</li>" +
			"<li>현금</li>" +
			"<li>계좌이체</li>" +
			"<li>간편결제 (카카오페이, 네이버페이)</li>" +
			"</ul>" +
			"<p><em>할부는 5만원 이상부터 가능합니다.</em></p>",
			"기타", 15, now.minusDays(30)));

		faqPostRepo.saveAll(faqs);
		System.out.println("✅ FAQ 게시판 초기 데이터 생성 완료: " + faqs.size() + "개");
	}

	/**
	 * FAQ 게시글 생성 헬퍼 메서드
	 */
	private FaqPost createFaq(String question, String answer, String category,
							  Integer displayOrder, LocalDateTime createdAt) {
		FaqPost faq = new FaqPost();
		faq.setQuestion(question);
		faq.setAnswer(answer);
		faq.setCategory(category);
		faq.setDisplayOrder(displayOrder);
		faq.setCreatedAt(createdAt);
		faq.setUpdatedAt(createdAt);
		faq.setDelFlag(false);
		return faq;
	}

	/**
	 * 커뮤니티 게시판 좋아요 초기 데이터 생성
	 * - 상위 20개 게시글에 대해 랜덤으로 좋아요 생성
	 * - 관리자(admin) 계정이 좋아요를 누른 것으로 설정
	 * - 좋아요 개수는 0~10개 사이로 랜덤 생성
	 */
	private void initCommunityLikes(CommunityPostRepository postRepo,
									org.springframework.samples.petclinic.community.repository.CommunityPostLikeRepository likeRepo,
									UserRepository userRepo) {
		try {
			// 모든 게시글 조회
			List<CommunityPost> allPosts = postRepo.findAll();
			if (allPosts.isEmpty()) {
				System.out.println("⚠️ 커뮤니티 게시글이 없어 좋아요 데이터를 생성하지 않습니다.");
				return;
			}

			// 상위 20개 게시글만 선택
			List<CommunityPost> posts = allPosts.size() > 20
				? allPosts.subList(0, 20)
				: allPosts;

			// 관리자 계정 조회
			User admin = userRepo.findByUsername("admin")
				.orElseGet(() -> {
					// 관리자가 없으면 임시로 "admin" username 사용
					System.out.println("⚠️ 관리자 계정을 찾을 수 없어 'admin' username을 사용합니다.");
					return null;
				});

			String likeUsername = admin != null ? admin.getUsername() : "admin";

			List<org.springframework.samples.petclinic.community.table.CommunityPostLike> likes = new ArrayList<>();
			int totalLikes = 0;

			for (CommunityPost post : posts) {
				// 각 게시글마다 0~10개의 좋아요 랜덤 생성
				int likeCount = ThreadLocalRandom.current().nextInt(0, 11);

				for (int i = 0; i < likeCount; i++) {
					// 사용자는 "admin", "user1", "user2", ... 형식으로 생성
					String username = i == 0 ? likeUsername : "user" + i;

					org.springframework.samples.petclinic.community.table.CommunityPostLike like =
						new org.springframework.samples.petclinic.community.table.CommunityPostLike(post, username);
					likes.add(like);
					totalLikes++;
				}
			}

			likeRepo.saveAll(likes);
			System.out.println("✅ 커뮤니티 좋아요 초기 데이터 생성 완료: " + totalLikes + "개 (게시글 " + posts.size() + "개)");
		} catch (Exception e) {
			System.err.println("❌ 커뮤니티 좋아요 초기 데이터 생성 실패: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 포토게시판 좋아요 초기 데이터 생성 (Phase 2-3)
	 * - 모든 포토게시글에 대해 랜덤으로 좋아요 생성
	 * - 관리자(admin) 계정이 좋아요를 누른 것으로 설정
	 * - 좋아요 개수는 5~20개 사이로 랜덤 생성 (포토게시판은 좋아요가 많을 것으로 예상)
	 */
	private void initPhotoLikes(PhotoPostRepository postRepo,
								org.springframework.samples.petclinic.photo.repository.PhotoPostLikeRepository likeRepo,
								UserRepository userRepo) {
		try {
			// 모든 포토게시글 조회
			List<PhotoPost> allPosts = postRepo.findAll();
			if (allPosts.isEmpty()) {
				System.out.println("⚠️ 포토게시글이 없어 좋아요 데이터를 생성하지 않습니다.");
				return;
			}

			// 관리자 계정 조회
			User admin = userRepo.findByUsername("admin")
				.orElseGet(() -> {
					// 관리자가 없으면 임시로 "admin" username 사용
					System.out.println("⚠️ 관리자 계정을 찾을 수 없어 'admin' username을 사용합니다.");
					return null;
				});

			String likeUsername = admin != null ? admin.getUsername() : "admin";

			List<org.springframework.samples.petclinic.photo.table.PhotoPostLike> likes = new ArrayList<>();
			int totalLikes = 0;

			for (PhotoPost post : allPosts) {
				// 각 게시글마다 5~20개의 좋아요 랜덤 생성 (포토게시판은 인기가 많음)
				int likeCount = ThreadLocalRandom.current().nextInt(5, 21);

				for (int i = 0; i < likeCount; i++) {
					// 사용자는 "admin", "user1", "user2", ... 형식으로 생성
					String username = i == 0 ? likeUsername : "user" + i;

					org.springframework.samples.petclinic.photo.table.PhotoPostLike like =
						new org.springframework.samples.petclinic.photo.table.PhotoPostLike(post, username);
					likes.add(like);
					totalLikes++;
				}
			}

			likeRepo.saveAll(likes);
			System.out.println("✅ 포토게시판 좋아요 초기 데이터 생성 완료: " + totalLikes + "개 (게시글 " + allPosts.size() + "개)");
		} catch (Exception e) {
			System.err.println("❌ 포토게시판 좋아요 초기 데이터 생성 실패: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
