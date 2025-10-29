package org.springframework.samples.petclinic.community.table;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;
import org.springframework.samples.petclinic.common.entity.BaseEntity;

import java.time.LocalDateTime;

/*
 * Project : spring-petclinic
 * File    : CommunityPostDto.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 커뮤니티 게시판 테이블
 *    	   @SQLDelete어노테이션에 @Where 어노테이션을 사용했었지만 6.3버전 이상부터는 deprecated되면서
 *         @SQLRestriction어노테이션으로 where조건 매핑해줌
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "community_post")
@SQLDelete(sql = "UPDATE community_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class CommunityPost extends BaseEntity {

	@Column(nullable = false, length = 255)
	private String title;

	@Lob
	@Column(columnDefinition = "TEXT", nullable = false)
	private String content;

	@Column(nullable = false, length = 100)
	private String author;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "view_count", nullable = false , columnDefinition = "INT DEFAULT 0")
	private int viewCount = 0;

	@Column(name = "like_count", nullable = false , columnDefinition = "INT DEFAULT 0")
	private int likeCount = 0;

	@Column(name = "del_flag", nullable = false)
	private boolean delFlag = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 60)
	private String deletedBy;

	// VARCHAR(1) : 'Y' / 'N'
	@Column(name = "attach_flag", nullable = false)
	private boolean attachFlag = false;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public boolean isDelFlag() {
		return delFlag;
	}

	public void setDelFlag(boolean delFlag) {
		this.delFlag = delFlag;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public String getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(String deletedBy) {
		this.deletedBy = deletedBy;
	}

	public boolean isAttachFlag() {
		return attachFlag;
	}

	public void setAttachFlag(boolean attachFlag) {
		this.attachFlag = attachFlag;
	}
}
