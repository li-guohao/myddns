FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ARG TAR_FAILE=build/distributions/*.tar
WORKDIR /app/mydns
ADD ${TAR_FAILE} /app/
RUN ["/bin/ls", "/app"]
RUN ["mv", "/app/mydns/myddns-0.0.1", "/app/mydns"]
RUN ["/bin/ls", "/app/mydns"]
RUN ["chmod", "+x", "/app/mydns/bin/myddns"]
ENTRYPOINT /app/mydns/bin/myddns

