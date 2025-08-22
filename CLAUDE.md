# 프로젝트 개요
- 퍼피톡은 생성형 AI 기반, 반려동물 채팅 서비스입니다.

## 기능 소개
### 반려동물 생성
- 사용자가 반려동물의 페르소나를 선택하면, 이를 기반으로 반려동물을 생성한다.
- 반려동물의 페르소나는 수정할 수 없다.

### 채팅
- 생성한 반려동물과 채팅한다.
- 반려동물과 채팅방은 1:1 관계다.

### 알림
- 서비스에 마지막으로 접속한지 2시간이 지난 사용자를 대상으로 반려동물이 채팅을 보낸다.
- 채팅은 반려동물과의 이전 대화와 페르소나를 기반으로 작성한다.
- 반려동물이 채팅을 보내면 사용자는 알림을 받는다.

# 아키텍처 구조

```
root/
├── api/                    # 프레젠테이션 레이어 (REST API)
├── application/            # 애플리케이션 레이어 (Facade)
├── domain/                 # 도메인 레이어 (엔티티, 비즈니스 로직)
├── infrastructure/         # 인프라스트럭처 레이어 (데이터 액세스)
└── shared/                 # 공통 모듈 (유틸리티, 공유 클래스)
```

## 프로젝트 구조
- api/: 컨트롤러, DTO, 입력 값 검증
- application/: 유스케이스, 포트, DTO, 이벤트 핸들러, 예외 처리 등
- domain/: 엔티티, 값 객체, 도메인 서비스, 정책, 이벤트 등
- infrastructure/: JPA, 외부 연동, 설정 등 구현체

### 1. api
- 목적: REST API, 컨트롤러, 프레젠테이션 레이어
- 주요 클래스: `UserController`, `GlobalExceptionHandler`, DTOs(UserCreateRequest, UserResponse, UsersResponse)
- 의존성: application module, Spring Boot Web

### 2. application
- 목적: Facade 패턴 구현
- 주요 클래스: `UserFacade`, DTOs(UserCreateCommand, UserCreateResult)
- 의존성: domain module
- 규칙:
    - Facade가 직접 비즈니스 로직을 구현하면 안 됨
    - 비즈니스 규칙은 반드시 Domain Layer(Entity, VO, Domain Service)에 있어야 함
    - Facade는 단지 도메인 객체/서비스를 조합하고, 트랜잭션/흐름 제어를 책임짐

### 3. domain
- 목적: 도메인 모델, 엔티티, 도메인 서비스 (순수한 비즈니스 규칙)
- 주요 클래스: `User`, `UserStatus`, `UserDomainService`, `UserRepository(interface)`
- 의존성: 외부에 의존하지 않음
- 규칙:
    - 도메인 로직에서 @Transactional 선언 
    
### 4. infrastructure
- 목적: 데이터 액세스, 외부 시스템 연동, 기술적 구현
- 주요 클래스: `UserRepositoryImpl`, `UserJpaRepository`, `UserJpaEntity`
- 의존성: domain module, 외부 라이브러리(Spring Data Jpa)

### 5. shared
- 목적: 공통 유틸리티, 상수, 공유 인터페이스, 가급적 사용 지양
- 주요 클래스: `BaseEntity`, `ApiResponse`

# 기술 스택
- Spring Framework 3.xx 이상
- Spring Security는 사용 금지
- Java 17 이상
- Gradle
- MySQL
- H2(Test)

# 개발 원칙

## 메서드명
- 목록 조회 : {domain}List
- 단건 조회 : {domain}

## 예외 처리
- GlobalExceptionHandler를 구현하여 예외를 처리
- try catch 사용 지양

## Modern Java 활용
- 멀티 쓰레딩 환경을 고려하여 객체의 불변성 유지를 위해 record 활용
- JPA Entity와 같이 record 사용이 불가능한 경우를 제외하고, record 적극 활용
- stream과 함수형 인터페이스 활용

## 도메인 모델
- 상태와 행위를 함께 가짐
- 로직은 도메인 객체 내부에 위치
- 생성자는 private로 선언하여 외부에서 호출하는 것을 막고, 정적 팩토리 메서드를 통해 객체를 생성
    - 정적 팩토리 메서드 네이밍 컨벤션 준수
- 도메인 모델과 JPA Entity 분리

## 테스트
- Mockito 라이브러리 사용 금지(mock object 생성하여 테스트)