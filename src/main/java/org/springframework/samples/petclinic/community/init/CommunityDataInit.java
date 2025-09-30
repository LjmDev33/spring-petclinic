package org.springframework.samples.petclinic.community.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.petclinic.community.CommunityPost;
import org.springframework.samples.petclinic.community.repository.CommunityPostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Project : spring-petclinic
 * File    : CommunityDataInit.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Configuration
public class CommunityDataInit {

	@Bean
	CommandLineRunner initCommunityData(CommunityPostRepository repository){
		return args -> {
			if(repository.count() == 0){
				CommunityPost post1 = new CommunityPost();
				post1.setTitle("📢 공지사항");
				post1.setContent("이 커뮤니티는 개발자들이 자유롭게 의견을 나누는 공간입니다.");
				post1.setAuthor("관리자");
				post1.setCreatedAt(LocalDateTime.now());
				post1.setViewCount(199);
				post1.setLikeCount(0);

				CommunityPost post2 = new CommunityPost();
				post2.setTitle("💬 자유게시판 안내");
				post2.setContent("잡담, 질문, 공유하고 싶은 자료를 자유롭게 올려주세요.");
				post2.setAuthor("운영팀");
				post2.setCreatedAt(LocalDateTime.now());
				post2.setViewCount(240);
				post2.setLikeCount(1);

				CommunityPost post3 = new CommunityPost();
				post3.setTitle("🎉 첫 이벤트 안내");
				post3.setContent("다음 달에 열리는 개발자 밋업 이벤트에 많은 참여 바랍니다!");
				post3.setAuthor("운영팀");
				post3.setCreatedAt(LocalDateTime.now());
				post3.setViewCount(278);
				post3.setLikeCount(1);

				/*페이징 작업 대비하여 더미데이터 추가*/
				List<CommunityPost> posts = new ArrayList<>();

				LocalDateTime now = LocalDateTime.now();

				for (int i = 0; i < 103; i++) {
					CommunityPost noticeDummyData = new CommunityPost();
					noticeDummyData.setTitle("테스트 제목" + i);
					noticeDummyData.setContent("테스트 내용 추가");
					noticeDummyData.setAuthor("전산팀");
					noticeDummyData.setCreatedAt(now);
					noticeDummyData.setViewCount(i);
					noticeDummyData.setLikeCount(0);

					posts.add(noticeDummyData);
				}

				repository.save(post1);
				repository.save(post2);
				repository.save(post3);

				repository.saveAll(posts);
			}
		};
	}
}
