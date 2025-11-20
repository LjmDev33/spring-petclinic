package org.springframework.samples.petclinic.faq.table;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Project : spring-petclinic
 * File    : FaqPost.java
 * Created : 2025-11-14
 * Author  : Jeongmin Lee
 *
 * Description :
 *   사용목적: 자주묻는질문(FAQ) 게시글 엔티티
 *   연관 기능:
 *     - FAQ 목록/상세 조회
 *     - FAQ 등록/수정/삭제(관리자)
 *   미구현 기능:
 *     - FAQ 조회수/클릭 통계 관리
 *     - FAQ 카테고리별 노출 순서 자동 조정
 */
@Entity
@Table(name = "`faq_posts`")
public class FaqPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")  // PK 컬럼명 명시
    private Long id;

    @Column(nullable = false, length = 200)
    private String question;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "del_flag", nullable = false)
    private boolean delFlag = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDelFlag() {
        return delFlag;
    }

    public void setDelFlag(boolean delFlag) {
        this.delFlag = delFlag;
    }
}

