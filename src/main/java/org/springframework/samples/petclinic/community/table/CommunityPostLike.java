package org.springframework.samples.petclinic.community.table;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ConstraintMode;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Project : spring-petclinic
 * File    : CommunityPostLike.java
 * Created : 2025-11-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   커뮤니티(공지사항) 게시글 좋아요 테이블
 *
 * Purpose (만든 이유):
 *   1. 사용자가 공지사항 게시글에 좋아요를 누를 수 있는 기능
 *   2. 로그인 사용자만 좋아요 가능
 *   3. 중복 좋아요 방지 (UNIQUE 제약조건)
 *   4. 좋아요 취소 기능 (레코드 삭제)
 *
 * Key Features (주요 기능):
 *   - 게시글당 사용자당 1개 좋아요만 가능
 *   - 생성일자 자동 기록
 *   - CommunityPost와 ManyToOne 연관관계
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(
	name = "community_post_likes",
	uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "username"})
)
public class CommunityPostLike {

	/**
	 * 좋아요 ID (Primary Key)
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * 게시글 (ManyToOne)
	 * - 한 게시글에 여러 좋아요 가능
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "post_id", nullable = false)
	private CommunityPost post;

	/**
	 * 사용자 아이디 (로그인 username)
	 * - User 엔티티와 연관관계 추가 전까지는 String으로 관리
	 * - TODO: User 엔티티 추가 시 @ManyToOne으로 변경
	 */
	@Column(name = "username", nullable = false, length = 50)
	private String username;

	/**
	 * 생성일자 (좋아요 누른 시간)
	 */
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	// Constructors
	public CommunityPostLike() {
	}

	public CommunityPostLike(CommunityPost post, String username) {
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

	public CommunityPost getPost() {
		return post;
	}

	public void setPost(CommunityPost post) {
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
}
