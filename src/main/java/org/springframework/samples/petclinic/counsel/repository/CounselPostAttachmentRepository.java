package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.counsel.table.CounselPostAttachment;

/**
 * @author Jeongmin Lee
 * @description CounselPostAttachment 엔티티에 대한 데이터 접근 리포지토리.
 * 이 리포지토리는 게시글과 첨부파일의 관계 데이터 CRUD 연산을 담당합니다.
 * 아직 구현되지 않은 기능:
 * - 특정 첨부파일과 연결된 모든 게시글 조회
 */
public interface CounselPostAttachmentRepository extends JpaRepository<CounselPostAttachment, Integer> {
}

