FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ARG TAR_FAILE=build/distributions/*.tar
RUN ["df", "-h"]
WORKDIR application
ADD ${TAR_FAILE} application
RUN ["chmod", "+x", "bin/myddns"]
ENTRYPOINT bin/myddns

