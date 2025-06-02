# 베이스 이미지 (JDK 21, Alpine 기반)
FROM eclipse-temurin:21-jdk-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사 (프로젝트 루트 기준 경로 확인 필요)
COPY build/libs/group_platform.jar app.jar

# 컨테이너가 사용하는 포트 명시 (Spring 기본 8080)
EXPOSE 8080

# 메모리 옵션을 포함한 애플리케이션 실행 명령
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]