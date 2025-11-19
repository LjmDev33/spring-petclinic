package org.springframework.samples.petclinic.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/*
 * Project : spring-petclinic
 * File    : PageResponse.java
 * Created : 2025-09-29
 * Author  : Jeongmin Lee
 *
 * Description :
 *   TODO: 공용 페이징 처리 클래스
 *
 * License :
 *   Copyright (c) 2025 AOF(AllForOne) / All rights reserved.
 */
public class PageResponse<T> {
	private List<T> content;
	private int totalPages;
	private int currentPage;
	private long totalElements;
	private int size;

	public PageResponse(Page<T> page) {
		this.content = page.getContent();
		this.totalPages = page.getTotalPages();
		this.currentPage = page.getNumber();
		this.totalElements = page.getTotalElements();
		this.size = page.getSize();
	}

	public List<T> getContent() { return content; }
	public void setContent(List<T> content) { this.content = content; }

	public int getTotalPages() { return totalPages; }
	public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

	public int getCurrentPage() { return currentPage; }
	public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

	/**
	 * Spring Data Page 인터페이스 호환성을 위한 getter
	 * pagination fragment에서 page.number로 접근 가능
	 */
	public int getNumber() { return currentPage; }

	public long getTotalElements() { return totalElements; }
	public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

	public int getSize() { return size; }
	public void setSize(int size) { this.size = size; }
}
