# 닉네임 변경 이력 관리 방법

**작성일**: 2025-11-11  
**작성자**: Jeongmin Lee

---

## 📋 목차
1. [개요](#개요)
2. [방법 1: 별도 이력 테이블 생성](#방법-1-별도-이력-테이블-생성)
3. [방법 2: JSON 컬럼 사용](#방법-2-json-컬럼-사용)
4. [방법 3: 감사 로그 테이블 활용](#방법-3-감사-로그-테이블-활용)
5. [방법 4: 로그 파일 기록](#방법-4-로그-파일-기록)
6. [권장 방법](#권장-방법)

---

## 개요

닉네임 변경 이력을 관리하는 방법은 여러 가지가 있습니다. 각 방법의 장단점과 구현 방법을 비교합니다.

---

## 방법 1: 별도 이력 테이블 생성 ⭐ 권장

### 장점
- ✅ 데이터 정규화 (제3정규형)
- ✅ 무제한 이력 저장 가능
- ✅ 복잡한 조회 쿼리 작성 가능 (기간별, 사용자별)
- ✅ 인덱싱 최적화 가능
- ✅ 통계 분석 용이

### 단점
- ❌ 테이블 추가 필요
- ❌ JOIN 쿼리 필요
- ❌ 스토리지 증가

### 구현 예시

#### Entity 클래스
```java
@Entity
@Table(name = "user_nickname_history")
public class UserNicknameHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "old_nickname", length = 30)
    private String oldNickname;
    
    @Column(name = "new_nickname", nullable = false, length = 30)
    private String newNickname;
    
    @Column(name = "changed_by", nullable = false, length = 50)
    private String changedBy;  // 변경한 사용자 (본인 또는 관리자)
    
    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "reason", length = 200)
    private String reason;  // 변경 사유 (선택)
    
    // Getters and Setters
}
```

#### Repository
```java
@Repository
public interface UserNicknameHistoryRepository extends JpaRepository<UserNicknameHistory, Long> {
    List<UserNicknameHistory> findByUserIdOrderByChangedAtDesc(Long userId);
    
    List<UserNicknameHistory> findByUserIdAndChangedAtBetween(
        Long userId, 
        LocalDateTime start, 
        LocalDateTime end
    );
}
```

#### Service
```java
@Service
@Transactional
public class UserService {
    private final UserNicknameHistoryRepository historyRepository;
    
    public void updateProfile(String username, String email, String name, 
                              String newNickname, String phone, String ipAddress) {
        User user = findByUsername(username);
        String oldNickname = user.getNickname();
        
        // 닉네임 변경 시 이력 저장
        if (!oldNickname.equals(newNickname)) {
            // 닉네임 중복 검증
            if (userRepository.existsByNickname(newNickname)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            
            // 이력 저장
            UserNicknameHistory history = new UserNicknameHistory();
            history.setUserId(user.getId());
            history.setOldNickname(oldNickname);
            history.setNewNickname(newNickname);
            history.setChangedBy(username);
            history.setIpAddress(ipAddress);
            historyRepository.save(history);
            
            log.info("Nickname changed: userId={}, old={}, new={}, ip={}", 
                     user.getId(), oldNickname, newNickname, ipAddress);
        }
        
        user.setNickname(newNickname);
        // ... 기타 필드 업데이트
        userRepository.save(user);
    }
    
    // 이력 조회
    public List<UserNicknameHistory> getNicknameHistory(Long userId) {
        return historyRepository.findByUserIdOrderByChangedAtDesc(userId);
    }
}
```

#### 조회 쿼리 예시
```sql
-- 사용자의 전체 닉네임 변경 이력
SELECT * FROM user_nickname_history 
WHERE user_id = 1 
ORDER BY changed_at DESC;

-- 최근 7일간 닉네임 변경 이력
SELECT * FROM user_nickname_history 
WHERE user_id = 1 
  AND changed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY);

-- 특정 닉네임을 사용했던 사용자 찾기
SELECT DISTINCT user_id 
FROM user_nickname_history 
WHERE new_nickname = '특정닉네임' OR old_nickname = '특정닉네임';
```

---

## 방법 2: JSON 컬럼 사용

### 장점
- ✅ 테이블 추가 불필요
- ✅ 단순한 구조

### 단점
- ❌ 복잡한 조회 쿼리 작성 어려움
- ❌ 인덱싱 제한
- ❌ MySQL 5.7+ 필요
- ❌ 대용량 데이터 처리 성능 저하

### 구현 예시

#### Entity 클래스
```java
@Entity
@Table(name = "users")
public class User {
    // ... 기존 필드
    
    @Column(name = "nickname_history", columnDefinition = "JSON")
    private String nicknameHistory;  // JSON 배열 저장
    
    // JSON 파싱 헬퍼 메서드
    public void addNicknameHistory(String oldNickname, String newNickname, String changedBy) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<Map<String, Object>> history = nicknameHistory == null ? 
                new ArrayList<>() : 
                mapper.readValue(nicknameHistory, new TypeReference<List<Map<String, Object>>>() {});
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("oldNickname", oldNickname);
            entry.put("newNickname", newNickname);
            entry.put("changedBy", changedBy);
            entry.put("changedAt", LocalDateTime.now().toString());
            
            history.add(0, entry);  // 최신 이력을 앞에 추가
            
            // 최대 50개만 유지 (용량 제한)
            if (history.size() > 50) {
                history = history.subList(0, 50);
            }
            
            this.nicknameHistory = mapper.writeValueAsString(history);
        } catch (JsonProcessingException e) {
            log.error("Failed to update nickname history", e);
        }
    }
}
```

#### JSON 데이터 예시
```json
[
  {
    "oldNickname": "구닉네임1",
    "newNickname": "새닉네임1",
    "changedBy": "user123",
    "changedAt": "2025-11-11T14:30:00"
  },
  {
    "oldNickname": "구닉네임2",
    "newNickname": "구닉네임1",
    "changedBy": "user123",
    "changedAt": "2025-11-01T10:00:00"
  }
]
```

---

## 방법 3: 감사 로그 테이블 활용

### 장점
- ✅ 통합 감사 로그로 관리
- ✅ 닉네임 외 다른 정보 변경도 함께 추적
- ✅ 컴플라이언스 요구사항 충족

### 단점
- ❌ 닉네임 전용 조회 시 필터링 필요
- ❌ 테이블 크기 증가

### 구현 예시

#### Entity 클래스
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String entityType;  // "USER"
    
    @Column(nullable = false)
    private Long entityId;  // user_id
    
    @Column(nullable = false)
    private String action;  // "UPDATE"
    
    @Column(nullable = false)
    private String fieldName;  // "nickname"
    
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(columnDefinition = "TEXT")
    private String newValue;
    
    @Column(nullable = false)
    private String changedBy;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
    
    @Column(length = 50)
    private String ipAddress;
}
```

#### Service
```java
public void updateProfile(String username, String newNickname) {
    User user = findByUsername(username);
    String oldNickname = user.getNickname();
    
    if (!oldNickname.equals(newNickname)) {
        // 감사 로그 저장
        AuditLog log = new AuditLog();
        log.setEntityType("USER");
        log.setEntityId(user.getId());
        log.setAction("UPDATE");
        log.setFieldName("nickname");
        log.setOldValue(oldNickname);
        log.setNewValue(newNickname);
        log.setChangedBy(username);
        auditLogRepository.save(log);
    }
    
    user.setNickname(newNickname);
    userRepository.save(user);
}
```

---

## 방법 4: 로그 파일 기록

### 장점
- ✅ 데이터베이스 부하 없음
- ✅ 구현 간단

### 단점
- ❌ 조회 어려움
- ❌ 통계 분석 불가
- ❌ 백업/복구 어려움

### 구현 예시

```java
public void updateProfile(String username, String newNickname) {
    User user = findByUsername(username);
    String oldNickname = user.getNickname();
    
    if (!oldNickname.equals(newNickname)) {
        log.info("Nickname changed: userId={}, username={}, old={}, new={}, timestamp={}", 
                 user.getId(), username, oldNickname, newNickname, LocalDateTime.now());
    }
    
    user.setNickname(newNickname);
    userRepository.save(user);
}
```

#### 로그 파일 예시
```
2025-11-11 14:30:00 INFO  UserService - Nickname changed: userId=1, username=user123, old=구닉네임, new=새닉네임, timestamp=2025-11-11T14:30:00
```

---

## 권장 방법

### 🥇 1순위: 방법 1 (별도 이력 테이블 생성)

**이유**:
- 데이터 정규화 및 확장성
- 복잡한 조회 및 통계 분석 가능
- 인덱싱 최적화 가능
- 무제한 이력 저장

**사용 시나리오**:
- 닉네임 변경 빈도가 높은 경우
- 통계 분석 필요
- 관리자 페이지에서 이력 조회 필요

---

### 🥈 2순위: 방법 3 (감사 로그 테이블 활용)

**이유**:
- 통합 감사 로그로 관리
- 컴플라이언스 요구사항 충족
- 닉네임 외 다른 정보 변경도 함께 추적

**사용 시나리오**:
- 이미 감사 로그 시스템이 있는 경우
- 전체 데이터 변경 이력 추적 필요

---

### 🥉 3순위: 방법 4 (로그 파일 기록)

**이유**:
- 간단한 구현
- 데이터베이스 부하 없음

**사용 시나리오**:
- 닉네임 변경 빈도가 낮은 경우
- 단순 모니터링 목적

---

## 구현 우선순위

### 현재 권장: 방법 4 (로그 파일 기록) + 향후 방법 1로 마이그레이션

**1단계 (현재)**: 로그 파일 기록
```java
log.info("Nickname changed: userId={}, old={}, new={}", user.getId(), oldNickname, newNickname);
```

**2단계 (향후)**: 별도 이력 테이블 생성
- 닉네임 변경 빈도가 높아지면 마이그레이션
- 기존 로그 파일 데이터를 테이블로 Import

---

## 변경 이력

### [1.0] - 2025-11-11
- 최초 문서 작성
- 4가지 방법 비교 및 권장 사항 제시

---

**문서 버전**: 1.0  
**최종 검토**: 2025-11-11  
**담당자**: Jeongmin Lee

