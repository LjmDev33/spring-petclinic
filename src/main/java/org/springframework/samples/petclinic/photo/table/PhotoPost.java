package org.springframework.samples.petclinic.photo.table;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.samples.petclinic.common.entity.BaseEntity;
import org.springframework.samples.petclinic.user.table.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Project : spring-petclinic
 * File    : PhotoPost.java
 * Created : 2025-11-25
 * Author  : Jeongmin Lee
 *
 * Description :
 *   포토게시판 게시글 Entity
 *   이미지 중심의 갤러리형 게시판
 *
 * Features:
 *   - 이미지 썸네일 지원
 *   - 다중 이미지 첨부
 *   - 조회수, 좋아요 수 관리
 *   - Soft Delete (del_flag)
 */
@Entity
@jakarta.persistence.Table(name = "photo_post",
	indexes = {
		@jakarta.persistence.Index(name = "idx_photo_created", columnList = "created_at DESC"),
		@jakarta.persistence.Index(name = "idx_photo_author", columnList = "author")
	})
@SQLDelete(sql = "UPDATE photo_post SET del_flag=1, deleted_at=NOW() WHERE id=?")
@SQLRestriction("del_flag = 0")
public class PhotoPost extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "author_id")
	private User user;

	/** 게시글 제목 */
	@Column(nullable = false, length = 200)
	private String title;

	/** 게시글 내용 */
	@Lob
	@Column(columnDefinition = "TEXT")
	private String content;

	/** 작성자 */
	@Column(nullable = false, length = 100)
	private String author;

	/** 썸네일 이미지 URL (대표 이미지) */
	@Column(length = 500)
	private String thumbnailUrl;

	/** 조회수 */
	@Column(nullable = false)
	private int viewCount = 0;

	/** 좋아요 수 */
	@Column(nullable = false)
	private int likeCount = 0;

	/** 생성 일시 */
	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	/** 수정 일시 */
	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	/** 삭제 플래그 (Soft Delete) */
	@Column(name = "del_flag", nullable = false)
	private boolean delFlag = false;

	/** 삭제 일시 */
	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	/** 삭제한 사용자 */
	@Column(name = "deleted_by", length = 60)
	private String deletedBy;

	/** 첨부파일 목록 (Phase 3) */
	@OneToMany(mappedBy = "photoPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<PhotoPostAttachment> attachments = new ArrayList<>();

	// Getter & Setter

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

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
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

	/** Phase 3: 첨부파일 목록 조회 */
	public List<PhotoPostAttachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<PhotoPostAttachment> attachments) {
		this.attachments = attachments;
	}

	/** Phase 3: 첨부파일 추가 편의 메서드 */
	public void addAttachment(PhotoPostAttachment attachment) {
		this.attachments.add(attachment);
		attachment.setPhotoPost(this);
	}

	/** Phase 3: 첨부파일 제거 편의 메서드 */
	public void removeAttachment(PhotoPostAttachment attachment) {
		this.attachments.remove(attachment);
		attachment.setPhotoPost(null);
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		if(user != null){
			this.author = user.getNickname();
		}
	}
}

