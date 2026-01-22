/*
 * PetClinic Schema Initialization
 * Database: MySQL / MariaDB
 */

-- 1. 사용자 (Users) - 모든 연관관계의 핵심이므로 가장 먼저 생성
CREATE TABLE IF NOT EXISTS `users` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `name` varchar(50) NOT NULL,
  `nickname` varchar(30) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `profile_image_url` varchar(500) DEFAULT NULL,
  `enabled` varchar(255) NOT NULL,
  `accountNonExpired` varchar(255) NOT NULL,
  `accountNonLocked` varchar(255) NOT NULL,
  `credentialsNonExpired` varchar(255) NOT NULL,
  `last_login_ip` varchar(50) DEFAULT NULL,
  `last_login_at` datetime(6) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_users_username` (`username`),
  UNIQUE KEY `UK_users_nickname` (`nickname`)
  )

-- 2. 사용자 권한 (User Roles)
CREATE TABLE IF NOT EXISTS `user_roles` (
                                          `user_id` bigint NOT NULL,
                                          `role` varchar(255) DEFAULT NULL,
  KEY `IDX_user_roles_user_id` (`user_id`),
  CONSTRAINT `FK_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
  )

-- 3. 비밀번호 재설정 토큰 (Password Reset Tokens)
CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
                                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                                     `user_id` bigint NOT NULL,
                                                     `token` varchar(100) NOT NULL,
  `used` varchar(255) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pwd_token` (`token`),
  KEY `IDX_pwd_token_user_id` (`user_id`),
  CONSTRAINT `FK_pwd_token_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
  )

-- 4. 시스템 설정 (System Config) - 독립 테이블
CREATE TABLE IF NOT EXISTS `system_config` (
                                             `id` bigint NOT NULL AUTO_INCREMENT,
                                             `property_key` varchar(100) NOT NULL,
  `property_value` varchar(500) NOT NULL,
  `description` varchar(1000) DEFAULT NULL,
  `is_active` varchar(255) NOT NULL,
  `updated_by` varchar(100) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_system_config_key` (`property_key`)
  )

-- 5. 첨부파일 (Attachment) - 여러 게시판에서 참조
CREATE TABLE IF NOT EXISTS `attachment` (
                                          `id` bigint NOT NULL AUTO_INCREMENT,
                                          `original_filename` varchar(255) NOT NULL,
  `stored_filename` varchar(255) NOT NULL,
  `content_type` varchar(100) DEFAULT NULL,
  `file_size` bigint NOT NULL,
  `download_count` int NOT NULL,
  `del_flag` varchar(255) NOT NULL,
  `deleted_by` varchar(60) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_attachment_store_alive` (`stored_filename`,`del_flag`),
  KEY `IDX_attachment_created` (`created_at` DESC),
  KEY `IDX_attachment_del_flag` (`del_flag`)
  )

-- 6. FAQ 게시판 (Faq Posts) - 독립 테이블
CREATE TABLE IF NOT EXISTS `faq_posts` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `category` varchar(50) DEFAULT NULL,
  `question` varchar(200) NOT NULL,
  `answer` text NOT NULL,
  `display_order` int DEFAULT NULL,
  `del_flag` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
  )

-- ==========================================
-- 7. 커뮤니티 게시판 (Community Post)
-- ==========================================
CREATE TABLE IF NOT EXISTS `community_post` (
                                              `id` bigint NOT NULL AUTO_INCREMENT,
                                              `author_id` bigint DEFAULT NULL, -- Users FK
                                              `author` varchar(100) NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` text NOT NULL,
  `view_count` int NOT NULL,
  `like_count` int NOT NULL,
  `attach_flag` varchar(255) NOT NULL,
  `del_flag` varchar(255) NOT NULL,
  `deleted_by` varchar(60) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_community_author_id` (`author_id`),
  CONSTRAINT `FK_community_author_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
  )

CREATE TABLE IF NOT EXISTS `community_post_attachment` (
                                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                                         `community_post_id` bigint NOT NULL,
                                                         `attachment_id` bigint NOT NULL,
                                                         PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_community_post_attachment` (`community_post_id`,`attachment_id`),
  KEY `IDX_community_attach_id` (`attachment_id`),
  CONSTRAINT `FK_community_attach_post` FOREIGN KEY (`community_post_id`) REFERENCES `community_post` (`id`),
  CONSTRAINT `FK_community_attach_file` FOREIGN KEY (`attachment_id`) REFERENCES `attachment` (`id`)
  )

CREATE TABLE IF NOT EXISTS `community_post_likes` (
                                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                                    `post_id` bigint NOT NULL,
                                                    `username` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_community_likes` (`post_id`,`username`),
  CONSTRAINT `FK_community_likes_post` FOREIGN KEY (`post_id`) REFERENCES `community_post` (`id`)
  )

-- ==========================================
-- 8. 온라인 상담 게시판 (Counsel Post)
-- ==========================================
CREATE TABLE IF NOT EXISTS `counsel_post` (
                                            `id` bigint NOT NULL AUTO_INCREMENT,
                                            `author_id` bigint DEFAULT NULL, -- Users FK
                                            `author_name` varchar(100) NOT NULL,
  `author_email` varchar(120) DEFAULT NULL,
  `password_hash` varchar(100) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `content` mediumtext NOT NULL,
  `content_path` varchar(500) DEFAULT NULL,
  `status` enum('COMPLETE','END','WAIT') NOT NULL,
  `is_secret` varchar(255) NOT NULL,
  `view_count` int NOT NULL,
  `comment_count` int NOT NULL,
  `attach_flag` varchar(255) NOT NULL,
  `del_flag` varchar(255) NOT NULL,
  `deleted_by` varchar(60) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_counsel_author_id` (`author_id`),
  CONSTRAINT `FK_counsel_author_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
  )

CREATE TABLE IF NOT EXISTS `counsel_post_attachments` (
                                                        `id` int NOT NULL AUTO_INCREMENT,
                                                        `counsel_post_id` bigint NOT NULL,
                                                        `attachment_id` bigint NOT NULL,
                                                        PRIMARY KEY (`id`),
  KEY `IDX_counsel_attach_post` (`counsel_post_id`),
  KEY `IDX_counsel_attach_file` (`attachment_id`),
  CONSTRAINT `FK_counsel_attach_post` FOREIGN KEY (`counsel_post_id`) REFERENCES `counsel_post` (`id`),
  CONSTRAINT `FK_counsel_attach_file` FOREIGN KEY (`attachment_id`) REFERENCES `attachment` (`id`)
  )

CREATE TABLE IF NOT EXISTS `counsel_post_likes` (
                                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                                  `post_id` bigint NOT NULL,
                                                  `username` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_counsel_likes` (`post_id`,`username`),
  CONSTRAINT `FK_counsel_likes_post` FOREIGN KEY (`post_id`) REFERENCES `counsel_post` (`id`)
  )

CREATE TABLE IF NOT EXISTS `counsel_comment` (
                                               `id` bigint NOT NULL AUTO_INCREMENT,
                                               `post_id` bigint NOT NULL,
                                               `parent_id` bigint DEFAULT NULL,
                                               `author_name` varchar(100) NOT NULL,
  `author_email` varchar(120) DEFAULT NULL,
  `password_hash` varchar(100) DEFAULT NULL,
  `content` text NOT NULL,
  `is_staff_reply` varchar(255) NOT NULL,
  `del_flag` varchar(255) NOT NULL,
  `deleted_by` varchar(60) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_comment_post_created` (`post_id`,`created_at`),
  KEY `IDX_comment_parent` (`parent_id`),
  CONSTRAINT `FK_counsel_comment_post` FOREIGN KEY (`post_id`) REFERENCES `counsel_post` (`id`),
  CONSTRAINT `FK_counsel_comment_parent` FOREIGN KEY (`parent_id`) REFERENCES `counsel_comment` (`id`)
  )

CREATE TABLE IF NOT EXISTS `counsel_comment_attachment` (
                                                          `counsel_comment_id` bigint NOT NULL,
                                                          `attachment_id` bigint NOT NULL,
                                                          `sort_order` int NOT NULL,
                                                          `del_flag` varchar(255) NOT NULL,
  `deleted_by` varchar(60) DEFAULT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`attachment_id`,`counsel_comment_id`),
  KEY `IDX_comment_attach_comment` (`counsel_comment_id`),
  CONSTRAINT `FK_comment_attach_file` FOREIGN KEY (`attachment_id`) REFERENCES `attachment` (`id`),
  CONSTRAINT `FK_comment_attach_comment` FOREIGN KEY (`counsel_comment_id`) REFERENCES `counsel_comment` (`id`)
  )

-- ==========================================
-- 9. 포토 게시판 (Photo Post)
-- ==========================================
CREATE TABLE IF NOT EXISTS `photo_post` (
                                          `id` bigint NOT NULL AUTO_INCREMENT,
                                          `author_id` bigint DEFAULT NULL, -- Users FK
                                          `author` varchar(100) NOT NULL,
  `title` varchar(200) NOT NULL,
  `content` text,
  `thumbnailUrl` varchar(500) DEFAULT NULL,
  `viewCount` int NOT NULL,
  `likeCount` int NOT NULL,
  `del_flag` varchar(255) NOT NULL,
  `deleted_by` varchar(60) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `deleted_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_photo_created` (`created_at` DESC),
  KEY `IDX_photo_author_id` (`author_id`),
  CONSTRAINT `FK_photo_author_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`)
  )

CREATE TABLE IF NOT EXISTS `photo_post_attachment` (
                                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                                     `photo_post_id` bigint NOT NULL,
                                                     `attachment_id` bigint NOT NULL,
                                                     PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_photo_post_attachment` (`photo_post_id`,`attachment_id`),
  KEY `IDX_photo_attach_file` (`attachment_id`),
  CONSTRAINT `FK_photo_attach_post` FOREIGN KEY (`photo_post_id`) REFERENCES `photo_post` (`id`),
  CONSTRAINT `FK_photo_attach_file` FOREIGN KEY (`attachment_id`) REFERENCES `attachment` (`id`)
  )

CREATE TABLE IF NOT EXISTS `photo_post_likes` (
                                                `id` bigint NOT NULL AUTO_INCREMENT,
                                                `post_id` bigint NOT NULL,
                                                `username` varchar(50) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_photo_likes` (`post_id`,`username`),
  KEY `IDX_photo_likes_post` (`post_id`),
  KEY `IDX_photo_likes_username` (`username`),
  CONSTRAINT `FK_photo_likes_post` FOREIGN KEY (`post_id`) REFERENCES `photo_post` (`id`)
  )
