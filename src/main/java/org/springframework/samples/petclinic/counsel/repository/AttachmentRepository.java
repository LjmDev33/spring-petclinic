package org.springframework.samples.petclinic.counsel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.petclinic.counsel.model.Attachment;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Jeongmin Lee
 * @description Attachment 엔티티에 대한 데이터 접근 리포지토리.
 * 이 리포지토리는 첨부파일 데이터의 기본적인 CRUD 연산을 담당합니다.
 * 아직 구현되지 않은 기능:
 * - 특정 기간 동안 업로드된 파일 목록 조회
 */
public interface AttachmentRepository extends JpaRepository<Attachment, Integer> {

    /**
     * 특정 시간 이전에 논리적으로 삭제된 첨부파일 목록을 조회합니다.
     * @param cutoffTime 기준 시간
     * @return 삭제된 첨부파일 목록
     */
    List<Attachment> findByDelFlagTrueAndDeletedAtBefore(LocalDateTime cutoffTime);
}
