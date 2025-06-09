# 🧑‍💻 그룹 커뮤니티 프로젝트

## 📌 소개

본 프로젝트는 **Java, Spring**를 기반으로 한 **(스터디)그룹 커뮤니티 웹 서비스**입니다.  
사용자는 그룹을 생성, 가입, 관리할 수 있으며, 각 그룹 내에서 게시글, Q&A 게시판, 댓글 등 다양한 커뮤니케이션 기능을 제공합니다.

---

## 🛠 기술 스택

| 구분 | 기술 |
|------|------|
| **Backend** | Java 21, Spring Boot 3.4.5, Spring MVC, Spring Data JPA, QueryDSL, Spring Security |
| **Database** | MySQL |
| **검색엔진** | Elasticsearch 8.15.5 (Nori Analyzer 커스텀 설정) |
| **Cache & Session** | Redis |
| **DevOps** | Docker, Docker Compose, GitHub Actions (CI/CD) |
| **빌드 도구** | Gradle |

---

## 🚀 주요 기능

### 그룹 관리
- 그룹 생성, 수정, 삭제
- 그룹 가입/탈퇴 및 그룹장 변경
- 그룹별 멤버 권한 관리 (리더, 일반 멤버 등)

### 게시판 & Q&A 기능
- 그룹 내외 게시글 작성, 수정, 삭제
- 댓글 및 Q&A 답변 작성 (Q&A관련은 구현 예정)
- 게시글 고정 기능 및 공지사항 구분

### 카테고리 관리
- 그룹 필터링용 전역 Enum 카테고리 사용
- 그룹 내 개별 카테고리 관리 가능

### 검색 기능
- **그룹명 검색**: QueryDSL 적용
- **게시글 검색**: Elasticsearch 적용  
  - Nori 형태소 분석기 및 edge_ngram 자동완성 지원
  - 게시글 저장/수정/삭제 시 Elasticsearch와 이벤트 기반 비동기 동기화 처리

### 인증 및 권한 관리
- Spring Security 기반 인증/인가
- Redis를 통한 세션 관리

### CI/CD 및 배포 자동화
- GitHub Actions 기반 자동 빌드 및 테스트
- Docker 및 Docker Compose 설정 완료

---
