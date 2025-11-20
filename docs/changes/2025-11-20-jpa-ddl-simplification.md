# JPA DDL 자동 생성 단순화 및 정리

**날짜:** 2025-11-20  
**작성자:** Jeongmin Lee

## 문제 상황

`ddl-auto: create` 설정에도 불구하고 테이블 DROP, CREATE, 데이터 생성이 제대로 작동하지 않는 문제 발생.

**근본 원인:**
- DatabaseConfig의 `@PostConstruct`가 Hibernate EntityManagerFactory 생성 **이후**에 실행됨
- 테이블 삭제가 Hibernate 스키마 생성보다 늦게 실행되는 역순 문제
- 불필요하게 복잡한 설정이 오히려 문제를 야기

## 해결 방법: 단순화

### 삭제한 파일
1. ❌ `DatabaseConfig.java` - 불필요한 복잡성 제거
2. ❌ `HibernateConfig.java` - 불필요한 복잡성 제거

### 최종 설정 (application-dev.yml)

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/petclinic?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=Asia/Seoul
    username: dev33
    password: ezflow_010

  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        jdbc:
          batch_size: 50
    hibernate:
      ddl-auto: create  # 테이블 DROP 후 CREATE
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: true
    open-in-view: false

  thymeleaf:
    mode: HTML
    cache: false

  main:
    allow-bean-definition-overriding: true

  messages:
    basename: messages/messages,messages/ErrorMessages
    encoding: UTF-8
    cache-duration: 5m

  jackson:
    serialization:
      INDENT_OUTPUT: true

server:
  port: 8080

petclinic:
  counsel:
    upload-dir: C:/eGovFrameDev-3.9.0-64bit/petclinic/data/counsel/uploads
    content-dir: C:/eGovFrameDev-3.9.0-64bit/petclinic/data/counsel/contents
```

## 동작 방식

1. **애플리케이션 시작**
2. **Hibernate가 자동으로:**
   - 기존 테이블 DROP
   - 엔티티 기반으로 테이블 CREATE
   - 외래키 제약조건 자동 생성
3. **DataInit 실행**
   - 각 모듈의 초기 데이터 자동 생성
   - FAQ, SystemConfig, User, Counsel 등

## 왜 단순한 것이 더 좋은가?

### Before (복잡함)
```
DatabaseConfig @PostConstruct (타이밍 문제!)
  ↓
테이블 수동 삭제 시도
  ↓
Hibernate 스키마 생성 (이미 실행됨 → 실패)
  ↓
DataInit (테이블 없음 → 오류)
```

### After (단순함)
```
Hibernate ddl-auto: create
  ↓
자동 DROP → 자동 CREATE
  ↓
DataInit → 데이터 생성
```

## 장점

✅ **단순함:** 설정 파일 하나로 모든 것 해결  
✅ **타이밍 문제 없음:** Hibernate가 알아서 순서 관리  
✅ **외래키 자동 처리:** 별도 설정 불필요  
✅ **유지보수 용이:** 복잡한 로직 제거  
✅ **신뢰성:** Spring Boot 기본 동작에 의존

## 주의사항

⚠️ **개발 환경 전용**
- `ddl-auto: create`는 애플리케이션 시작 시마다 모든 데이터 삭제
- 개발 중 스키마 변경사항 즉시 반영
- 운영 환경에서는 `ddl-auto: validate` + Flyway/Liquibase 사용 권장

## 결론

**"Keep It Simple, Stupid (KISS)"** 원칙 적용
- 불필요한 복잡성 제거
- Spring Boot 기본 기능 신뢰
- 문제는 항상 단순한 해결책이 있음

