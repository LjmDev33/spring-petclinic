package org.springframework.samples.petclinic.counsel.table;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Where;
import org.springframework.samples.petclinic.common.entity.BaseEntity;
import org.springframework.samples.petclinic.common.table.Attachment;

import java.time.LocalDateTime;

/*
 * Project : spring-petclinic
 * File    : CounselPostAttachment.java
 * Created : 2025-10-23
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 온라인상담 게시글 첨부 테이블
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "counsel_post_attachment")
@SQLDelete(sql = "UPDATE counsel_post_attachment SET del_flag=1, deleted_at=NOW() WHERE counsel_post_id=? AND attachment_id=?")
@SQLRestriction("del_flag = 0")
public class CounselPostAttachment {

	@EmbeddedId
	private CounselPostAttachmentId id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("counselPostId")
	@JoinColumn(name = "counsel_post_id", nullable = false)
	private CounselPost post;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@MapsId("attachmentId")
	@JoinColumn(name = "attachment_id", nullable = false)
	private Attachment attachment;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder = 0;

	@Column(name = "del_flag", nullable = false)
	private boolean delFlag = false;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Column(name = "deleted_by", length = 60)
	private String deletedBy;

	public CounselPostAttachmentId getId() {
		return id;
	}

	public void setId(CounselPostAttachmentId id) {
		this.id = id;
	}

	public CounselPost getPost() {
		return post;
	}

	public void setPost(CounselPost post) {
		this.post = post;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
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
}
