package org.springframework.samples.petclinic.faq.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.faq.repository.FaqPostRepository;
import org.springframework.samples.petclinic.faq.table.FaqPost;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Project : spring-petclinic
 * File    : FaqService.java
 * Created : 2025-11-14
 * Author  : Jeongmin Lee
 *
 * Description :
 *   자주묻는질문(FAQ) 게시판 비즈니스 로직 Service
 *
 * Purpose (만든 이유):
 *   1. FAQ 데이터의 비즈니스 로직을 중앙 집중화
 *   2. 카테고리 기반 필터링 제공 (일반, 진료, 예약, 수술, 기타)
 *   3. 키워드 검색 기능 (질문/답변)
 *   4. 노출 순서 관리 (displayOrder)
 *   5. Soft Delete 정책 적용
 *
 * Key Features (주요 기능):
 *   - FAQ CRUD (생성, 조회, 수정, 삭제 - Soft Delete)
 *   - 카테고리 필터링 (category 필드 기반)
 *   - 키워드 검색 (질문만 / 질문+답변)
 *   - 페이징 처리 (Stream 기반 메모리 페이징)
 *   - 노출 순서 정렬 (displayOrder → createdAt)
 *   - Soft Delete (del_flag = true)
 *
 * Business Rules (비즈니스 규칙):
 *   - FAQ는 관리자만 작성/수정/삭제 가능
 *   - 삭제된 FAQ는 조회되지 않음 (del_flag = false만)
 *   - 노출 순서는 displayOrder → createdAt 내림차순
 *   - 검색 시 대소문자 구분 없음 (toLowerCase)
 *
 * Search Types (검색 타입):
 *   - "question": 질문(제목)만 검색
 *   - "all" 또는 기타: 질문 + 답변 모두 검색
 *
 * Category Types (카테고리):
 *   - "일반": 일반적인 질문
 *   - "진료": 진료 관련
 *   - "예약": 예약 관련
 *   - "수술": 수술 관련
 *   - "기타": 기타 질문
 *
 * Performance Note (성능 고려사항):
 *   - 현재는 Stream 기반 메모리 필터링 (전체 데이터 로드 후 필터)
 *   - 데이터가 많아지면 QueryDSL로 DB 레벨 필터링 권장
 *   - FAQ는 일반적으로 데이터 수가 적어 현재 방식으로 충분
 *
 * Usage Examples (사용 예시):
 *   // 모든 FAQ 조회
 *   List<FaqPost> all = faqService.getAllFaqs();
 *
 *   // 카테고리 필터링
 *   List<FaqPost> faqs = faqService.searchFaqs(null, "진료", "all");
 *
 *   // 키워드 검색 (페이징)
 *   Page<FaqPost> results = faqService.searchFaqsWithPaging("예약", null, "all", pageable);
 *
 *   // FAQ 등록 (관리자)
 *   FaqPost created = faqService.createFaq("질문", "답변", "일반", 1);
 *
 *   // FAQ 삭제 (Soft Delete)
 *   faqService.softDeleteFaq(id);
 *
 * Transaction Management (트랜잭션 관리):
 *   - @Transactional 클래스 레벨 적용
 *   - 모든 public 메서드가 트랜잭션 내에서 실행
 *
 * Dependencies (의존 관계):
 *   - FaqPostRepository: DB 접근
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Service
@Transactional
public class FaqService {

    private final FaqPostRepository faqPostRepository;

    public FaqService(FaqPostRepository faqPostRepository) {
        this.faqPostRepository = faqPostRepository;
    }

    /**
     * 모든 FAQ 조회 (삭제되지 않은 것만)
     */
    public List<FaqPost> getAllFaqs() {
        return faqPostRepository.findByDelFlagFalseOrderByDisplayOrderAscCreatedAtDesc();
    }

    /**
     * FAQ 검색 (키워드, 카테고리, 타입 필터링)
     * @param keyword 검색 키워드
     * @param category 카테고리 필터
     * @param type 검색 타입 ("question": 제목만, "all": 제목+내용)
     */
    public List<FaqPost> searchFaqs(String keyword, String category, String type) {
        List<FaqPost> faqs = getAllFaqs();

        // 카테고리 필터
        if (category != null && !category.isBlank()) {
            faqs = faqs.stream()
                    .filter(f -> category.equals(f.getCategory()))
                    .collect(Collectors.toList());
        }

        // 키워드 검색
        if (keyword != null && !keyword.isBlank()) {
            String lowerKeyword = keyword.toLowerCase();

            if ("question".equals(type)) {
                // 제목(질문)만 검색
                faqs = faqs.stream()
                        .filter(f -> f.getQuestion().toLowerCase().contains(lowerKeyword))
                        .collect(Collectors.toList());
            } else {
                // 제목+내용(질문+답변) 검색
                faqs = faqs.stream()
                        .filter(f -> f.getQuestion().toLowerCase().contains(lowerKeyword)
                                  || f.getAnswer().toLowerCase().contains(lowerKeyword))
                        .collect(Collectors.toList());
            }
        }

        return faqs;
    }

    /**
     * FAQ 검색 with 페이징
     */
    public Page<FaqPost> searchFaqsWithPaging(String keyword, String category, String type, Pageable pageable) {
        List<FaqPost> filteredFaqs = searchFaqs(keyword, category, type);

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredFaqs.size());

        List<FaqPost> pagedFaqs = filteredFaqs.subList(start, end);

        return new PageImpl<>(pagedFaqs, pageable, filteredFaqs.size());
    }

    /**
     * FAQ 단건 조회
     */
    public FaqPost getFaq(Long id) {
        return faqPostRepository.findById(id)
                .filter(f -> !f.isDelFlag())
                .orElseThrow(() -> new IllegalArgumentException("FAQ not found: " + id));
    }

    /**
     * FAQ 등록
     */
    public FaqPost createFaq(String question, String answer, String category, Integer displayOrder) {
        FaqPost post = new FaqPost();
        post.setQuestion(question);
        post.setAnswer(answer);
        post.setCategory(category);
        post.setDisplayOrder(displayOrder);
        post.setCreatedAt(LocalDateTime.now());
        return faqPostRepository.save(post);
    }

    /**
     * FAQ 수정
     */
    public FaqPost updateFaq(Long id, String question, String answer, String category, Integer displayOrder) {
        FaqPost post = getFaq(id);
        post.setQuestion(question);
        post.setAnswer(answer);
        post.setCategory(category);
        post.setDisplayOrder(displayOrder);
        post.setUpdatedAt(LocalDateTime.now());
        return faqPostRepository.save(post);
    }

    /**
     * FAQ 소프트 삭제
     */
    public void softDeleteFaq(Long id) {
        FaqPost post = faqPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FAQ not found: " + id));
        post.setDelFlag(true);
        post.setUpdatedAt(LocalDateTime.now());
        faqPostRepository.save(post);
    }
}

