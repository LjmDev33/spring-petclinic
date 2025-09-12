FROM mysql:8.4

# 최소 유틸 설치: EL9 계열에서 동작하도록 microdnf/dnf/yum 순서로 시도
USER root
RUN set -eux; \
    if command -v microdnf >/dev/null 2>&1; then \
      microdnf -y update && \
      microdnf -y install vim-minimal less procps-ng iproute net-tools curl wget && \
      microdnf -y clean all; \
    elif command -v dnf >/dev/null 2>&1; then \
      dnf -y update && \
      dnf -y install vim-minimal less procps-ng iproute net-tools curl wget && \
      dnf -y clean all; \
    else \
      yum -y update && \
      yum -y install vim-minimal less procps-ng iproute net-tools curl wget && \
      yum -y clean all; \
    fi
USER mysql

# UTF-8 기본화
COPY my.cnf /etc/mysql/conf.d/charset.cnf

# 초기 스크립트 (공식 엔트리포인트가 첫 실행 시 자
