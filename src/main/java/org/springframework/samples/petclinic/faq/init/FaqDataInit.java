package org.springframework.samples.petclinic.faq.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.samples.petclinic.faq.service.FaqService;
import org.springframework.stereotype.Component;

/**
 * Project : spring-petclinic
 * File    : FaqDataInit.java
 * Created : 2025-11-14
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: FAQ 초기 데이터 생성
 *   연관 기능:
 *     - 애플리케이션 시작 시 샘플 FAQ 데이터 자동 생성
 */
@Component
public class FaqDataInit implements ApplicationRunner {

    private final FaqService faqService;

    public FaqDataInit(FaqService faqService) {
        this.faqService = faqService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 기존 FAQ가 없을 경우에만 샘플 데이터 생성
            if (faqService.getAllFaqs().isEmpty()) {
                createSampleFaqs();
            }
        } catch (Exception e) {
            // 데이터 초기화 실패 시 로그만 남기고 애플리케이션은 정상 기동
            System.err.println("FAQ 초기 데이터 생성 실패: " + e.getMessage());
        }
    }

    private void createSampleFaqs() {
        // 일반 카테고리
        faqService.createFaq(
                "동물병원 운영 시간은 어떻게 되나요?",
                "<p>평일(월~금): 오전 9시 ~ 오후 8시</p>" +
                "<p>토요일: 오전 9시 ~ 오후 6시</p>" +
                "<p>일요일/공휴일: 오전 10시 ~ 오후 4시</p>" +
                "<p>※ 점심시간: 오후 1시 ~ 2시</p>",
                "일반", 1
        );

        faqService.createFaq(
                "주차는 가능한가요?",
                "<p>네, 병원 건물 지하에 무료 주차장이 있습니다.</p>" +
                "<p>주차 공간이 협소하오니 가급적 대중교통을 이용해 주시면 감사하겠습니다.</p>",
                "일반", 2
        );

        // 예약 카테고리
        faqService.createFaq(
                "진료 예약은 어떻게 하나요?",
                "<p>진료 예약은 다음 방법으로 가능합니다:</p>" +
                "<ul>" +
                "<li>전화 예약: 02-XXXX-XXXX</li>" +
                "<li>온라인 예약: 홈페이지 온라인상담 게시판 이용</li>" +
                "<li>방문 예약: 병원 접수처에서 직접 예약</li>" +
                "</ul>" +
                "<p>※ 응급 상황은 예약 없이 바로 내원하셔도 됩니다.</p>",
                "예약", 3
        );

        faqService.createFaq(
                "예약 취소나 변경은 어떻게 하나요?",
                "<p>예약 취소나 변경은 병원에 전화(02-XXXX-XXXX)로 연락 주시면 됩니다.</p>" +
                "<p>가급적 예약 시간 24시간 전에 연락 주시면 다른 보호자분들께 도움이 됩니다.</p>",
                "예약", 4
        );

        // 진료 카테고리
        faqService.createFaq(
                "처음 방문 시 준비물은 무엇인가요?",
                "<p>초진 시 다음 사항을 준비해 주세요:</p>" +
                "<ul>" +
                "<li>보호자 신분증</li>" +
                "<li>기존 진료 기록 (있는 경우)</li>" +
                "<li>예방접종 수첩 (있는 경우)</li>" +
                "<li>현재 복용 중인 약 정보</li>" +
                "</ul>",
                "진료", 5
        );

        faqService.createFaq(
                "야간 응급진료도 가능한가요?",
                "<p>일반 진료 시간 외 응급 상황 발생 시:</p>" +
                "<p>1. 병원 응급 연락처(010-XXXX-XXXX)로 먼저 연락 주세요.</p>" +
                "<p>2. 당직 수의사가 상황을 파악하고 응급처치 방법을 안내해 드립니다.</p>" +
                "<p>3. 필요시 야간 응급동물병원으로 안내해 드립니다.</p>",
                "진료", 6
        );

        // 수술 카테고리
        faqService.createFaq(
                "중성화 수술 비용은 어느 정도인가요?",
                "<p>중성화 수술 비용은 동물의 종류, 크기, 성별에 따라 다릅니다:</p>" +
                "<ul>" +
                "<li>고양이 (암컷): 약 15~20만원</li>" +
                "<li>고양이 (수컷): 약 10~15만원</li>" +
                "<li>소형견 (암컷): 약 20~30만원</li>" +
                "<li>소형견 (수컷): 약 15~20만원</li>" +
                "</ul>" +
                "<p>※ 정확한 비용은 진료 후 안내해 드립니다.</p>",
                "수술", 7
        );

        faqService.createFaq(
                "수술 전 금식이 필요한가요?",
                "<p>네, 전신마취가 필요한 수술의 경우 금식이 필수입니다:</p>" +
                "<p>- 수술 전날 저녁 9시 이후 금식</p>" +
                "<p>- 수술 당일 아침 물도 금지</p>" +
                "<p>※ 금식을 하지 않으면 마취 중 구토로 인한 위험이 있어 수술이 연기될 수 있습니다.</p>",
                "수술", 8
        );

        // 기타 카테고리
        faqService.createFaq(
                "진료비 카드 결제가 가능한가요?",
                "<p>네, 모든 종류의 신용카드와 체크카드 결제가 가능합니다.</p>" +
                "<p>현금 결제 시 현금영수증도 발급해 드립니다.</p>" +
                "<p>※ 카카오페이, 네이버페이 등 간편결제도 지원합니다.</p>",
                "기타", 9
        );

        faqService.createFaq(
                "반려동물 보험 적용이 되나요?",
                "<p>네, 대부분의 반려동물 보험사와 제휴되어 있습니다:</p>" +
                "<ul>" +
                "<li>DB손해보험</li>" +
                "<li>현대해상</li>" +
                "<li>KB손해보험</li>" +
                "<li>메리츠화재</li>" +
                "</ul>" +
                "<p>보험 청구를 위한 진료확인서, 진료비 영수증을 발급해 드립니다.</p>",
                "기타", 10
        );

        faqService.createFaq(
                "예방접종은 언제 받아야 하나요?",
                "<p>강아지 예방접종 시기:</p>" +
                "<ul>" +
                "<li>1차: 생후 6~8주</li>" +
                "<li>2차: 1차 접종 후 3~4주</li>" +
                "<li>3차: 2차 접종 후 3~4주</li>" +
                "<li>추가접종: 매년 1회</li>" +
                "</ul>" +
                "<p>고양이도 유사한 일정으로 진행됩니다. 자세한 일정은 초진 시 상담해 드립니다.</p>",
                "진료", 11
        );
    }
}

