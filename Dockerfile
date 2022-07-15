FROM eclipse-temurin:17-jre as builder
MAINTAINER li-guohao
ARG APPDIR=/app/myddns
WORKDIR $APPDIR
RUN gradle clean distTar
RUN tar -xvf $APPDIR/build/distributions/myddns-1.0-SNAPSHOT.tar $APPDIR/
CMD chmod +x $APPDIR/bin/myddns
ENTRYPOINT $APPDIR/bin/myddns

