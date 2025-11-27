package org.springframework.samples.petclinic.community.table;

import jakarta.persistence.*;
import org.springframework.samples.petclinic.common.entity.BaseEntity;
import org.springframework.samples.petclinic.common.table.Attachment;

/**
 * Project : spring-petclinic
 * File    : CommunityPostAttachment.java
 * Created : 2025-11-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   Community 게시글과 Attachment의 중간 테이블 Entity
 *   - ManyToOne 관계로 CommunityPost와 Attachment 연결
 *   - Phase 3: 게시글 첨부파일 관리 기능
 *
 * Purpose (만든 이유):
 *   1. 게시글과 첨부파일의 다대다 관계를 명시적으로 관리
 *   2. 추후 첨부파일별 순서, 설명 등 추가 정보를 저장할 수 있도록 확장 가능
 *   3. JPA의 OneToMany/ManyToOne 양방향 관계를 통해 편리한 조회
 *
 * Related Features (연관 기능):
 *   - Community 게시글 작성 시 파일 첨부
 *   - Community 게시글 수정 시 파일 추가/삭제
 *   - Community 게시글 삭제 시 첨부파일 Soft Delete
 *
 * Note:
 *   - CommunityPost 삭제 시 cascade로 자동 삭제됨 (orphanRemoval = true)
 *   - Attachment는 공통 테이블이므로 별도 Soft Delete 처리 필요
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "community_post_attachment",
	uniqueConstraints = @UniqueConstraint(
		name = "uq_community_post_attachment",
		columnNames = {"community_post_id", "attachment_id"}
	))
public class CommunityPostAttachment extends BaseEntity {

	/** Community 게시글 (다대일) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "community_post_id", nullable = false)
	private CommunityPost communityPost;

	/** 첨부파일 (다대일) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "attachment_id", nullable = false)
	private Attachment attachment;

	// 기본 생성자
	public CommunityPostAttachment() {
	}

	// 편의 생성자
	public CommunityPostAttachment(CommunityPost communityPost, Attachment attachment) {
		this.communityPost = communityPost;
		this.attachment = attachment;
	}

	// Getters and Setters

	public CommunityPost getCommunityPost() {
		return communityPost;
	}

	public void setCommunityPost(CommunityPost communityPost) {
		this.communityPost = communityPost;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}
}

