FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ENV APPDIR=/app/myddns
WORKDIR $APPDIR
RUN ["$APPDIR/gradlew", "clean", "distTar"]
RUN ["tar", "-xvf", "$APPDIR/build/distributions/myddns-1.0-SNAPSHOT.tar", "-C", "$APPDIR/"]
RUN ["chmod", "+x", "$APPDIR/bin/myddns"]
ENTRYPOINT $APPDIR/bin/myddns

