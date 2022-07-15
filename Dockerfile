FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
RUN ["ll"]
ARG TAR_FAILE=build/distributions/*.tar
WORKDIR application
ADD ${TAR_FAILE} application
RUN ["chmod", "+x", "bin/myddns"]
ENTRYPOINT bin/myddns

