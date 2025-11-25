package org.springframework.samples.petclinic.photo.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.samples.petclinic.photo.repository.PhotoPostRepository;
import org.springframework.samples.petclinic.photo.table.PhotoPost;
import org.springframework.stereotype.Component;

/**
 * Project : spring-petclinic
 * File    : PhotoDataInit.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 초기 데이터 생성
 */
@Component
@Order(5)
public class PhotoDataInit implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(PhotoDataInit.class);

	private final PhotoPostRepository photoPostRepository;

	public PhotoDataInit(PhotoPostRepository photoPostRepository) {
		this.photoPostRepository = photoPostRepository;
	}

	@Override
	public void run(String... args) {
		if (photoPostRepository.count() > 0) {
			log.info("포토게시판 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
			return;
		}

		log.info("포토게시판 초기 데이터 생성 시작...");

		// src/main/resources/static/animal 폴더의 이미지 파일 목록 (실제 존재하는 27개 파일)
		// 경로: /animal/** → classpath:/static/animal/ (WebConfig에서 매핑)
		// Git에 포함되므로 모든 사용자가 이미지를 볼 수 있음
		String[] animalImages = {
			"/animal/2105636287_0FDjfSC7_20250923_abcamc_10EC9B94ECA784EBA38CEC9588EB82B4.jpg",
			"/animal/2105636287_iv9cGu24_20250701_abcamc_EAB1B4EAB095EAB280ECA784EC9DB4EBB2A4ED8AB8_EC9BB9EC9AA9_H1.jpg",
			"/animal/2105636287_jvBJblHY_20250923_abcamc_EAB491EAB2ACEBB391EAB480EB82B4_EC9888EBB0A9ECA091ECA285EC9DB4EBB2A4ED8AB8_H1.jpg",
			"/animal/3697191616_67233c5948140_ED8AB9ED9994ECA784EBA38C_EAB3A0EC9691EC9DB4ECA784EBA38C.jpg",
			"/animal/3697191616_67242aab9d813_ECA784EBA38CEAB3BCEBAAA9_EB82B4EAB3BC.jpg",
			"/animal/3697191616_67242d7845837_ECA784EBA38CEAB3BCEBAAA9_ECB998EAB3BC.jpg",
			"/animal/3697191616_672433a9be06b_ECA784ECA491ECB998EBA38CEC84BCED84B0_EC99B8EAB3BC.jpg",
			"/animal/3697191616_672438c314d48_ECA784ECA491ECB998EBA38CEC84BCED84B0_PDA.jpg",
			"/animal/3697191616_672438d2209d4_ECA784ECA491ECB998EBA38CEC84BCED84B0_EAB1B4EAB095EAB280ECA784.jpg",
			"/animal/3697191616_672442be07915_EBAFB8EC8598_ECA084EBACB4ECA081EC9DB8EAB8B0EC88A0.png",
			"/animal/3697191616_672442dbb5c05_EBAFB8EC8598_EB8692EC9D80EC8898ECA480EC9D98EAB8B0ECA480.png",
			"/animal/3697191616_6724805c2e7a6_ECA784ECA491ECB998EBA38CEC84BCED84B0_EC9D91EAB889ECA784EBA38C.jpg",
			"/animal/987832805_6729e192cb260_ECA784EBA38CEAB3BCEBAAA9_ED94BCEBB680EAB3BC_N1.png",
			"/animal/987832805_6729e19e1f9bf_ECA784EBA38CEAB3BCEBAAA9_EC9588EAB3BC_N1.png",
			"/animal/987832805_672c6878bc2ea_EB8BB4EB82ADED8C8CEC97B4.jpg",
			"/animal/987832805_672c6be565e59_ED8AB9ED9994ECA784EBA38C_EB9494EC8AA4ED81AC.jpg",
			"/animal/987832805_6733fec6a701b_ED8AB9ED9994ECA784EBA38C_EC8AACEAB09CEAB3A8ED8388EAB5AC_2.jpg",
			"/animal/987832805_673be8f8e5f05_ED8AB9ED9994ECA784EBA38C_ECA285EC9691EC99B8EAB3BC.png",
			"/animal/987832805_674452617a88e_EAB095EC9584ECA780EC9E94ECA1B4EC9CA0ECB998EBB09CECB998ECBC80EC9DB4EC8AA4.jpg",
			"/animal/987832805_67445320580e5_EAB3A0EC9691EC9DB4EAB2BDEAB3A8EAB3A8ECA088.jpg",
			"/animal/987832805_67481e5be7158_EC97ACEC9584ECA491EC84B1ED9994ECBC80EC9DB4EC8AA4.jpg",
			"/animal/987832805_67481f191beef_ECA084EBB0A9EC8BADEC9E90EC9DB8EB8C80ED8C8CEC97B4.jpg",
			"/animal/987832805_67481ffb956c0_ECA084EBB0A9EC8BADEC9E90EC9DB8EB8C80ED8C8CEC97B4EC8898EC88A0.jpg",
			"/animal/987832805_6748229c92ee8_EBAFB8EC8598_EBAAB0EC9E85EC9D98EAB09CEB8590_N2.png",
			"/animal/987832805_6751c1d5714d6_EBACB8ECA09CEC84B1EC9B90EC9EA5EB8B98.png",
			"/animal/987832805_6751d36626050_EAB980EAB1B4ED98B8EC9B90EC9EA5EB8B98.png",
			"/animal/987832805_67663c7e95d45_EC9881EC8381EC84BCED84B0_H1.png"
		};

		// 샘플 데이터 20개 생성 (animal 폴더 이미지 사용)
		for (int i = 1; i <= 20; i++) {
			String randomImage = animalImages[(int) (Math.random() * animalImages.length)];

			PhotoPost post = new PhotoPost();
			post.setTitle("우리 아이 사진 #" + i);
			post.setContent("<p><img src=\"" + randomImage + "\" alt=\"동물 사진\" style=\"max-width: 100%;\"></p>"
				+ "<p>우리 아이의 귀여운 모습입니다! 😊</p>"
				+ "<p>건강하게 잘 자라고 있어요.</p>");
			post.setAuthor("사용자" + (i % 5 + 1));
			post.setThumbnailUrl(randomImage);
			post.setViewCount((int) (Math.random() * 100));
			post.setLikeCount((int) (Math.random() * 50));

			photoPostRepository.save(post);
		}

		log.info("✅ 포토게시판 초기 데이터 생성 완료: 총 20개 게시글 (animal 폴더의 27개 이미지 중 랜덤 사용)");
	}
}

