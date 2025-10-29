package org.springframework.samples.petclinic.common.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.community.repository.CommunityPostRepository;
import org.springframework.samples.petclinic.community.table.CommunityPost;
import org.springframework.samples.petclinic.counsel.CounselStatus;
import org.springframework.samples.petclinic.counsel.repository.CounselCommentRepository;
import org.springframework.samples.petclinic.counsel.repository.CounselPostRepository;
import org.springframework.samples.petclinic.counsel.table.CounselComment;
import org.springframework.samples.petclinic.counsel.table.CounselPost;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Project : spring-petclinic
 * File    : DataInit.java
 * Created : 2025-10-24
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 프로젝트 모든 게시판 및 유저정보들 초기 데이터 삽입
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
										CounselCommentRepository counselCommentRepo){
		return args -> {
			/*커뮤니티 -> 공지사항 리스트데이터 init*/
			if(communityPostRepo.count() == 0){
				initCommunityPosts(communityPostRepo);
			}
			/*온라인상담 게시판 리스트와 댓글 FK관계고려하여 두 테이블 다 체크*/
			if(counselPostRepo.count() == 0 && counselCommentRepo.count() == 0){
				List<CounselPost> savedPosts = initCounselPosts(counselPostRepo);
				initCounselComments(counselCommentRepo, savedPosts);
			}

			// 🔎 (옵션) 게시글은 있는데 댓글만 비었을 때 댓글만 채우고 싶다면 아래 주석 해제
			// else if (counselPostRepo.count() > 0 && counselCommentRepo.count() == 0) {
			//     List<CounselPost> postsToAnswer = counselPostRepo.findAll(); // 필요시 status=COMPLETE만 추려도 됨
			//     initCounselComments(counselCommentRepo, postsToAnswer);
			// }
		};
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

		/*페이징 작업 대비하여 더미데이터 추가*/
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

	private List<CounselPost> initCounselPosts(CounselPostRepository counselPostRepo) {
		List<CounselPost> counselPosts = new ArrayList<>();

		counselPosts.add(newPost("잠복고환 복강경수술 문의", "권혜경",
			"2025-10-16", 3, true, "2025-10-21"));

		counselPosts.add(newPost("여아 강아지 복강경 중성화 수술 비용문의", "김상록",
			"2025-10-11", 2, true, "2025-10-12"));

		counselPosts.add(newPost("강아지 다리 절뚝거림", "장혜원",
			"2025-10-04", 3, true, "2025-10-12"));

		counselPosts.add(newPost("2025 자랑스런 대한민국인 & 파워브랜드대상 인터뷰 건", "스포츠조선",
			"2025-09-29", 3, true, "2025-09-30"));

		counselPosts.add(newPost("기관지", "김경진",
			"2025-09-28", 3, true, "2025-09-30"));

		counselPosts.add(newPost("수컷고양이 중성화수술비", "홍차",
			"2025-09-27", 3, true, "2025-09-30"));

		counselPosts.add(newPost("SBS Biz 라이프 매거진 ‘참 좋은 하루’ 방송프로그램 출연섭외건으로 문의드립니다", "장신애",
			"2025-09-26", 2, true, "2025-09-30"));

		counselPosts.add(newPost("여아 복강경 중성화 수술 문의", "이세인",
			"2025-09-09", 3, false, null));

		return counselPostRepo.saveAll(counselPosts);

	}

	private CounselPost newPost(String title, String author, String postYmd,
								int views, boolean answered, String replyYmdOrNull) {
		CounselPost p = new CounselPost();
		p.setTitle(title);
		p.setContent(title + " 내용입니다.");
		p.setAuthorName(author);
		p.setAuthorEmail(null);
		p.setPasswordHash(null);
		p.setSecret(true);
		p.setViewCount(views);
		p.setCommentCount(answered ? 1 : 0);
		p.setStatus(answered ? CounselStatus.COMPLETE : CounselStatus.WAIT);
		LocalDateTime created = LocalDate.parse(postYmd).atStartOfDay();
		p.setCreatedAt(created);
		p.setUpdatedAt(created);

		if (answered && replyYmdOrNull != null) {
			LocalDateTime replyAt = LocalDate.parse(replyYmdOrNull).atStartOfDay();
			p.setUpdatedAt(replyAt);
		}
		p.setAttachFlag(false);
		p.setDelFlag(false);
		p.setDeletedBy(null);
		return p;

	}

	private void initCounselComments(CounselCommentRepository counselCommentRepo,
									 List<CounselPost> posts) {
		List<CounselComment> comments = new ArrayList<>();
		for (CounselPost p : posts) {
			if (p.getStatus() == CounselStatus.COMPLETE) {
				LocalDateTime replyAt = p.getUpdatedAt(); // newPost()에서 답변일로 세팅됨
				CounselComment c = new CounselComment();
				c.setPost(p);
				c.setParent(null);
				c.setContent("[답변] " + p.getTitle() + " 에 대한 답변입니다.");
				c.setAuthorName(STAFF);
				c.setAuthorEmail(null);
				c.setPasswordHash(null);
				c.setStaffReply(true);
				c.setCreatedAt(replyAt);
				c.setUpdatedAt(replyAt);
				comments.add(c);
			}
		}
		if (!comments.isEmpty()) {
			counselCommentRepo.saveAll(comments);
		}
	}



}
