FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ARG TAR_FAILE=build/distributions/*.tar
ENV APP_DIR=/app/mydns
WORKDIR application
ADD ${TAR_FAILE} application
RUN ["chmod", "+x", "$APP_DIR/bin/myddns"]
ENTRYPOINT $APP_DIR/bin/myddns

