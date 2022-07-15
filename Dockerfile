FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ARG TAR_FAILE=build/distributions/*.tar
WORKDIR /app/mydns
ADD ${TAR_FAILE} /app/mydns
RUN ["/bin/ls", "/app/mydns"]
RUN ["chmod", "+x", "/app/mydns/bin/myddns"]
ENTRYPOINT /app/mydns/bin/myddns

