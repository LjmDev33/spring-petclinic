package org.springframework.samples.petclinic.community;

import jakarta.persistence.*;
import org.springframework.samples.petclinic.common.entity.BaseEntity;

import java.time.LocalDateTime;

/*
 * Project : spring-petclinic
 * File    : CommunityPostDto.java
 * Created : 2025-09-26
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: Add class description here.
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
@Entity
@Table(name = "community_post")
public class CommunityPost extends BaseEntity {

    private String title;

    @Column(length = 2000)
    private String content;

    private String author;

    private LocalDateTime createdAt;

	private int viewCount = 0;   // 조회수 기본 0
	private int likeCount = 0;   // 좋아요 기본 0
    // Getter & Setter
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public int getViewCount() {return viewCount;}
	public void setViewCount(int viewCount) {this.viewCount = viewCount;}
	public int getLikeCount() {return likeCount;}
	public void setLikeCount(int likeCount) {this.likeCount = likeCount;}
}
