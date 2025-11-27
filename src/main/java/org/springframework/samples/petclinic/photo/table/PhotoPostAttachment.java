package org.springframework.samples.petclinic.photo.table;

import jakarta.persistence.*;
import org.springframework.samples.petclinic.common.entity.BaseEntity;
import org.springframework.samples.petclinic.common.table.Attachment;

/**
 * Project : spring-petclinic
 * File    : PhotoPostAttachment.java
 * Created : 2025-11-27
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 게시글과 첨부파일 간의 중간 테이블
 *   다대다(Many-to-Many) 관계를 일대다/다대일로 풀어낸 구조
 *
 * Purpose (만든 이유):
 *   - 게시글 하나에 여러 이미지 첨부 지원
 *   - Attachment 테이블 공용화
 *   - 첨부파일 순서 관리 가능
 */
@Entity
@Table(name = "photo_post_attachment",
	uniqueConstraints = @UniqueConstraint(
		name = "uq_photo_post_attachment",
		columnNames = {"photo_post_id", "attachment_id"}
	))
public class PhotoPostAttachment extends BaseEntity {

	/** 포토 게시글 (다대일) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "photo_post_id", nullable = false)
	private PhotoPost photoPost;

	/** 첨부파일 (다대일) */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "attachment_id", nullable = false)
	private Attachment attachment;

	/** 기본 생성자 (JPA 필수) */
	protected PhotoPostAttachment() {}

	/** 편의 생성자 */
	public PhotoPostAttachment(PhotoPost photoPost, Attachment attachment) {
		this.photoPost = photoPost;
		this.attachment = attachment;
	}

	// Getters and Setters
	public PhotoPost getPhotoPost() {
		return photoPost;
	}

	public void setPhotoPost(PhotoPost photoPost) {
		this.photoPost = photoPost;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}
}

