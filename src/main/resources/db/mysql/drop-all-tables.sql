-- ========================================
-- PetClinic 데이터베이스 초기화 스크립트
-- 개발 환경 전용 (외래키 제약조건 무시)
-- ========================================

SET FOREIGN_KEY_CHECKS = 0;

-- 테이블 삭제 (역순)
DROP TABLE IF EXISTS `counsel_comment_attachment`;
DROP TABLE IF EXISTS `counsel_post_attachment`;
DROP TABLE IF EXISTS `community_post_attachment`;
DROP TABLE IF EXISTS `user_roles`;
DROP TABLE IF EXISTS `persistent_logins`;
DROP TABLE IF EXISTS `counsel_comment`;
DROP TABLE IF EXISTS `counsel_post`;
DROP TABLE IF EXISTS `community_post`;
DROP TABLE IF EXISTS `system_config`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `counsel_attachments`;
DROP TABLE IF EXISTS `attachment`;

SET FOREIGN_KEY_CHECKS = 1;

-- 참고: 테이블은 Hibernate가 자동 생성합니다.
-- 이 스크립트는 기존 데이터를 완전히 삭제하고 새로 시작할 때만 사용하세요.

