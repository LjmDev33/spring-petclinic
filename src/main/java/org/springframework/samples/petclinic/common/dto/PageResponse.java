package org.springframework.samples.petclinic.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Project : spring-petclinic
 * File    : PageResponse.java
 * Created : 2025-09-29
 * Author  : Jeongmin Lee
 *
 * Description :
 *   공용 페이징 응답 DTO (모든 도메인의 목록 조회에서 사용)
 *
 * Purpose (만든 이유):
 *   1. Spring Data의 Page 객체를 프론트엔드 친화적인 DTO로 변환
 *   2. 모든 게시판 목록에서 일관된 페이징 응답 형식 제공
 *   3. Thymeleaf 템플릿에서 쉽게 접근 가능한 속성 제공
 *   4. JSON API 응답에서도 사용 가능한 표준 형식
 *
 * Key Features (주요 기능):
 *   - Spring Data Page 객체를 래핑하여 필요한 정보만 노출
 *   - 현재 페이지, 전체 페이지 수, 전체 요소 수, 페이지 크기 제공
 *   - Thymeleaf pagination fragment와 호환 (page.number 접근 가능)
 *   - 제네릭 타입으로 모든 DTO 타입 지원
 *
 * Wrapped Information (포함 정보):
 *   - content: 현재 페이지의 데이터 목록 (List<T>)
 *   - totalPages: 전체 페이지 수
 *   - currentPage: 현재 페이지 번호 (0부터 시작)
 *   - totalElements: 전체 데이터 수
 *   - size: 한 페이지당 데이터 수
 *
 * Usage Examples (사용 예시):
 *   // Service에서 생성
 *   Page<Post> page = repository.findAll(pageable);
 *   PageResponse<PostDto> response = new PageResponse<>(page);
 *
 *   // Controller에서 Model에 추가
 *   model.addAttribute("page", response);
 *
 *   // Thymeleaf에서 사용
 *   <div th:each="post : ${page.content}">...</div>
 *   <span th:text="${page.totalPages}">10</span>
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
