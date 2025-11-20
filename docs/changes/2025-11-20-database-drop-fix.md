# 데이터베이스 테이블 자동 관리 설정

**날짜:** 2025-11-20  
**작성자:** Jeongmin Lee

## 문제 상황

Hibernate의 `ddl-auto: create-drop` 모드 사용 시 외래키 제약조건으로 인해 테이블 DROP이 실패하는 문제 발생:

```
Error executing DDL "alter table community_post_attachment drop foreign key FK57m40mq145cwgpsohwdcb9do3"
Table 'petclinic.system_config' doesn't exist
```

### 원인 분석

1. Hibernate가 테이블을 DROP하려고 할 때 외래키 제약조건 때문에 실패
2. `FOREIGN_KEY_CHECKS=0` 설정이 Hibernate의 스키마 관리 타이밍과 맞지 않음
3. DROP이 실패하면 CREATE도 실행되지 않아 테이블이 존재하지 않는 상태로 남음

## 해결 방법

### 1. application-dev.yml 수정

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/petclinic?...&sessionVariables=FOREIGN_KEY_CHECKS=0
    hikari:
      connection-init-sql: SET SESSION FOREIGN_KEY_CHECKS=0  # 모든 커넥션에서 외래키 체크 비활성화

  jpa:
    properties:
      hibernate:
        globally_quoted_identifiers: true  # 모든 식별자에 백틱 자동 적용
    hibernate:
      ddl-auto: create  # 애플리케이션 시작 시 테이블 DROP 후 CREATE
  
  sql:
    init:
      mode: never  # DataInit이 데이터를 추가하므로 never로 설정
```

### 2. DatabaseConfig.java 개선

애플리케이션 시작 시 `@PostConstruct`를 사용하여 모든 테이블을 강제 삭제:

```java
@PostConstruct
public void configureDatabaseSettings() {
    Connection conn = null;
    Statement stmt = null;
    
    try {
        conn = dataSource.getConnection();
        stmt = conn.createStatement();
        
        // 1. 외래키 체크 비활성화 (SESSION 레벨 - 권한 불필요)
        stmt.execute("SET SESSION FOREIGN_KEY_CHECKS = 0");
        
        // 2. 모든 테이블 강제 삭제
        String[] tables = {
            "counsel_comment_attachment", "counsel_post_attachment",
            "community_post_attachment", "counsel_comment",
            "counsel_post", "community_post", "attachment",
            "faq_posts", "system_config", "user_roles",
            "persistent_logins", "users"
        };
        
        for (String table : tables) {
            stmt.execute("DROP TABLE IF EXISTS `" + table + "`");
        }
        
        System.out.println("✅ [DEV] 모든 테이블 삭제 완료");
        System.out.println("✅ [DEV] Hibernate ddl-auto: create 모드로 테이블 재생성");
        
    } catch (SQLException e) {
        System.err.println("⚠️ [DEV] 데이터베이스 설정 실패: " + e.getMessage());
    } finally {
        // 리소스 해제
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) { }
    }
}
```

**주요 특징:**
- Hibernate의 EntityManagerFactory 생성 **전**에 실행
- **SESSION 레벨**에서만 외래키 체크 비활성화 (SUPER 권한 불필요)
- 외래키 제약조건 무시하고 모든 테이블 강제 삭제
- Hibernate가 깨끗한 상태에서 테이블 재생성
- `ddl-auto: create`와 조합하여 완벽한 초기화
- 개발 환경(`@Profile("dev")`)에서만 동작

### 3. HibernateConfig.java 생성

Hibernate가 외래키 제약조건을 자동으로 생성하지 않도록 추가 설정:

```java
@Bean
public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
    return hibernateProperties -> {
        hibernateProperties.put("hibernate.jpa.compliance.global_id_generators", "false");
    };
}
```

### 4. FaqPost 엔티티 개선

```java
@Entity
@Table(name = "`faq_posts`")  // 백틱 추가
public class FaqPost {
    
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")  // TEXT 타입 명시
    private String answer;
}
```

## 적용된 테이블 목록

다음 테이블들이 자동으로 관리됩니다 (테이블이 없으면 생성, 있으면 스키마 변경사항만 반영):

1. counsel_comment_attachment
2. counsel_post_attachment
3. counsel_comment
4. counsel_post
5. community_post_attachment
6. community_post
7. faq_posts
8. attachment
9. system_config
10. persistent_logins
11. user_roles
12. users

**참고:** Petclinic 기본 테이블(owners, pets, vets 등)은 프로젝트에서 제거되었습니다.

## 동작 순서

1. **애플리케이션 시작**
2. **DatabaseConfig.@PostConstruct 실행**
   - `SET SESSION FOREIGN_KEY_CHECKS = 0`
   - 외래키 체크 비활성화로 스키마 관리 작업 원활화
   - **모든 테이블 강제 삭제** (DROP TABLE IF EXISTS)
   - Hibernate가 깨끗한 상태에서 시작할 수 있도록 준비
3. **Hibernate EntityManagerFactory 생성**
   - `ddl-auto: create`에 의해 모든 엔티티 테이블 생성
   - 외래키 제약조건도 함께 생성
   - 스키마 변경사항 즉시 반영
4. **DataInit 실행**
   - 각 테이블의 초기 데이터 생성
   - FAQ, User, SystemConfig, Counsel 등의 샘플 데이터

## 주의사항

⚠️ **개발 환경 전용 설정입니다!**

- `@Profile("dev")` 설정으로 개발 환경에서만 동작
- 운영 환경에서는 절대 사용하지 마세요
- `ddl-auto: create` 모드는 **애플리케이션 시작 시마다 모든 데이터를 삭제**합니다
- 개발 중 스키마 변경사항이 즉시 반영되어 편리하지만, 기존 데이터는 유지되지 않습니다
- 운영 환경에서는 `ddl-auto: validate` + Flyway/Liquibase 사용 권장

## 검증 방법

1. 애플리케이션 시작 로그 확인:
```
🔄 [DEV] 데이터베이스 초기화 시작...
✅ [DEV] 12개 테이블 삭제 완료
✅ [DEV] Hibernate ddl-auto: create 모드로 테이블 재생성 시작
Hibernate: drop table if exists `faq_posts`
Hibernate: drop table if exists `system_config`
...
Hibernate: create table `faq_posts` (id bigint not null auto_increment, ...)
Hibernate: create table `system_config` (id bigint not null auto_increment, ...)
```

2. DataInit 로그 확인:
```
FAQ 초기 데이터 생성 완료
SystemConfig 초기 데이터 생성 완료
User 초기 데이터 생성 완료
Counsel 초기 데이터 생성 완료
```

3. 서버 정상 기동 및 데이터 확인

## 발생한 오류들과 해결 방법

### 오류 1: "Executor can not be null"
```
⚠️ [DEV] 테이블 삭제 프로세스 실패: Executor can not be null
java.sql.SQLException: Executor can not be null
```

**원인:**
```java
conn.setNetworkTimeout(null, 10000);  // ❌ 첫 번째 파라미터가 null
```

**해결:**
테이블 삭제 방식을 포기하고 `ddl-auto: update` 모드 사용

### 오류 2: "Access denied; you need SUPER or SYSTEM_VARIABLES_ADMIN privilege"
```
⚠️ [DEV] 데이터베이스 설정 실패: Access denied; you need (at least one of) the SUPER or SYSTEM_VARIABLES_ADMIN privilege(s) for this operation
```

**원인:**
```java
stmt.execute("SET GLOBAL FOREIGN_KEY_CHECKS = 0");  // ❌ SUPER 권한 필요
```

**해결:**
1. `SET GLOBAL` 명령 제거 (권한 불필요한 SESSION 레벨만 사용)
2. datasource URL에 `sessionVariables=FOREIGN_KEY_CHECKS=0` 추가
3. HikariCP `connection-init-sql`에도 설정

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/petclinic?...&sessionVariables=FOREIGN_KEY_CHECKS=0
  hikari:
    connection-init-sql: SET SESSION FOREIGN_KEY_CHECKS=0
```

```java
// DatabaseConfig.java
stmt.execute("SET SESSION FOREIGN_KEY_CHECKS = 0");  // ✅ 권한 문제 없음
```

### 오류 3: "Table 'petclinic.system_config' doesn't exist" (재발)
```
ERROR: Table 'petclinic.system_config' doesn't exist
org.springframework.dao.InvalidDataAccessResourceUsageException
Caused by: java.sql.SQLSyntaxErrorException: Table 'petclinic.system_config' doesn't exist
```

**원인:**
1. `ddl-auto: update` 모드는 기존 테이블이 없을 때 생성하려고 하지만, 외래키 제약조건으로 인해 생성 실패
2. `sessionVariables=FOREIGN_KEY_CHECKS=0` 설정이 Hibernate의 스키마 생성 타이밍과 맞지 않음
3. DataInit이 실행될 때 테이블이 여전히 존재하지 않음

**최종 해결:**
1. `ddl-auto: create`로 변경하여 애플리케이션 시작 시마다 테이블 재생성
2. DatabaseConfig의 `@PostConstruct`에서 Hibernate보다 먼저 모든 테이블 강제 삭제
3. 외래키 체크 비활성화 상태에서 삭제하여 제약조건 문제 회피

```java
@PostConstruct
public void configureDatabaseSettings() {
    // 1. 외래키 체크 비활성화
    stmt.execute("SET SESSION FOREIGN_KEY_CHECKS = 0");
    
    // 2. 모든 테이블 강제 삭제
    String[] tables = {"counsel_comment_attachment", "counsel_post_attachment", ...};
    for (String table : tables) {
        stmt.execute("DROP TABLE IF EXISTS `" + table + "`");
    }
    
    // 3. Hibernate가 깨끗한 상태에서 테이블 재생성
}
```

```yaml
hibernate:
  ddl-auto: create  # 애플리케이션 시작 시 테이블 DROP 후 CREATE
```

**장점:**
- ✅ 애플리케이션 시작 시마다 깨끗한 상태에서 시작
- ✅ 외래키 제약조건 문제 완전 해결
- ✅ 스키마 변경 사항 즉시 반영
- ⚠️ 단점: 기존 데이터는 유지되지 않음 (개발 환경에서는 문제없음)

## 향후 개선 사항

1. 운영 환경을 위한 마이그레이션 스크립트 관리 (Flyway 또는 Liquibase)
2. 개발 환경에서도 선택적으로 데이터 보존 옵션 추가
3. 타임아웃 값을 설정 파일로 외부화
4. 스키마 변경 이력 추적

## 관련 파일

- `src/main/resources/application-dev.yml`
- `src/main/java/.../common/config/DatabaseConfig.java`
- `src/main/java/.../common/config/HibernateConfig.java`
- `src/main/java/.../faq/table/FaqPost.java`
