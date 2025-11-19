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
 *   사용목적: FAQ 게시판 비즈니스 로직 처리
 *   연관 기능:
 *     - FAQ 목록/상세 조회 (페이징, 검색, 필터링)
 *     - FAQ 등록/수정/삭제(관리자)
 *   미구현 기능:
 *     - FAQ 노출 순서 자동 조정
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

