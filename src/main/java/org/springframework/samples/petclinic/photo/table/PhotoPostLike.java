package org.springframework.samples.petclinic.photo.table;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Project : spring-petclinic
 * File    : PhotoPostLike.java
 * Created : 2025-11-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 게시글 좋아요 Entity (Phase 2-3)
 *
 * Purpose (만든 이유):
 *   1. 포토게시판 게시글에 대한 좋아요 기능 제공
 *   2. 로그인 사용자만 좋아요 가능
 *   3. 한 사용자당 한 게시글에 1개의 좋아요만 가능 (UNIQUE 제약)
 *   4. 좋아요 취소 시 레코드 삭제 (토글 방식)
 *
 * Key Features (주요 기능):
 *   - 게시글과 사용자의 M:N 관계 표현
 *   - UNIQUE 제약으로 중복 좋아요 방지
 *   - CASCADE 삭제로 게시글 삭제 시 연관 좋아요도 삭제
 *   - 좋아요 생성 시간 자동 기록 (@CreationTimestamp)
 *
 * Business Rules (비즈니스 규칙):
 *   - 로그인 사용자만 좋아요 가능
 *   - 중복 좋아요 불가 (post_id, username 조합 UNIQUE)
 *   - 좋아요 취소는 레코드 삭제로 처리 (Soft Delete 미적용)
 *
 * Database Schema:
 *   - Table: photo_post_likes
 *   - Columns: id, post_id, username, created_at
 *   - Indexes: PK(id), UNIQUE(post_id, username), FK(post_id)
 *
 * Usage Examples (사용 예시):
 *   // 좋아요 추가
 *   PhotoPostLike like = new PhotoPostLike(post, "user01");
 *   likeRepository.save(like);
 *
 *   // 좋아요 조회
 *   boolean exists = likeRepository.existsByPostIdAndUsername(postId, username);
 *
 *   // 좋아요 개수
 *   long count = likeRepository.countByPostId(postId);
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "photo_post_likes",
	uniqueConstraints = @UniqueConstraint(
		name = "uk_photo_post_likes_post_username",
		columnNames = {"post_id", "username"}
	),
	indexes = {
		@Index(name = "idx_photo_post_likes_post", columnList = "post_id"),
		@Index(name = "idx_photo_post_likes_username", columnList = "username")
	}
)
public class PhotoPostLike {

	/** 좋아요 고유 ID */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 게시글 (ManyToOne 관계) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private PhotoPost post;

	/** 좋아요를 누른 사용자 아이디 */
	@Column(name = "username", nullable = false, length = 50)
	private String username;

	/** 좋아요 생성 시간 (자동 설정) */
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	// Constructors

	/**
	 * 기본 생성자 (JPA용)
	 */
	protected PhotoPostLike() {
	}

	/**
	 * 좋아요 생성자
	 *
	 * @param post 게시글
	 * @param username 사용자 아이디
	 */
	public PhotoPostLike(PhotoPost post, String username) {
		this.post = post;
		this.username = username;
	}

	// Getters and Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PhotoPost getPost() {
		return post;
	}

	public void setPost(PhotoPost post) {
		this.post = post;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	// Utility Methods

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PhotoPostLike)) return false;
		PhotoPostLike that = (PhotoPostLike) o;
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public String toString() {
		return "PhotoPostLike{" +
			"id=" + id +
			", postId=" + (post != null ? post.getId() : null) +
			", username='" + username + '\'' +
			", createdAt=" + createdAt +
			'}';
	}
}
