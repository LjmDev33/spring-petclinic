package org.springframework.samples.petclinic.common.dto;

/**
 * Project : spring-petclinic
 * File    : UploadResponse.java
 * Created : 2026-02-04
 * Author  : Jeongmin Lee
 *
 * Description :
 * 파일 업로드 결과 반환용 DTO
 * - 에디터 이미지 URL 및 첨부파일 메타데이터 포함
 * - Lombok 미사용 (보안 정책 준수)
 */
public class UploadResponse {

	private Long id;              // 첨부파일 ID (게시글 연결 및 식별용)
	private String fileName;      // 원본 파일명 (사용자 표시용)
	private String url;           // 접근 URL (에디터 이미지 src용)
	private long size;            // 파일 크기 (Byte)

	// 기본 생성자
	public UploadResponse() {
	}

	// 전체 필드 생성자
	public UploadResponse(Long id, String fileName, String url, long size) {
		this.id = id;
		this.fileName = fileName;
		this.url = url;
		this.size = size;
	}

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "UploadResponse{" +
			"id=" + id +
			", fileName='" + fileName + '\'' +
			", url='" + url + '\'' +
			", size=" + size +
			'}';
	}
}
