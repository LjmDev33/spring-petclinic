package org.springframework.samples.petclinic.common.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.samples.petclinic.common.repository.AttachmentRepository;
import org.springframework.samples.petclinic.common.table.Attachment;
import org.springframework.samples.petclinic.counsel.service.FileStorageService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시스템 통합 청소 스케줄러
 * 1. 고아 파일 청소 (Orphan Cleanup): 글 작성 중 취소된 파일 정리
 * 2. 영구 삭제 (Hard Delete): 삭제된 지 2주 지난 파일 완전 삭제
 */
@Component
public class FileCleanupScheduler {

	private static final Logger log = LoggerFactory.getLogger(FileCleanupScheduler.class);
	private final AttachmentRepository attachmentRepository;
	private final FileStorageService fileStorageService;

	public FileCleanupScheduler(AttachmentRepository attachmentRepository, FileStorageService fileStorageService) {
		this.attachmentRepository = attachmentRepository;
		this.fileStorageService = fileStorageService;
	}

	/**
	 * [Task 1] 고아 파일 청소 (매일 새벽 3시)
	 * - 에디터에 업로드했으나 글을 저장하지 않은 파일
	 * - 물리 파일 삭제 -> DB Soft Delete 처리
	 */
	@Scheduled(cron = "0 0 3 * * *")
	@Transactional
	public void cleanupGlobalOrphanFiles() {
		log.info("🧹 [Task 1] Starting orphan file cleanup...");
		LocalDateTime cutoffDate = LocalDateTime.now().minusHours(24); // 24시간 기준

		List<Attachment> orphanFiles = attachmentRepository.findGlobalOrphanFiles(cutoffDate);

		if (orphanFiles.isEmpty()) return;

		int count = 0;
		for (Attachment file : orphanFiles) {
			try {
				fileStorageService.deleteFile(file.getStoredFilename()); // 물리 삭제

				file.setDelFlag(true); // 소프트 삭제 처리
				file.setDeletedAt(LocalDateTime.now());
				file.setDeletedBy("SYSTEM_ORPHAN_CLEANER_SCHEDULER");
				count++;
			} catch (Exception e) {
				log.error("Failed to process orphan file: {}", file.getId(), e);
			}
		}
		log.info("🗑️ [Task 1] Orphan cleanup finished. Processed {} files.", count);
	}

	/**
	 * [Task 2] 삭제된 파일 완전 영구 삭제 (매일 새벽 1시)
	 * - 사용자가 삭제하거나 Task 1에 의해 삭제된 지 2주 지난 파일
	 * - DB Row 완전 삭제 (물리 파일이 남아있다면 삭제 시도)
	 */
	@Scheduled(cron = "0 0 1 * * *")
	@Transactional
	public void purgeOldDeletedFiles() {
		log.info("🔥 [Task 2] Starting permanent purge of old files...");
		LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

		List<Attachment> filesToDelete = attachmentRepository.findByDelFlagTrueAndDeletedAtBefore(twoWeeksAgo);

		if (filesToDelete.isEmpty()) {
			log.info("✨ [Task 2] No old files found to purge.");
			return;
		}

		int count = 0;
		for (Attachment attachment : filesToDelete) {
			try {
				// 1. 물리 파일 삭제 시도 및 결과 확인
				boolean isPhysicalDeleted = fileStorageService.deleteFile(attachment.getStoredFilename());

				if (isPhysicalDeleted) {
					log.info("✅ [Task 2] Physical file deleted: {}", attachment.getOriginalFilename());
				} else {
					// 이미 없는 경우 (Task 1에 의해 지워졌거나 직접 지워짐) -> 에러 아님, 정보성 로그
					log.info("ℹ️ [Task 2] Physical file already missing (skipped): {}", attachment.getOriginalFilename());
				}

				// 2. DB 영구 삭제
				attachmentRepository.delete(attachment);
				count++;

			} catch (Exception e) {
				// 진짜 DB 에러나 예상치 못한 예외만 Error 레벨로 기록
				log.error("❌ Error purging attachment ID {}: {}", attachment.getId(), e.getMessage());
			}
		}
		log.info("💀 [Task 2] Permanent purge finished. Processed {} records.", count);
	}
}
