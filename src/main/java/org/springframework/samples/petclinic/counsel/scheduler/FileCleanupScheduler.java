package org.springframework.samples.petclinic.counsel.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.counsel.model.Attachment;
import org.springframework.samples.petclinic.counsel.repository.AttachmentRepository;
import org.springframework.samples.petclinic.counsel.service.FileStorageService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Jeongmin Lee
 * @description 주기적으로 오래된 파일을 정리하는 스케줄러 클래스.
 * 이 클래스는 논리적으로 삭제된 지 2주 이상 지난 첨부파일을 물리적으로 삭제합니다.
 * 아직 구현되지 않은 기능:
 * - 스케줄러 실행 결과 관리자 알림
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
     * 매일 자정에 실행되어, 삭제된 지 2주 이상 된 파일을 물리적으로 삭제합니다.
     * cron = "0 0 0 * * ?" (매일 0시 0분 0초)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupOldFiles() {
        log.info("Starting scheduled cleanup of old files...");
        try {
            LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
            List<Attachment> filesToDelete = attachmentRepository.findByDelFlagTrueAndDeletedAtBefore(twoWeeksAgo);

            if (filesToDelete.isEmpty()) {
                log.info("No old files to delete.");
                return;
            }

            log.info("Found {} files to delete.", filesToDelete.size());

            for (Attachment attachment : filesToDelete) {
                try {
                    fileStorageService.deleteFile(attachment.getFilePath());
                    attachmentRepository.delete(attachment); // DB에서 최종 삭제
                    log.info("Successfully deleted attachment record and physical file: {}", attachment.getOriginalFileName());
                } catch (Exception e) {
                    log.error("Error during deletion of attachment ID {}: {}", attachment.getId(), e.getMessage());
                    // 개별 파일 삭제 실패가 전체 스케줄링 작업을 중단시키지 않도록 처리
                }
            }
            log.info("Finished scheduled cleanup of old files.");
        } catch (Exception e) {
            log.error("An unexpected error occurred during the file cleanup schedule.", e);
        }
    }
}

