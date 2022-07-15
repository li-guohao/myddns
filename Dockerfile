FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ARG TAR_FAILE=build/distributions/*.tar
WORKDIR application
ADD ${TAR_FAILE} application
RUN ["chmod", "+x", "${application}/bin/myddns"]
ENTRYPOINT ${application}/bin/myddns

