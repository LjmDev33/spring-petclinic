package org.springframework.samples.petclinic.common.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.community.repository.CommunityPostRepository;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.counsel.CounselStatus;
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
										PasswordEncoder passwordEncoder){
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

			// 온라인상담 데이터 초기화
			long postCount = counselPostRepo.count();
			long commentCount = counselCommentRepo.count();
			if(postCount == 0){
				initCounselDataRandom(counselPostRepo, counselCommentRepo, contentStorage);
			} else if (postCount > 0 && commentCount == 0) {
				generateCommentsForExistingPosts(counselPostRepo, counselCommentRepo);
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
		multiLogin.setDescription("멀티로그인 허용 여부. true: 멀티로그인 허용, false: 단일 로그인만 허용");
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

	private void initCommunityPosts(CommunityPostRepository communityPostRepo) {
		LocalDateTime now = LocalDateTime.now();

		CommunityPost post1 = new CommunityPost();
		post1.setTitle("📢 공지사항");
		post1.setContent("이 커뮤니티는 개발자들이 자유롭게 의견을 나누는 공간입니다.");
		post1.setAuthor("관리자");
		post1.setCreatedAt(LocalDateTime.now());
		post1.setViewCount(199);
		post1.setLikeCount(0);
		post1.setAttachFlag(false);
		post1.setDelFlag(false);
		post1.setDeletedBy("");

		CommunityPost post2 = new CommunityPost();
		post2.setTitle("💬 자유게시판 안내");
		post2.setContent("잡담, 질문, 공유하고 싶은 자료를 자유롭게 올려주세요.");
		post2.setAuthor("운영팀");
		post2.setCreatedAt(LocalDateTime.now());
		post2.setViewCount(240);
		post2.setLikeCount(1);
		post2.setAttachFlag(false);
		post2.setDelFlag(false);
		post2.setDeletedBy("");

		CommunityPost post3 = new CommunityPost();
		post3.setTitle("🎉 첫 이벤트 안내");
		post3.setContent("다음 달에 열리는 개발자 밋업 이벤트에 많은 참여 바랍니다!");
		post3.setAuthor("운영팀");
		post3.setCreatedAt(LocalDateTime.now());
		post3.setViewCount(278);
		post3.setLikeCount(1);
		post3.setAttachFlag(false);
		post3.setDelFlag(false);
		post3.setDeletedBy("");

		/* 페이징 작업 대비 더미데이터 */
		List<CommunityPost> posts = new ArrayList<>();
		for (int i = 0; i < 103; i++) {
			CommunityPost noticeDummyData = new CommunityPost();
			noticeDummyData.setTitle("테스트 제목" + i);
			noticeDummyData.setContent("테스트 내용 추가");
			noticeDummyData.setAuthor("전산팀");
			noticeDummyData.setCreatedAt(now);
			noticeDummyData.setViewCount(i);
			noticeDummyData.setLikeCount(0);
			noticeDummyData.setAttachFlag(false);
			noticeDummyData.setDelFlag(false);
			noticeDummyData.setDeletedBy("");
			posts.add(noticeDummyData);
		}

		communityPostRepo.save(post1);
		communityPostRepo.save(post2);
		communityPostRepo.save(post3);
		communityPostRepo.saveAll(posts);
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

		// 2단계: COMPLETE(답변완료) 상태 게시글에만 댓글 생성
		List<CounselComment> comments = new ArrayList<>();
		for (CounselPost p : posts) {
			if (p.getStatus() == CounselStatus.COMPLETE) {
				CounselComment comment = buildStaffReply(p, p.getCreatedAt().toLocalDate());
				comments.add(comment);
				p.setCommentCount(1);
			} else {
				p.setCommentCount(0);
			}
		}

		// 댓글 저장
		if (!comments.isEmpty()) {
			commentRepo.saveAll(comments);
		}

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
}
