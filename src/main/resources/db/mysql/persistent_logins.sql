-- Spring Security Remember-Me 테이블 생성 SQL
-- Spring Security가 자동으로 Remember-Me 토큰을 관리하는 테이블

CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

-- 인덱스 생성 (성능 최적화)
CREATE INDEX idx_persistent_logins_username ON persistent_logins(username);
CREATE INDEX idx_persistent_logins_last_used ON persistent_logins(last_used);

-- 테이블 설명
-- username: 로그인한 사용자 ID
-- series: 토큰 시리즈 (고유값, Primary Key)
-- token: 인증 토큰 (매 로그인마다 재생성)
-- last_used: 토큰 마지막 사용 시간

-- 데이터 확인 쿼리
-- SELECT * FROM persistent_logins WHERE username = 'admin';

-- 만료된 토큰 삭제 (30일 이상 사용하지 않은 토큰)
-- DELETE FROM persistent_logins WHERE last_used < DATE_SUB(NOW(), INTERVAL 30 DAY);

