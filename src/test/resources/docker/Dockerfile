# Dockerfile

FROM docker.elastic.co/elasticsearch/elasticsearch:8.15.5

# Nori 플러그인 설치 명령
# --batch 옵션은 사용자 상호작용 없이 자동 설치를 진행합니다.
RUN bin/elasticsearch-plugin install --batch analysis-nori

# 필요한 경우 (선택 사항): JVM 메모리 설정
# ENV ES_JAVA_OPTS="-Xms512m -Xmx512m"

# 보안 비활성화 (테스트용) - Testcontainers withEnv로도 가능하지만, 여기에 명시해도 됨
# ENV xpack.security.enabled=false
# ENV xpack.security.http.ssl.enabled=false
# ENV xpack.security.transport.ssl.enabled=false